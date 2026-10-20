package cajero;

import javax.swing.SwingUtilities;

public class Cajero {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CajeroAutomatico cajero = new CajeroAutomatico(); 
            cajero.setVisible(true); 
        });
    }
}