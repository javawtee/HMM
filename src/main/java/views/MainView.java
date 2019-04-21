package views;
import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {
    private JPanel mainContainer;
    public JButton loadHMMBtn;
    private JLabel hmmName;
    public JButton unloadHMMBtn;
    public JButton saveHMMBtn;
    public JButton viewHMMBtn;
    public JButton trainHMMBtn;
    public JButton viewTrainedHMMBtn;
    public JButton testHMMBtn;
    private JLabel testResults;
    public JTextArea loggerView;
    public JScrollPane loggerScrollPane;

    public void setTestResults(String text){
        testResults.setText(text);
        testResults.setToolTipText(text);
    }

    public void setHmmName(String text){
        hmmName.setText(text);
        hmmName.setToolTipText(text);
    }

    public String getHmmName(){ return hmmName.getText(); }

    public MainView(){
        this.setMinimumSize(new Dimension(600, 400));
        this.setPreferredSize(new Dimension(600, 400));
        this.setMaximumSize(new Dimension(600, 400));
        this.add(mainContainer);

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
}
