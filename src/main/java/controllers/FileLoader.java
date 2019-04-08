package controllers;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

class FileLoader {
    private Logger logger;
    private JFileChooser fileChooser;
    private File loadedFile;
    private String lastOpenedPath = ".";

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
                return true;
            }
            logger.addEvent("Selected file is not appropriate. File should end with ." + fileExtension);
        }
        return false;
    }
}
