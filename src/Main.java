import com.fernandez.net.Client;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Client client = new Client();
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.run();
    }
}
