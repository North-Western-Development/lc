/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.api.API;
import li.cil.oc2.api.bus.device.DeviceTypes;
import li.cil.oc2.common.block.Blocks;
import li.cil.oc2.common.block.ComputerBlock;
import li.cil.oc2.common.util.NBTUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import static li.cil.oc2.common.Constants.BLOCK_ENTITY_TAG_NAME_IN_ITEM;
import static li.cil.oc2.common.Constants.ITEMS_TAG_NAME;
import static li.cil.oc2.common.util.NBTUtils.makeInventoryTag;
import static li.cil.oc2.common.util.RegistryUtils.key;
import static li.cil.oc2.common.util.TranslationUtils.text;

public final class ItemGroup {
    public static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, API.MOD_ID);

    public static final RegistryObject<CreativeModeTab> COMMON_TAB = TAB_REGISTER.register("common", () -> CreativeModeTab.builder()
        // Set name of tab to display
        .title(Component.translatable("item_group." + API.MOD_ID + ".common"))
        // Set icon of creative tab
        .icon(() -> new ItemStack(Items.COMPUTER.get()))
        // Add default items to tab
        .displayItems((params, output) -> {
            // Items
            output.accept(Items.BUS_CABLE.get());
            output.accept(Items.BUS_INTERFACE.get());
            output.accept(Items.CHARGER.get());
            // Computer With Flash
            ItemStack computerWithFlash = new ItemStack(Blocks.COMPUTER.get());

            final CompoundTag itemsTag = NBTUtils.getOrCreateChildTag(computerWithFlash.getOrCreateTag(), BLOCK_ENTITY_TAG_NAME_IN_ITEM, ITEMS_TAG_NAME);
            itemsTag.put(key(DeviceTypes.FLASH_MEMORY), makeInventoryTag(
                new ItemStack(Items.FLASH_MEMORY_CUSTOM.get())
            ));

            output.accept(computerWithFlash);

            // Preconfigured Computer
            ItemStack preconfiguredComputer = new ItemStack(Blocks.COMPUTER.get());

            final CompoundTag preconfiguredItemsTag = NBTUtils.getOrCreateChildTag(preconfiguredComputer.getOrCreateTag(), BLOCK_ENTITY_TAG_NAME_IN_ITEM, ITEMS_TAG_NAME);
            preconfiguredItemsTag.put(key(DeviceTypes.FLASH_MEMORY), makeInventoryTag(
                new ItemStack(Items.FLASH_MEMORY_CUSTOM.get())
            ));
            preconfiguredItemsTag.put(key(DeviceTypes.MEMORY), makeInventoryTag(
                new ItemStack(Items.MEMORY_LARGE.get()),
                new ItemStack(Items.MEMORY_LARGE.get()),
                new ItemStack(Items.MEMORY_LARGE.get()),
                new ItemStack(Items.MEMORY_LARGE.get())
            ));
            preconfiguredItemsTag.put(key(DeviceTypes.HARD_DRIVE), makeInventoryTag(
                new ItemStack(Items.HARD_DRIVE_CUSTOM.get())
            ));
            preconfiguredItemsTag.put(key(DeviceTypes.CARD), makeInventoryTag(
                new ItemStack(Items.NETWORK_INTERFACE_CARD.get())
            ));

            preconfiguredComputer.setHoverName(text("block.{mod}.computer.preconfigured"));

            output.accept(preconfiguredComputer);

            // Normal Items Again
            output.accept(Items.CREATIVE_ENERGY.get());
            output.accept(Items.DISK_DRIVE.get());
            output.accept(Items.FLASH_MEMORY_FLASHER.get());
            output.accept(Items.KEYBOARD.get());
            output.accept(Items.NETWORK_CONNECTOR.get());
            output.accept(Items.NETWORK_HUB.get());
            output.accept(Items.PROJECTOR.get());
            output.accept(Items.REDSTONE_INTERFACE.get());
            output.accept(Items.WRENCH.get());
            output.accept(Items.MANUAL.get());
            output.accept(Items.ROBOT.get());
            output.accept(Items.NETWORK_CABLE.get());
            output.accept(Items.MEMORY_SMALL.get());
            output.accept(Items.MEMORY_MEDIUM.get());
            output.accept(Items.MEMORY_LARGE.get());
            output.accept(Items.MEMORY_EXTRA_LARGE.get());
            output.accept(Items.HARD_DRIVE_SMALL.get());
            output.accept(Items.HARD_DRIVE_MEDIUM.get());
            output.accept(Items.HARD_DRIVE_LARGE.get());
            output.accept(Items.HARD_DRIVE_EXTRA_LARGE.get());
            output.accept(Items.HARD_DRIVE_CUSTOM.get());
            output.accept(Items.FLASH_MEMORY.get());
            output.accept(Items.FLASH_MEMORY_CUSTOM.get());
            output.accept(Items.FLOPPY.get());
            output.accept(Items.FLOPPY_MODERN.get());
            output.accept(Items.REDSTONE_INTERFACE_CARD.get());
            output.accept(Items.NETWORK_INTERFACE_CARD.get());
            output.accept(Items.NETWORK_TUNNEL_CARD.get());
            output.accept(Items.FILE_IMPORT_EXPORT_CARD.get());
            output.accept(Items.SOUND_CARD.get());
            output.accept(Items.INVENTORY_OPERATIONS_MODULE.get());
            output.accept(Items.BLOCK_OPERATIONS_MODULE.get());
            output.accept(Items.NETWORK_TUNNEL_MODULE.get());
            output.accept(Items.TRANSISTOR.get());
            output.accept(Items.CIRCUIT_BOARD.get());
            output.accept(Items.NETWORK_SWITCH.get());
            output.accept(Items.VXLAN_HUB.get());
            output.accept(Items.PCI_CARD_CAGE.get());
        })
        .build()
    );
}
