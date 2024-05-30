package li.cil.oc2.common.vxlan;

import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class GREHeader implements Serializable
{
    public static final int LENGTH = 21;

    public InetAddress FinalDestinationAddress;
    public InetAddress SourceAddress;
    public int FinalDestinationPort;
    public int SourcePort;
    public int Vti;
    public boolean IsArp;

    public GREHeader()
    {

    }

    public GREHeader(InetAddress fda, InetAddress sa, int finalDestinationPort, int sourcePort, int vti, boolean isArp)
    {
        FinalDestinationAddress = fda;
        SourceAddress = sa;
        FinalDestinationPort = finalDestinationPort;
        SourcePort = sourcePort;
        Vti = vti;
        IsArp = isArp;
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(FinalDestinationAddress.getAddress());
        baos.write(SourceAddress.getAddress());
        baos.write(ByteBuffer.allocate(4).putInt(FinalDestinationPort).array());
        baos.write(ByteBuffer.allocate(4).putInt(SourcePort).array());
        baos.write(ByteBuffer.allocate(4).putInt(Vti).array());
        baos.write(ByteBuffer.allocate(1).put(BoolConverter.BooleanToByte(IsArp)).array());
        return baos.toByteArray();
    }

    public static GREHeader deserialize(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return new GREHeader(InetAddress.getByAddress(bais.readNBytes(4)), InetAddress.getByAddress(bais.readNBytes(4)), ByteBuffer.wrap(bais.readNBytes(4)).getInt(), ByteBuffer.wrap(bais.readNBytes(4)).getInt(), ByteBuffer.wrap(bais.readNBytes(4)).getInt(), BoolConverter.ByteToBoolean(ByteBuffer.wrap(bais.readNBytes(1)).get(0)));
    }

    @Override
    public String toString()
    {
        return String.format("Destination IP: %s\nSource IP: %s\nDestination Port: %d\nSource Port: %d", FinalDestinationPort, SourceAddress, FinalDestinationPort, SourcePort);
    }
}

