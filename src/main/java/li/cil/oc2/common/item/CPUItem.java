package li.cil.oc2.common.item;

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
}
