/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.blockentity;

import li.cil.oc2.api.bus.DeviceBusElement;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.DeviceTypes;
import li.cil.oc2.api.bus.device.provider.ItemDeviceQuery;
import li.cil.oc2.api.capabilities.TerminalUserProvider;
import li.cil.oc2.client.audio.LoopingSoundManager;
import li.cil.oc2.common.Config;
import li.cil.oc2.common.block.ComputerBlock;
import li.cil.oc2.common.block.ProjectorBlock;
import li.cil.oc2.common.bus.AbstractBlockDeviceBusElement;
import li.cil.oc2.common.bus.BlockDeviceBusController;
import li.cil.oc2.common.bus.CommonDeviceBusController;
import li.cil.oc2.common.bus.device.BlockDeviceBusElement;
import li.cil.oc2.common.bus.device.util.Devices;
import li.cil.oc2.common.bus.device.vm.block.KeyboardDevice;
import li.cil.oc2.common.bus.device.vm.block.MonitorDevice;
import li.cil.oc2.common.bus.device.vm.block.ProjectorDevice;
import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.container.ComputerInventoryContainer;
import li.cil.oc2.common.container.ComputerTerminalContainer;
import li.cil.oc2.common.container.MonitorDisplayContainer;
import li.cil.oc2.common.energy.FixedEnergyStorage;
import li.cil.oc2.common.network.MonitorLoadBalancer;
import li.cil.oc2.common.network.Network;
import li.cil.oc2.common.network.ProjectorLoadBalancer;
import li.cil.oc2.common.network.message.*;
import li.cil.oc2.common.serialization.NBTSerialization;
import li.cil.oc2.common.util.*;
import li.cil.oc2.common.vm.*;
import li.cil.oc2.jcodec.codecs.h264.H264Decoder;
import li.cil.oc2.jcodec.codecs.h264.H264Encoder;
import li.cil.oc2.jcodec.codecs.h264.encode.CQPRateControl;
import li.cil.oc2.jcodec.common.model.ColorSpace;
import li.cil.oc2.jcodec.common.model.Picture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static li.cil.oc2.common.Constants.BLOCK_ENTITY_TAG_NAME_IN_ITEM;
import static li.cil.oc2.common.Constants.ITEMS_TAG_NAME;

public final class MonitorBlockEntity extends ModBlockEntity implements TickableBlockEntity {
    private static final String STATE_TAG_NAME = "state";
    private static final String ENERGY_TAG_NAME = "energy";
    private static final String IS_PROJECTING_TAG_NAME = "projecting";
    private static final String HAS_ENERGY_TAG_NAME = "has_energy";

