package models.HMM.state.definition;

import models.HMM.state.probabilities.Emission;

import java.io.Serializable;
import java.util.HashMap;

public class MatchState extends Emission implements IStateDefinition, Serializable {
    int index;

    public MatchState(int index, HashMap<Character, Double> emissionCounts){
        super(emissionCounts);
        this.index = index;
    }

    @Override
    public String getStateName() {
        return "M" + index;
    }

    @Override
    public HashMap<String, Double> getTransitionProbabilities() {
        return null;
    }
}
