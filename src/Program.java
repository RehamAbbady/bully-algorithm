import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Program {
    public static void main(String[] args) throws InterruptedException, IOException {

        Process process= new Process(ProcessHandle.current().pid());
        TimeUnit.SECONDS.sleep(-1);

    }
}
