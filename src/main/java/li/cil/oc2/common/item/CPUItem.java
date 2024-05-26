package li.cil.oc2.common.item;

import li.cil.oc2.common.util.TextFormatUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CPUItem extends ModItem {

    private final int frequency;

    public CPUItem(int frequency)
    {
        this.frequency = frequency;
    }

    public int getFrequency()
    {
        return frequency;
    }

    public String getFrequencyString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(frequency/1_000_000);
        builder.append(" MHz");

        return builder.toString();
    }

    @Override
    public Component getName(final ItemStack stack) {
        return Component.literal("")
            .append(super.getName(stack))
            .append(" (")
            .append(getFrequencyString())
            .append(")");
    }
}
