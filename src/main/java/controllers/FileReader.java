package controllers;

import models.SequenceModel;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class FaReader {
    private File faFile;
    private ArrayList<SequenceModel> sequenceModels;
    FaReader(File faFile){
        this.faFile = faFile;
        sequenceModels = new ArrayList<>();
        sequenceModels = prepareSequenceModels();
    }

    ArrayList<SequenceModel> getSequenceModels(){ return sequenceModels; }

    private ArrayList<SequenceModel> prepareSequenceModels(){
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(faFile));
            String sequenceName = "";
            String sequence = "";
            int sequenceLength = 0;
            for(String line; (line = reader.readLine()) != null;){
                if(line.startsWith(">")) {
                    if(!("".equals(sequence))){
                        // has input sequenceName and sequence
                        // check sequence length, if it is the first sequence, save it to compare
                        if(sequenceLength == 0){
                            sequenceLength = sequence.length();
                        }
                        // check length of the next n-th sequence, if not equal => unaligned sequences
                        if(sequence.length() != sequenceLength){
                            JOptionPane.showMessageDialog(null, "Found different length. Sequences are not aligned");
                            return null;
                        }
                        // add to ArrayList of SequenceModel
                        SequenceModel sequenceModel = new SequenceModel(sequenceName, sequence);
                        sequenceModels.add(sequenceModel);
                    }
                    sequenceName = line.substring(1, line.length());
                    sequence = "";
                } else {
                    sequence = sequence.concat(line);
                }
            }
            // prevent skipping the last sequence, inputs are ready
            SequenceModel sequenceModel = new SequenceModel(sequenceName, sequence);
            sequenceModels.add(sequenceModel);
            return sequenceModels;
        } catch (IOException ex){
            // ignore
        }
        return null;
    }
}
