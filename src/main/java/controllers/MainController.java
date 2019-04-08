package controllers;

import views.MainView;

public class MainController {
    private MainView mainView;
    private Logger logger;
    private FileLoader fileLoader;
    private HMModeler HMModeler;


    public MainController(){
        // initialize view
        this.mainView = new MainView();

        // initialize Logger
        logger = new Logger(mainView.loggerScrollPane, mainView.loggerView);

        // initialize FileLoader
        fileLoader = new FileLoader(logger);

        // initialize HMModeler
        HMModeler = new HMModeler();

        // setup view's actions
        setButtonListener();


        logger.addEvent("Program started");
    }

    private void setButtonListener(){
        mainView.loadHMMBtn.addActionListener(e -> {
            if(fileLoader.open("something", "preset HMM")){
                mainView.setHmmName("loaded");

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
                FaReader faReader = new FaReader(fileLoader.getLoadedFile());
                logger.addEvent("Reading selected file");
                if(faReader.getSequenceModels() != null){
                    logger.addEvent(
                            "Finished reading\n" +
                            " - Number of sequences: " + faReader.getSequenceModels().size() + "\n" +
                            " - Sequence length: " + faReader.getSequenceModels().get(0).getSequenceLength()
                    );
                    String result = HMModeler.train(faReader.getSequenceModels());
                    if(result == null){
                        logger.addEvent("Finished training. To see result, click View (next to 'Train a pHMM' button)");
                    } else {
                        logger.addEvent(result);
                    }
                    return;
                }
                logger.addEvent("Error reading file: Found different length. Sequences are not aligned");
            }
        });
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
