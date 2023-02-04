package LiveSplitAddRun;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Content extends JPanel implements ActionListener {

    public static File lssFile;
    public static File txtFile;
    private static final JTextArea lssText = new JTextArea();
    private static final JTextArea txtText = new JTextArea();
    private static final JTextArea outputText = new JTextArea("Waiting...");


    public Content() {
        this.setFocusable(true);
        this.setSize(300, 300);
        this.setLayout(new GridLayout(3, 1));

        JButton lssButton = new JButton("Pick .lss file");
        lssButton.setActionCommand("lss");
        lssButton.addActionListener(this);
        this.add(lssButton);
        lssText.setLineWrap(true);
        this.add(lssText);

        JButton txtButton = new JButton("Pick times file");
        txtButton.setActionCommand("txt");
        txtButton.addActionListener(this);
        this.add(txtButton);
        txtText.setLineWrap(true);
        this.add(txtText);

        JButton execute = new JButton("Generate File");
        execute.setActionCommand("exe");
        execute.addActionListener(this);
        this.add(execute);
        outputText.setLineWrap(true);
        this.add(outputText);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("exe")) {
            try {
                outputText.setText(Main.convert(lssFile.getPath(), Main.getTimes(txtFile)));
            }
            catch (Exception ex) {
                outputText.setText("There was an error converting the files.\n" + ex.getMessage());
            }
            return;
        }

        boolean isSplits = e.getActionCommand().equals("lss");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(isSplits ? new LssOnly() : new TxtOnly());

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            if (isSplits) lssFile = chooser.getSelectedFile();
            else txtFile = chooser.getSelectedFile();
        }
        updateTexts();
    }

    private static void updateTexts() {
        if (lssFile != null) lssText.setText(lssFile.getName());
        if (txtFile != null) txtText.setText(txtFile.getName());
    }

    private static class LssOnly extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".lss");
        }

        @Override
        public String getDescription() {
            return ".lss files";
        }
    }

    private static class TxtOnly extends FileFilter {

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return ".txt files";
        }
    }

}
