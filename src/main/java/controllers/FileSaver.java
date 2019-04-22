package controllers;

import models.HMM.HMM;

import java.io.*;
import java.util.Date;

class FileSaver {
    private final String pathToDir = ".\\HMM";
    FileSaver(Logger logger, HMM hmm){
        // check if dir exists
        File file = new File(pathToDir);
        if(!file.exists()){
            try {
                if (file.mkdir()) {
                    logger.addEvent("Folder created @ '" + file.getCanonicalPath() + "'");
                }
            } catch (IOException ex){
                // ignored
                logger.addEvent("Creating folder error: " + ex.toString());
            }
        }

        file = new File(pathToDir + "\\HMM-" + new Date().getTime() +".hmm");

        // serialize HMM and write object
        try {
            if(file.createNewFile()) {
                ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
                output.writeObject(hmm);
                output.close();
                logger.addEvent("HMM is saved @ " + file.getCanonicalPath());
            }
        } catch (IOException ex) {
            // ignored
            logger.addEvent("Saving file error: " + ex.toString());
        }
    }
}
