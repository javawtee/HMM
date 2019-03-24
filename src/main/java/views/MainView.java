package views;

import controllers.FileLoader;
import controllers.HMMController;
import controllers.Logger;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {
    private JPanel mainContainer;
    private JPanel functionalField;
    private JPanel hmmField;
    private JButton loadHMMBtn;
    private JLabel hmmName;
    private JButton unloadHMMBtn;
    private JButton saveHMMBtn;
    private JButton viewHMMBtn;
    private JPanel trainingField;
    private JButton trainHMMBtn;
    private JButton viewTrainedHMMBtn;
    private JPanel testField;
    private JButton testHMMBtn;
    private JLabel testResults;
    private JTextArea loggerView;
    private JScrollPane loggerScrollPane;

    private Logger logger;
    private HMMController hmmController;

    public MainView(){
        //data preparation
        logger = new Logger(loggerScrollPane, loggerView);
        setButtonsListener();

        //show view
        initView();

        logger.addEvent("Program started");
    }

    private void initView(){
        this.setMinimumSize(new Dimension(600, 400));
        this.setPreferredSize(new Dimension(600, 400));
        this.setMaximumSize(new Dimension(600, 400));
        this.add(mainContainer);

        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void setButtonsListener(){
        FileLoader fileLoader = new FileLoader(logger);
        loadHMMBtn.addActionListener(e -> {
            if(fileLoader.open("xml", "preset HMM")){
                hmmName.setText("loaded");
            }
            buttonToggler();
        });

        unloadHMMBtn.addActionListener(e -> {
            logger.addEvent("Unloaded preset HMM");
            hmmName.setText("No HMM found");
            buttonToggler();
        });

        trainHMMBtn.addActionListener(e -> {
            fileLoader.open("fa", "training data set");
        });
    }

    private void buttonToggler(){
        boolean loadedPresetHMM = !hmmName.getText().equals("No HMM found");
        //load field
        viewHMMBtn.setEnabled(loadedPresetHMM);
        unloadHMMBtn.setEnabled(loadedPresetHMM);

        //train field
        trainHMMBtn.setEnabled(!loadedPresetHMM);

        //test field
        testHMMBtn.setEnabled(loadedPresetHMM);
    }
}
