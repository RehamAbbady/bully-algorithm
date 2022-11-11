import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class EventListener {
    public interface UDPConnectionListener {
        public String onReceive(String msg);
    }

}
class Initiater {
    private List<EventListener.UDPConnectionListener> listeners = new ArrayList<EventListener.UDPConnectionListener>();

    public void addListener(EventListener.UDPConnectionListener toAdd) {
        listeners.add(toAdd);
    }
    public  void send(String msg) throws IOException {
        byte[] buffer = msg.getBytes(Charset.forName("UTF-8"));
        InetAddress address = InetAddress.getLocalHost();
        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, address, 26840
        );
        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.send(packet);
        for (EventListener.UDPConnectionListener hl : listeners)
            hl.onReceive( msg);
    }

}

class Responder implements EventListener.UDPConnectionListener {
    @Override
    public String onReceive(String msg) {
        try {
            DatagramSocket serverSocket = new DatagramSocket(26840);
            byte[] receiveData = new byte[8];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.setSoTimeout(1);

            serverSocket.receive(receivePacket);
            msg = new String( receivePacket.getData(), 0,
                    receivePacket.getLength()
            );
            if(!msg.isEmpty()&&!msg.isBlank()&&msg!=null)
            {
                System.out.println("RECEIVED: " + msg);
                return msg;
            }
            else
            {
                return null;
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

    }
}
