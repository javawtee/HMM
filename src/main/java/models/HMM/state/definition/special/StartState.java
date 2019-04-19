package models.HMM.state.definition.special;

import models.HMM.state.probabilities.Correction;

import java.util.HashMap;

public class StartState extends AbstractSpecialState {
    public StartState(HashMap<Integer, String> positionForTransitions){
        super(positionForTransitions);
    }

    @Override
    public void generateTransitionProbabilities(double passingI) {
        // Start: SMi, SIi-1, SDi
        double SM = 0.0, SD = 0.0;
        for(Integer position: positionForTransitions.keySet()){
            String transition = positionForTransitions.get(position);
            if (transition.startsWith("M")) { // e.g: S->M* or ME
                SM++;
            }
            if (transition.startsWith("D")) { // e.g: S->D* or DE
                SD++;
            }
        }
        // determine if Io is uniform then S->Io = 0
        /*
                S         final combined insert state
                          -
                Matched   A(2)
                M         A(1)
                M         A(1)
                M         A(1)
          */
        double SI = passingI;

        // estimate and correct probabilities probabilities
        double[] returned = Correction.correctTransitionProbabilities(SM, SI, SD);

        transitionProbabilities.put("SM", returned[0]);
        transitionProbabilities.put("SI", returned[1]);
        transitionProbabilities.put("SD", returned[2]);
    }

    @Override
    public String getStateName() {
        return "S";
    }
}
