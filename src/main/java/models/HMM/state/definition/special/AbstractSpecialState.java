package models.HMM.state.definition.special;

import models.HMM.state.definition.IStateDefinition;

import java.io.Serializable;
import java.util.HashMap;

public abstract class AbstractSpecialState implements IStateDefinition, Serializable {
    private HashMap<Character, Double> emissionProbabilities = new HashMap<>();
    HashMap<Integer, String> positionForTransitions;
    HashMap<String, Double> transitionProbabilities = new HashMap<>();

    public AbstractSpecialState(HashMap<Integer, String> positionForTransitions){
        this.positionForTransitions = positionForTransitions;
        // Special state has no emission
        emissionProbabilities.put('*', 0.0);
    }

    public abstract void generateTransitionProbabilities(double passingI);

    @Override
    public abstract String getStateName();

    @Override
    public HashMap<Character, Double> getEmissionProbabilities() {
        return emissionProbabilities;
    }

    @Override
    public HashMap<String, Double> getTransitionProbabilities() {
        return transitionProbabilities;
    }
}
