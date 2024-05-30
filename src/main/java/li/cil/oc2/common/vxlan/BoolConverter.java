package li.cil.oc2.common.vxlan;

public class BoolConverter {
    public static byte BooleanToByte(boolean b) {
        return (byte) (b ? 1 : 0);
    }

    public static boolean ByteToBoolean(byte b) {
        return (b == 1);
    }
}