    private static final ExecutorService DECODER_WORKERS = Executors.newCachedThreadPool(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("Projector Frame Decoder");
        return thread;
    });

    ///////////////////////////////////////////////////////////////////

    private final FixedEnergyStorage energy = new FixedEnergyStorage(Config.computerEnergyStorage);

    ///////////////////////////////////////////////////////////////////

    private boolean hasEnergy;
    private boolean isMounted;
    private boolean isPowered;

    @Nullable private CompletableFuture<?> runningDecode;
    private final H264Decoder decoder = new H264Decoder();
    private final ByteBuffer decoderBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    @Nullable private ProjectorBlockEntity.FrameConsumer frameConsumer;

    private boolean needsIDR;
    private final BlockDeviceBusElement busElement = new BlockDeviceBusElement();
    private final MonitorDevice monitorDevice = new MonitorDevice(this, this::handleMountedChanged);
    private final KeyboardDevice<BlockEntity> keyboardDevice = new KeyboardDevice<>(this);
    private final Picture picture = Picture.create(MonitorDevice.WIDTH, MonitorDevice.HEIGHT, ColorSpace.YUV420J);

    private final H264Encoder encoder = new H264Encoder(new CQPRateControl(12));
    private final ByteBuffer encoderBuffer = ByteBuffer.allocateDirect(1024 * 1024);


    ///////////////////////////////////////////////////////////////////

    public void setRequiresKeyframe() {
        needsIDR = true;
    }

    public boolean getPowerState() { return isPowered; }

    public boolean isMounted() { return isMounted; }

    private long lastKeepAliveSentAt;

    public void handleInput(final int keycode, final boolean isDown) {
        keyboardDevice.sendKeyEvent(keycode, isDown);
    }

    @Nullable
    private ByteBuffer encodeFrame() {
        final boolean hasChanges = monitorDevice.applyChanges(picture);
        if (!hasChanges && !needsIDR) {
            return null;
        }

        encoderBuffer.clear();
        final ByteBuffer frameData;
        try {
            if (needsIDR) {
                frameData = encoder.encodeIDRFrame(picture, encoderBuffer);
                needsIDR = false;
            } else {
                frameData = encoder.encodeFrame(picture, encoderBuffer).data();
            }
        } catch (final BufferOverflowException ignored) {
            return null;
        }

        final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(frameData);
        deflater.finish();
        final ByteBuffer compressedFrameData = ByteBuffer.allocateDirect(1024 * 1024);
        deflater.deflate(compressedFrameData, Deflater.FULL_FLUSH);
        deflater.end();
        compressedFrameData.flip();

        return compressedFrameData;
    }

    private void handleMountedChanged(final boolean value) {
        updateProjectorState(value, hasEnergy);
    }

    public void setFrameConsumer(@Nullable final ProjectorBlockEntity.FrameConsumer consumer) {
        if (consumer == frameConsumer) {
            return;
        }
        synchronized (picture) {
            this.frameConsumer = consumer;
            if (frameConsumer != null) {
                frameConsumer.processFrame(picture);
            }
        }
    }

    private void updateProjectorState(final boolean isMounted, final boolean hasEnergy) {
        if (isMounted == this.isMounted && hasEnergy == this.hasEnergy) {
            return;
        }

        // We may get called from unmount() of our device, which can be triggered due to chunk unload.
        // Hence, we need to check the loaded state here, lest we ghost load the chunk, breaking everything.
        if (level != null && !level.isClientSide() && level.isLoaded(getBlockPos())) {
            if (this.isMounted && !isMounted) {
                Arrays.fill(picture.getPlaneData(0), (byte) -128);
                Arrays.fill(picture.getPlaneData(1), (byte) 0);
                Arrays.fill(picture.getPlaneData(2), (byte) 0);
            }

            this.isMounted = isMounted;
            this.hasEnergy = hasEnergy;

            level.setBlock(getBlockPos(), getBlockState().setValue(ProjectorBlock.LIT, isMounted), Block.UPDATE_CLIENTS);

            Network.sendToClientsTrackingBlockEntity(new MonitorStateMessage(this, isMounted, hasEnergy), this);
        }
    }

    public void applyMonitorStateClient(final boolean isProjecting, final boolean hasEnergy) {
        if (level == null || !level.isClientSide()) {
            return;
        }

        this.isMounted = isProjecting;
        this.hasEnergy = hasEnergy;
    }

    public MonitorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.MONITOR.get(), pos, state);

        busElement.addDevice(keyboardDevice);
        busElement.addDevice(monitorDevice);

        encoder.setKeyInterval(100);

        // We want to unload devices even on level unload to free global resources.
        setNeedsLevelUnloadEvent();
    }

    public void start() {
        isPowered = true;
    }

    public void stop() {
        isPowered = false;
    }

    public void openTerminalScreen(final ServerPlayer player) {
        MonitorDisplayContainer.createServer(this, energy, player);
    }

    public void onRendering() {
        final long now = System.currentTimeMillis();
        if (now - lastKeepAliveSentAt > 1000) {
            lastKeepAliveSentAt = now;
            Network.sendToServer(new MonitorRequestFramebufferMessage(this));
        }
    }

    @Override
    public void serverTick() {
        if (level == null) {
            return;
        }

        final boolean hasPowered;
        if (Config.projectorsUseEnergy()) {
            hasPowered = energy.extractEnergy(Config.projectorEnergyPerTick, true) >= Config.projectorEnergyPerTick;
            if (hasPowered) {
                energy.extractEnergy(Config.projectorEnergyPerTick, false);
            }
        } else {
            hasPowered = true;
        }

        updateProjectorState(isMounted, isPowered);

        if (!hasEnergy || !isPowered || (!monitorDevice.hasChanges() && !needsIDR)) {
            return;
        }

        MonitorLoadBalancer.offerFrame(this, this::encodeFrame);
    }

    public void applyNextFrameClient(final ByteBuffer frameData) {
        if (level == null || !level.isClientSide()) {
            return;
        }

        final CompletableFuture<?> lastDecode = runningDecode;
        runningDecode = CompletableFuture.runAsync(() -> {
            try {
                try {
                    if (lastDecode != null) lastDecode.join();
                } catch (final CompletionException ignored) {
                }

                final Inflater inflater = new Inflater();
                inflater.setInput(frameData);

                decoderBuffer.clear();
                inflater.inflate(decoderBuffer);
                decoderBuffer.flip();

                decoder.decodeFrame(decoderBuffer, picture.getData());

                synchronized (picture) {
                    if (frameConsumer != null) {
                        frameConsumer.processFrame(picture);
                    }
                }
            } catch (final DataFormatException ignored) {
            }
        }, DECODER_WORKERS);
    }

    @Override
    public CompoundTag getUpdateTag() {
        final CompoundTag tag = super.getUpdateTag();

        tag.putBoolean(IS_PROJECTING_TAG_NAME, isMounted);
        tag.putBoolean(HAS_ENERGY_TAG_NAME, hasEnergy);
        tag.putBoolean(STATE_TAG_NAME, isPowered);

        return tag;
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag) {
        super.handleUpdateTag(tag);

        isMounted = tag.getBoolean(IS_PROJECTING_TAG_NAME);
        hasEnergy = tag.getBoolean(HAS_ENERGY_TAG_NAME);
        isPowered = tag.getBoolean(STATE_TAG_NAME);
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put(ENERGY_TAG_NAME, energy.serializeNBT());
        tag.putBoolean(IS_PROJECTING_TAG_NAME, isPowered);
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);

        energy.deserializeNBT(tag.getCompound(ENERGY_TAG_NAME));
        hasEnergy = tag.getBoolean(HAS_ENERGY_TAG_NAME);
        isPowered = tag.getBoolean(IS_PROJECTING_TAG_NAME);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void collectCapabilities(final CapabilityCollector collector, @Nullable final Direction direction) {
        collector.offer(Capabilities.deviceBusElement(), busElement);
        if (Config.computersUseEnergy()) {
            collector.offer(Capabilities.energyStorage(), energy);
        }
    }
}
