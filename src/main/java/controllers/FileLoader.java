package controllers;

import models.HMM.HMM;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

class FileLoader {
    private Logger logger;
    private JFileChooser fileChooser;
    private File loadedFile;
    private String lastOpenedPath = ".";
    private HMM hmm;

    HMM getHMM() { return hmm; }

    File getLoadedFile(){
        return loadedFile;
    }

    FileLoader(Logger logger){
        this.logger = logger;
    }

    boolean open(String fileExtension, String something){
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(lastOpenedPath));
        int selected = fileChooser.showOpenDialog(null);
        if(selected == JFileChooser.OPEN_DIALOG ){
            if(fileChooser.getSelectedFile().toString().endsWith("." + fileExtension)){
                logger.addEvent("Selected " + something + " @ '" + fileChooser.getSelectedFile().toString() + "'");
                try {
                    lastOpenedPath = fileChooser.getSelectedFile().getParentFile().getCanonicalPath();
                } catch (IOException ex){
                    // ignore
                }
                loadedFile = fileChooser.getSelectedFile();
                if("hmm".equals(fileExtension)){
                    try{
                        ObjectInputStream input = new ObjectInputStream(new FileInputStream(loadedFile));
                        hmm = (HMM) input.readObject();
                        input.close();
                        logger.addEvent("Loaded HMM");
                    } catch (IOException | ClassNotFoundException ex){
                        // ignored
                        logger.addEvent("Loading HMM error: " + ex.toString());
                    }
                }
                return true;
            }
            logger.addEvent("Selected file is not appropriate. File should end with ." + fileExtension);
        }
        return false;
    }
}
