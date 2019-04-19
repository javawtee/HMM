package controllers;

import models.HMM.HMM;
import models.SequenceModel;

import java.util.ArrayList;

public class HMMScorer {
    private HMM hmm;
    private ArrayList<SequenceModel> sequenceModels;

    public HMMScorer(HMM hmm, ArrayList<SequenceModel> sequenceModels){
        this.hmm = hmm;
        this.sequenceModels = sequenceModels;
    }

    
}
