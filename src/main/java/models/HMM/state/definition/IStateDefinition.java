package models.HMM.state.definition;

import java.util.HashMap;

public interface IStateDefinition {
    String getStateName();
    HashMap<Character, Double> getEmissionProbabilities();
    HashMap<String, Double> getTransitionProbabilities();
}
