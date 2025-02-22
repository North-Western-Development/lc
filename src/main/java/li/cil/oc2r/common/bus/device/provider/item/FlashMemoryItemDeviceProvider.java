/* SPDX-License-Identifier: MIT */

package li.cil.oc2r.common.bus.device.provider.item;

import li.cil.oc2r.api.bus.device.ItemDevice;
import li.cil.oc2r.api.bus.device.provider.ItemDeviceQuery;
import li.cil.oc2r.common.bus.device.provider.util.AbstractItemDeviceProvider;
import li.cil.oc2r.common.bus.device.vm.item.ByteBufferFlashStorageDevice;
import li.cil.oc2r.common.item.FlashMemoryItem;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public final class FlashMemoryItemDeviceProvider extends AbstractItemDeviceProvider {
    public FlashMemoryItemDeviceProvider() {
        super(FlashMemoryItem.class);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected Optional<ItemDevice> getItemDevice(final ItemDeviceQuery query) {
        final ItemStack stack = query.getItemStack();
        final FlashMemoryItem item = (FlashMemoryItem) stack.getItem();

        final int capacity = Math.max(item.getCapacity(stack), 0);
        return Optional.of(new ByteBufferFlashStorageDevice(stack, capacity));
    }
}
