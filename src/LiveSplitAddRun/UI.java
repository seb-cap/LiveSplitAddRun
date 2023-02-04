package LiveSplitAddRun;

import javax.swing.JFrame;
import java.awt.Dimension;

public class UI extends JFrame {

    public UI() {
        Content c = new Content();
        this.setPreferredSize(new Dimension(800, 400));
        this.setContentPane(c);
        pack();

        this.setTitle("Livesplit Run Adder");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

}
