import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Process extends Thread {
    private static long processId;
    private static int checkAliveInterval=1000;
    private static int checkAliveTimeout=1500;

    private long coordinatorId;
    long lastCheckAliveFromCoordinator;
    private static final Logger LOGGER = Logger.getLogger( Process.class.getName() );

    public Process(long proccessId) throws IOException, InterruptedException {
        this.processId=proccessId;
        coordinatorId=0;
        lastCheckAliveFromCoordinator=0;
        Thread t = new Thread();
        t.start();
        this.checkAlive();
        this.listen();
    }

    private void checkAlive() throws IOException, InterruptedException {

        while(true){
            TimeUnit.SECONDS.sleep(1);

            long diff = Instant.now().toEpochMilli()-lastCheckAliveFromCoordinator;
            if(coordinatorId==0){
                this.electSelf();
            }
            else{
                if(coordinatorId==processId){
                    if(diff>checkAliveInterval){
                        lastCheckAliveFromCoordinator=Instant.now().toEpochMilli();
                        this.broadcastElectedNode();
                    }
                }
                if(diff>checkAliveTimeout){
                    LOGGER.log( Level.WARNING,String.format("TIMEOUT!!!!!!"));

                    coordinatorId=0;
                    lastCheckAliveFromCoordinator=Instant.now().toEpochMilli();
                    this.startElection();
                    this.electSelf();
                }
            }
            String msg=UDPConnection.receive();
            if(msg!=null)
                dataReceived(msg);
        }

    }
    private void listen() throws SocketTimeoutException {

        String s= UDPConnection.receive();

    }
    private void broadcastElectedNode() throws IOException {
        String msg=String.format("ELECTED %s",processId) ;
        UDPConnection.send(msg);
        LOGGER.log( Level.INFO,String.format("Process %s is the coordinator now",processId) );
    }
    private void electSelf() throws IOException {
        String electMsg=String.format("ELECT_SELF %s",processId) ;
        UDPConnection.send(electMsg);
        LOGGER.log( Level.INFO,String.format("Process %s elected itself",processId) );
    }
    private void startElection() throws IOException {
        String startMsg=String.format("START_ELECTION %s",processId) ;
        UDPConnection.send(startMsg);
        LOGGER.log( Level.INFO,String.format("Process %s started elections",processId) );
    }
    private void dataReceived(String msg) throws IOException {
        String msgArr[]= msg.split(" ");
        String msgType=msgArr[0];
        long msgProcess=Long.parseLong(msgArr[1]);
        if(msgType=="ELECT_SELF"){
            if(msgProcess>coordinatorId){
                LOGGER.log( Level.INFO,String.format("Coordinator changed from %s to %s",coordinatorId,msgProcess) );

                lastCheckAliveFromCoordinator=Instant.now().toEpochMilli();
                coordinatorId=msgProcess;

            }
        }
        else if(msgType=="ELECTED"){
            lastCheckAliveFromCoordinator=Instant.now().toEpochMilli();
            if(msgProcess!=processId){
                LOGGER.log( Level.INFO,String.format("Coordinator %s is alive",coordinatorId) );

            }
            if(msgProcess>coordinatorId){
                LOGGER.log( Level.INFO,String.format("Coordinator changed from %s to %s",coordinatorId,msgProcess) );

                lastCheckAliveFromCoordinator=Instant.now().toEpochMilli();
                coordinatorId=msgProcess;

            }
            if(msgProcess<processId){
                this.electSelf();
            }

        } else if(msgType=="START_ELECTION"){
            LOGGER.log( Level.INFO,String.format("RESTART ELECTION") );
            coordinatorId=0;
            lastCheckAliveFromCoordinator=Instant.now().toEpochMilli();
            electSelf();
        }

    }

}
