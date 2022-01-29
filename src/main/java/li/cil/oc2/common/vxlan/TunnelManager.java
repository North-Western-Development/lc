package li.cil.oc2.common.vxlan;

import li.cil.oc2.api.capabilities.NetworkInterface;
import li.cil.oc2.common.Config;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class TunnelManager {

    private final HashMap<Integer, NetworkInterface> tunnels = new HashMap<>();
    private final DatagramSocket socket;
    private static TunnelManager INSTANCE;

    public TunnelManager(InetAddress bindHost, short bindPort, InetAddress remoteHost, short remotePort) throws SocketException {
        socket = new DatagramSocket(bindPort, bindHost);
        socket.connect(remoteHost, remotePort);
    }

    public static void initialize() {
        if (Config.enable) {
            try {
                INSTANCE = new TunnelManager(
                    InetAddress.getByName(Config.bindHost), Config.bindPort,
                    InetAddress.getByName(Config.remoteHost), Config.remotePort
                );
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    INSTANCE.listen();
                }
            }).start();
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
                int vni_1 = packet.getData()[4];

                if ((flags & 0x08) != 0x08) {
                    continue;
                }

                NetworkInterface iface = tunnels.get(vni);

                if (iface != null) {
                    byte[] inner = new byte[packet.getData().length - 8];
                    copyBytes(packet.getData(), inner, 8, 0, packet.getData().length - 8);

                    iface.writeEthernetFrame(null, inner, 255);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static TunnelManager instance() {
        return INSTANCE;
    }

    private void copyBytes(byte[] input, byte[] output, int inputOffset, int outputOffset, int length) {
        for (int i = 0; i < length; i++) {
            output[outputOffset + i] = input[inputOffset + i];
        }
    }
}
