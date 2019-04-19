package models.HMM.state.definition;

import models.HMM.state.probabilities.Emission;

import java.util.HashMap;

public class MatchState extends Emission implements IStateDefinition {
    int index;

    public MatchState(int index, HashMap<Character, Double> emissionCounts, int numOfSequences){
        super(emissionCounts, numOfSequences);
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
