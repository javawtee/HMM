package models.HMM.state.definition;

import models.HMM.state.probabilities.Correction;
import models.HMM.state.probabilities.Emission;
import models.HMM.state.probabilities.MDTransition;

import java.io.Serializable;
import java.util.HashMap;

public class InsertState extends Emission implements IStateDefinition, Serializable {
    private int index;
    private HashMap<Integer, String> positionForNextTransitions;
    private HashMap<String, Double> transitionProbabilities = new HashMap<>();
    private HashMap<Integer, Integer> combinedMap;
    private boolean isUniform;
    private int emissionCountInCombinedColumn = 0; // used in Start state

    public int getEmissionCountInCombinedColumn() {
        return emissionCountInCombinedColumn;
    }

    public InsertState(int index,
                       boolean isUniform,
                       HashMap<Character, Double> emissionCounts,
                       HashMap<Integer,
                       String> positionForTransitions){
        super(emissionCounts);
        this.index = index;
        this.isUniform = isUniform;
        if(positionForTransitions != null) {
            combinedMap = new HashMap<>();
            // start combining, is not combining
            updateCombinedMap(positionForTransitions, false);
        }
    }

    private void updateCombinedMap(HashMap<Integer, String> positionForTransitions, boolean isCombining){
//                Mi Ii        Ii(combined)  Ii(combined) Mi+1
//                A  -         -             -            A
//                A  -         A (M->I)      A (ignored)  -
//                A  A (M->I)  A (ignored)   -            A
//                B  -         -             A (M->I)     A
//                -  A (Di->I) -             A (ignored)  -
        for(Integer position: positionForTransitions.keySet()){
            if(!isCombining) {
                combinedMap.put(position, positionForTransitions.get(position).startsWith("M") ? 0 : -1); // 0 for *->M, -1 for *->D
            } else {
                if(positionForTransitions.get(position).startsWith("M")) {
                    int lastCount = combinedMap.get(position);
                    // update to new count
                    combinedMap.put(position, lastCount + 1);
                }
            }
        }
        // store positionForNextTransitions to estimate probabilities probabilities after combination finishes
        this.positionForNextTransitions = positionForTransitions;
    }

    public void mergeEmissionCounts(HashMap<Character, Double> emissionCountMap){
        for(Character emission: emissionCountMap.keySet()) {
            double newCount = emissionProbabilities.containsKey(emission)
                    ? emissionProbabilities.get(emission) + emissionCountMap.get(emission)
                    : emissionCountMap.get(emission);
            emissionProbabilities.put(emission, newCount);
        }
    }

    public void updateTransitionCounts(HashMap<Integer, String> positionForNextTransitions){
        /*
            position    Ii        Ii(combining)  Ii(combining,unknown) Ii(combining,unknown)
            0           -         -              -                     -
            1           -         A (M->I)       A (ignored)           A (ignored)
            2           A (M->I)  A (ignored)    -                     -
            3           -         -              A (M->I)              A (ignored)
            4           A (Di->I) -              A (ignored)           -
         */
         /* updateCombinedMap // ignored deletions while combining insert states
                combined (collapsed) => combined => combined
                -                       -           -
                A                       A(1)        A(2)
                A(1)                    A(1)        A(1)
                -                       A           A(1)
                A                       A(1)        A(1)
          */
           updateCombinedMap(positionForNextTransitions, true);
    }

