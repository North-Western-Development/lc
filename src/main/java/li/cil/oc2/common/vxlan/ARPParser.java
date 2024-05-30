package li.cil.oc2.common.vxlan;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ARPParser {

    public static ARPInfo parseARP(byte[] frame) {
        if (frame.length < 28) {
            throw new IllegalArgumentException("Frame is too short to be a valid ARP packet");
        }

        ByteBuffer buffer = ByteBuffer.wrap(frame);

        buffer.getInt();
        buffer.getInt();
        buffer.getInt();
        buffer.getShort();

        int hardwareType = buffer.getShort();
        int protocolType = buffer.getShort();
        int hardwareAddressLength = buffer.get();
        int protocolAddressLength = buffer.get();
        int operation = buffer.getShort();

        byte[] senderHardwareAddress = new byte[hardwareAddressLength];
        byte[] senderProtocolAddress = new byte[protocolAddressLength];
        byte[] targetHardwareAddress = new byte[hardwareAddressLength];
        byte[] targetProtocolAddress = new byte[protocolAddressLength];

        buffer.get(senderHardwareAddress);
        buffer.get(senderProtocolAddress);
        buffer.get(targetHardwareAddress);
        buffer.get(targetProtocolAddress);

        return new ARPInfo(hardwareType, protocolType, operation,
            senderHardwareAddress, senderProtocolAddress,
            targetHardwareAddress, targetProtocolAddress);
    }

    public static boolean isArpPacket(byte[] data) {
        // Check if the frame is an ARP packet
        // Ethernet frame type for ARP is 0x0806
        int frameType = ((data[12] & 0xFF) << 8) | (data[13] & 0xFF);
        return frameType == 0x0806;
    }

    public static class ARPInfo {
        public final int hardwareType;
        public final int protocolType;
        public final int operation;
        public final byte[] senderHardwareAddress;
        public final byte[] senderProtocolAddress;
        public final byte[] targetHardwareAddress;
        public final byte[] targetProtocolAddress;

        public ARPInfo(int hardwareType, int protocolType, int operation,
                       byte[] senderHardwareAddress, byte[] senderProtocolAddress,
                       byte[] targetHardwareAddress, byte[] targetProtocolAddress) {
            this.hardwareType = hardwareType;
            this.protocolType = protocolType;
            this.operation = operation;
            this.senderHardwareAddress = senderHardwareAddress;
            this.senderProtocolAddress = senderProtocolAddress;
            this.targetHardwareAddress = targetHardwareAddress;
            this.targetProtocolAddress = targetProtocolAddress;
        }

        @Override
        public String toString() {
            return "ARPInfo{" +
                "hardwareType=" + hardwareType +
                ", protocolType=" + protocolType +
                ", operation=" + operation +
                ", senderHardwareAddress=" + Arrays.toString(senderHardwareAddress) +
                ", senderProtocolAddress=" + Arrays.toString(senderProtocolAddress) +
                ", targetHardwareAddress=" + Arrays.toString(targetHardwareAddress) +
                ", targetProtocolAddress=" + Arrays.toString(targetProtocolAddress) +
                '}';
        }
    }
}
