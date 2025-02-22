/* SPDX-License-Identifier: MIT */

package li.cil.oc2r.common.bus.device.provider.item;

import li.cil.oc2r.api.bus.device.ItemDevice;
import li.cil.oc2r.api.bus.device.provider.ItemDeviceQuery;
import li.cil.oc2r.common.Config;
import li.cil.oc2r.common.bus.device.provider.util.AbstractItemDeviceProvider;
import li.cil.oc2r.common.bus.device.rpc.item.InventoryOperationsModuleDevice;
import li.cil.oc2r.common.capabilities.Capabilities;
import li.cil.oc2r.common.item.Items;

import java.util.Optional;

public final class InventoryOperationsModuleDeviceProvider extends AbstractItemDeviceProvider {
    public InventoryOperationsModuleDeviceProvider() {
        super(Items.INVENTORY_OPERATIONS_MODULE);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected Optional<ItemDevice> getItemDevice(final ItemDeviceQuery query) {
        return query.getContainerEntity().flatMap(entity ->
            entity.getCapability(Capabilities.robot()).map(robot ->
                new InventoryOperationsModuleDevice(query.getItemStack(), entity, robot)));
    }

    @Override
    protected int getItemDeviceEnergyConsumption(final ItemDeviceQuery query) {
        return Config.inventoryOperationsModuleEnergyPerTick;
    }
}