    public void generateTransitionProbabilities(MDTransition lastMDTransitions){
        double MI = 0.0;
        double DI = 0.0;
        emissionCountInCombinedColumn = 0;
        double IM = 0.0; // Ii->Mi+1
        double II = 0.0; // Ii->Ii
        double ID = 0.0; // Ii->Di+1
        double[] fromI = new double[3];

        // if it is uniform insert state, override all calculations
        if(isUniform){
            fromI[0] = (double) 1/3;
            fromI[1] = (double) 1/3;
            fromI[2] = (double) 1/3;
        } else {
            HashMap<Integer, String> positionForLastMDStateTransitions = null;
            if(lastMDTransitions != null) {
                positionForLastMDStateTransitions = lastMDTransitions.getPositionForTransitions();
            }
            // gathering data
            for(Integer position: combinedMap.keySet()){
                int count = combinedMap.get(position);
                //System.out.println("*** " + position + ": " + count);
                if(count > II){ // finding max
                    II = combinedMap.get(position);
                }
                if(count > -1) {
                    emissionCountInCombinedColumn++;
                    if(combinedMap.get(position) > -1 && positionForNextTransitions.get(position).endsWith("M")){
                        IM++;
                        // ignore probabilities Mi->Mi+1
                        positionForNextTransitions.put(position, "");
                    }
                    if(combinedMap.get(position) > -1 &&
                            positionForLastMDStateTransitions != null &&
                            positionForLastMDStateTransitions.get(position).startsWith("M")){
                        MI++;
                    }
                } else {
                    if(combinedMap.get(position) > -1 && positionForNextTransitions.get(position).endsWith("D")){
                        ID++;
                        // ignore probabilities Di->Di+1
                        positionForNextTransitions.put(position, "");
                    }
                    if(combinedMap.get(position) > -1 &&
                            positionForLastMDStateTransitions != null &&
                            positionForLastMDStateTransitions.get(position).startsWith("D")){
                        DI++;
                    }
                }
            }

            if(lastMDTransitions != null) {
                updateLastMatchStateTransitions(lastMDTransitions, MI, DI);
            }

            fromI = Correction.correctTransitionProbabilities(IM, II, ID);
        }

        transitionProbabilities.put("IM" + (index+1), fromI[0]);
        transitionProbabilities.put("II" + index, fromI[1]);
        transitionProbabilities.put("ID" + (index+1), fromI[2]);
    }

    public void generateTransitionProbabilitiesToStop(MDTransition lastMatchStateTransitions){
        double MI = 0.0;
        double DI = 0.0;
        emissionCountInCombinedColumn = 0;
        double IE = 0.0; // Ii->Mi+1
        double II = 0.0; // Ii->Ii
        double[] fromI = new double[2];

        // if it is uniform insert state, override all calculations
        if(isUniform){
            fromI[0] = (double) 1/2;
            fromI[1] = (double) 1/2;
        } else {
            HashMap<Integer, String> positionForLastMDStateTransitions = null;
            if(lastMatchStateTransitions != null) {
                positionForLastMDStateTransitions = lastMatchStateTransitions.getPositionForTransitions();
            }
            // gathering data
            for(Integer position: combinedMap.keySet()){
                int count = combinedMap.get(position);
                //System.out.println("*** " + position + ": " + count);
                if(count > II){ // finding max
                    II = combinedMap.get(position);
                }
                if(count > -1) {
                    emissionCountInCombinedColumn++;
                    if(combinedMap.get(position) > -1 && positionForNextTransitions.get(position).endsWith("E")){
                        IE++;
                    }
                    if(combinedMap.get(position) > -1 &&
                            positionForLastMDStateTransitions != null &&
                            positionForLastMDStateTransitions.get(position).startsWith("M")){
                        MI++;
                    }
                } else {
                    if(combinedMap.get(position) > -1 &&
                            positionForLastMDStateTransitions != null &&
                            positionForLastMDStateTransitions.get(position).startsWith("D")){
                        DI++;
                    }
                }
            }

            if(lastMatchStateTransitions != null) {
               updateLastMatchStateTransitions(lastMatchStateTransitions, MI, DI);
            }

            fromI = Correction.correctTransitionProbabilitiesToStop(IE, II);
        }

        transitionProbabilities = new HashMap<>();
        transitionProbabilities.put("IE", fromI[0]);
        transitionProbabilities.put("II" + index, fromI[1]);
    }

    private void updateLastMatchStateTransitions(MDTransition lastMatchStateTransitions, double MI, double DI){
        // update probabilities from previous match and delete state to insert state
        lastMatchStateTransitions.updateTransitionCountDirectly("MI" + index, MI);
        lastMatchStateTransitions.updateTransitionCountDirectly("DI" + index, DI);
        // prepare data for generating probabilities probabilities for previous match and delete state
        lastMatchStateTransitions.setPositionForNextDestinations(positionForNextTransitions);
        // re-estimate last match and delete probabilities probabilities after all combining insert columns
        lastMatchStateTransitions.reEstimateTransitionProbabilities();
    }

    @Override
    public String getStateName() {
        return "I" + index;
    }

    @Override
    public HashMap<Character, Double> getEmissionProbabilities() {
        return emissionProbabilities;
    }

    @Override
    public HashMap<String, Double> getTransitionProbabilities(){
        return transitionProbabilities;
    }
}
