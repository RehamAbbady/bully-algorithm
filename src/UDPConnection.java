import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;

public class UDPConnection {

    public static void send(String msg) throws IOException {

        byte[] buffer = msg.getBytes(Charset.forName("UTF-8"));
        InetAddress address = InetAddress.getLocalHost();
        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, address, 26840
        );
        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.send(packet);
    }

    public synchronized static String  receive() throws SocketTimeoutException {
        Thread t = new Thread("tReceive");
        t.start();
        try {
            DatagramSocket serverSocket = new DatagramSocket(26840);
            byte[] receiveData = new byte[8];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.setSoTimeout(1);

            serverSocket.receive(receivePacket);
            String msg = new String( receivePacket.getData(), 0,
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
        finally {
            t.stop();
        }
    }

}
