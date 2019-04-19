package models.HMM;

import models.HMM.state.definition.IStateDefinition;
import models.HMM.state.probabilities.DLink;
import models.HMM.state.probabilities.MDTransition;
import models.SequenceModel;

import java.io.Serializable;
import java.util.ArrayList;

public class HMM implements Serializable {
    // ---- TRAINING SET
    private ArrayList<SequenceModel> sequenceModels;

    public ArrayList<SequenceModel> getSequenceModels() {
        return sequenceModels;
    }

    public void setSequenceModels(ArrayList<SequenceModel> sequenceModels) {
        this.sequenceModels = sequenceModels;
    }



    // ---- STATES
    private ArrayList<IStateDefinition> states = new ArrayList<>(); // consists of: Start, Stop, Insert and Match states

    public int getHMMSize(){ return states.size(); }
    public void addState(IStateDefinition definedState){
        states.add(definedState);
    }
    public IStateDefinition getState(int index){ return states.get(index); }


    // ---- TRANSITIONS
    private ArrayList<MDTransition> mdTransition;
    private DLink dLink;

    public ArrayList<MDTransition> getMdTransition() {
        return mdTransition;
    }

    public void setMdTransition(ArrayList<MDTransition> mdTransition) {
        this.mdTransition = mdTransition;
    }

    public DLink getdLink() {
        return dLink;
    }

    public void setdLink(DLink dLink) {
        this.dLink = dLink;
    }
}
