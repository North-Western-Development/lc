package li.cil.oc2.common.vxlan;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ICMPParser {

    public static ICMPInfo parseICMP(byte[] frame) {
        if (frame.length < 34) { // Minimum length: Ethernet (14) + IP (20)
            throw new IllegalArgumentException("Frame is too short to be a valid ICMP packet");
        }

        // Ethernet frame: [Destination MAC (6)] [Source MAC (6)] [Type (2)]
        int etherType = ((frame[12] & 0xFF) << 8) | (frame[13] & 0xFF);
        if (etherType != 0x0800) { // Check if EtherType is IPv4
            throw new IllegalArgumentException("Not an IPv4 packet");
        }

        // Skip Ethernet header
        ByteBuffer buffer = ByteBuffer.wrap(frame, 14, frame.length - 14);

        // IP header: [Version/IHL (1)] [Type of Service (1)] [Total Length (2)] [ID (2)] [Flags/Offset (2)] [TTL (1)] [Protocol (1)] [Checksum (2)] [Source IP (4)] [Destination IP (4)]
        buffer.get(); // Version/IHL
        buffer.get(); // Type of Service
        buffer.getShort(); // Total Length
        buffer.getShort(); // ID
        buffer.getShort(); // Flags/Offset
        buffer.get(); // TTL
        byte protocol = buffer.get(); // Protocol
        if (protocol != 1) { // Check if protocol is ICMP
            throw new IllegalArgumentException("Not an ICMP packet");
        }

        buffer.getShort(); // Checksum
        byte[] sourceIP = new byte[4];
        buffer.get(sourceIP);
        byte[] destinationIP = new byte[4];
        buffer.get(destinationIP);

        // ICMP header: [Type (1)] [Code (1)] [Checksum (2)] [Rest of Header (4)]
        byte type = buffer.get();
        byte code = buffer.get();
        short checksum = buffer.getShort();
        int restOfHeader = buffer.getInt(); // This could include identifier and sequence number for echo requests/replies

        // The rest of the packet is the ICMP payload
        byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);

        return new ICMPInfo(type, code, checksum, restOfHeader, sourceIP, destinationIP, payload);
    }

    public static boolean isIcmpPacket(byte[] data) {
        if (data.length < 34) { // Minimum length: Ethernet (14) + IP (20)
            return false;
        }

        int etherType = ((data[12] & 0xFF) << 8) | (data[13] & 0xFF);
        if (etherType != 0x0800) { // Check if EtherType is IPv4
            return false;
        }

        int ipHeaderStart = 14;
        byte protocol = data[ipHeaderStart + 9];
        return protocol == 1; // Check if protocol is ICMP
    }

    public static class ICMPInfo {
        public final byte type;
        public final byte code;
        public final short checksum;
        public final int restOfHeader;
        public final byte[] sourceIP;
        public final byte[] destinationIP;
        public final byte[] payload;

        public ICMPInfo(byte type, byte code, short checksum, int restOfHeader, byte[] sourceIP, byte[] destinationIP, byte[] payload) {
            this.type = type;
            this.code = code;
            this.checksum = checksum;
            this.restOfHeader = restOfHeader;
            this.sourceIP = sourceIP;
            this.destinationIP = destinationIP;
            this.payload = payload;
        }

        @Override
        public String toString() {
            return "ICMPInfo{" +
                "type=" + (type & 0xFF) +
                ", code=" + (code & 0xFF) +
                ", checksum=" + (checksum & 0xFFFF) +
                ", restOfHeader=" + restOfHeader +
                ", sourceIP=" + Arrays.toString(sourceIP) +
                ", destinationIP=" + Arrays.toString(destinationIP) +
                ", payload=" + Arrays.toString(payload) +
                '}';
        }
    }
}
