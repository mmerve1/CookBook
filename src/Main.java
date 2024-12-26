import java.awt.event.WindowEvent;
import javax.swing.*;
import java.awt.event.WindowAdapter;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {


            // Starting first window
            LoginWindow login = new LoginWindow(true);

            login.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent we) {
                }
            });
        });
    }
}
