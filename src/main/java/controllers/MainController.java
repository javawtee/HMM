package controllers;

import models.HMM.HMM;
import views.MainView;
import views.RawHMMView;

import java.io.File;
import java.io.IOException;

public class MainController {
    private MainView mainView;
    private Logger logger;
    private FileLoader fileLoader;
    private HMModeler HMModeler;
    private HMM currentHMM = null;


    public MainController(){
        // initialize view
        this.mainView = new MainView();

        // initialize Logger
        logger = new Logger(mainView.loggerScrollPane, mainView.loggerView);

        // initialize FileLoader
        fileLoader = new FileLoader(logger);

        // setup view's actions
        setButtonListener();


        logger.addEvent("Program started");
    }

    private void setButtonListener(){
        mainView.loadHMMBtn.addActionListener(e -> {
            if(fileLoader.open("hmm", "preset HMM")){
                try{
                    mainView.setHmmName(fileLoader.getLoadedFile().getCanonicalPath());
                    currentHMM = fileLoader.getHMM();
                    mainView.viewTrainedHMMBtn.setEnabled(false);
                    mainView.saveHMMBtn.setEnabled(false);
                } catch (IOException ex){
                    // ignored
                }

            }
            buttonToggle();
        });

        mainView.unloadHMMBtn.addActionListener(e -> {
            logger.addEvent("Unloaded preset HMM");
            mainView.setHmmName("No HMM found");
            buttonToggle();
        });

        mainView.trainHMMBtn.addActionListener(e -> {
            if(fileLoader.open("fa", "training data set")){
                FaReader faReader = new FaReader(fileLoader.getLoadedFile(), false);
                logger.addEvent("Reading selected file");
                if(faReader.getSequenceModels() != null){
                    logger.addEvent(
                            "Finished reading\n" +
                            " - Number of sequences: " + faReader.getSequenceModels().size() + "\n" +
                            " - Sequence length: " + faReader.getSequenceModels().get(0).getSequenceLength()
                    );
                    // initialize HMModeler
                    HMModeler = new HMModeler();
                    String result = HMModeler.train(faReader.getSequenceModels());
                    currentHMM = HMModeler.getTrainedHMM();
                    if(result == null){
                        mainView.viewTrainedHMMBtn.setEnabled(true);
                        mainView.saveHMMBtn.setEnabled(true);
                        mainView.testHMMBtn.setEnabled(true);
                        logger.addEvent("Finished training. To see result, click View (next to 'Train a pHMM' button)");
                    } else {
                        logger.addEvent(result);
                    }
                    return;
                }
                logger.addEvent("Error reading file: Found different length. Sequences are not aligned");
            }
        });

        mainView.testHMMBtn.addActionListener(e-> {
            if(fileLoader.open("fa", "testing data set")) {
                FaReader faReader = new FaReader(fileLoader.getLoadedFile(), true);
                logger.addEvent("Reading selected file");
                if(faReader.getSequenceModels() != null) {
                    logger.addEvent(
                            "Finished reading\n" +
                            " - Number of sequences: " + faReader.getSequenceModels().size()
                    );
                    logger.addEvent("Waiting for results...");
                    HMMScorer scorer = new HMMScorer(currentHMM, faReader.getSequenceModels());
                    double[] maxMin = scorer.getMaxMin();
                    mainView.setTestResults("Max = " + maxMin[0] + " ; min = " + maxMin[1]);
                    logger.addEvent("Check the results (max, min) above, next to 'Test' button");
                    for(int i = 0; i < faReader.getSequenceModels().size(); i++) {
                        logger.addEvent("Most likely path for sequence #" + i);
                        String path = "";
                        for (String state : scorer.getViterbiPath(i)) {
                            path = path.concat(state + " -> ");
                        }
                        logger.addEvent(path.concat("E"));
                    }
                }
            }
        });

        mainView.viewHMMBtn.addActionListener(e -> viewAction());
        mainView.viewTrainedHMMBtn.addActionListener(e -> viewAction());

        mainView.saveHMMBtn.addActionListener(e -> saveAction());
    }

    private void viewAction(){
        new RawHMMView(currentHMM);
    }

    private void saveAction(){
        new FileSaver(logger, currentHMM);
    }

    private void buttonToggle(){
        boolean loadedPresetHMM = !mainView.getHmmName().equals("No HMM found");
        //load field
        mainView.viewHMMBtn.setEnabled(loadedPresetHMM);
        mainView.unloadHMMBtn.setEnabled(loadedPresetHMM);

        //train field
        mainView.trainHMMBtn.setEnabled(!loadedPresetHMM);

        //test field
        mainView.testHMMBtn.setEnabled(loadedPresetHMM);
    }
}
