package li.cil.oc2.common.vxlan;

import li.cil.oc2.api.capabilities.NetworkInterface;
import li.cil.oc2.common.Config;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class TunnelManager {

    private final HashMap<Integer, TunnelInterface> tunnels = new HashMap<>();
    private final DatagramSocket socket;
    private static TunnelManager INSTANCE;
    private final InetAddress remoteHost;
    private final short remotePort;
    private final InetAddress bindHost;
    private final short bindPort;

    public TunnelManager(InetAddress bindHost, short bindPort, InetAddress remoteHost, short remotePort) throws SocketException {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.bindHost = bindHost;
        this.bindPort = bindPort;
        if (Config.enable) {
            socket = new DatagramSocket(bindPort, bindHost);
            socket.connect(remoteHost, remotePort);
        } else {
            socket = null;
        }
    }

    public static void initialize() {

        try {
            INSTANCE = new TunnelManager(
                InetAddress.getByName(Config.bindHost), (short) Config.bindPort,
                InetAddress.getByName(Config.remoteHost), (short) Config.remotePort
            );
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        if (Config.enable) {
            new Thread(() -> INSTANCE.listen()).start();
        }
    }

    public void listen() {
        byte[] buffer = new byte[65535];
        try {
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                if (packet.getData().length < 8) {
                    continue;
                }

                byte flags = packet.getData()[0];
                int vni = packet.getData()[6]
                    + packet.getData()[5] << 8
                    + packet.getData()[4] << 16;

                if ((flags & 0x08) != 0x08) {
                    continue;
                }

                TunnelInterface iface = tunnels.get(vni);

                if (iface != null) {
                    byte[] inner = new byte[packet.getData().length - 8];
                    System.arraycopy(packet.getData(), 8, inner, 0, packet.getData().length - 8);

                    iface.target.writeEthernetFrame(iface, inner, 255);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TunnelManager instance() {
        return INSTANCE;
    }

    public void sendToVti(int vti, byte[] payload) {
        if (socket != null) {
            byte[] buffer = new byte[payload.length + 8];

            System.arraycopy(payload, 0, buffer, 8, payload.length);

            buffer[0] = 0x08;
            buffer[4] = (byte) ((vti >> 16) & 0xff);
            buffer[5] = (byte) ((vti >> 8) & 0xff);
            buffer[6] = (byte) (vti & 0xff);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, this.remoteHost, this.remotePort);

            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public NetworkInterface registerVti(int vti, NetworkInterface iface) {
        TunnelInterface tuniface = new TunnelInterface(vti, iface);
        tunnels.put(vti, tuniface);
        return tuniface;
    }

    public void unregisterVti(int vti) {
        tunnels.remove(vti);
    }

    public class TunnelInterface implements NetworkInterface {
        final NetworkInterface target;
        private final int vti;

        public TunnelInterface(int vti, NetworkInterface iface) {
            this.vti = vti;
            this.target = iface;
        }

        @Override
        public byte[] readEthernetFrame() {
            return new byte[0];
        }

        @Override
        public void writeEthernetFrame(final NetworkInterface source, final byte[] frame, final int timeToLive) {
            TunnelManager.this.sendToVti(vti, frame);
        }
    }
}
