package models.HMM.state.probabilities;

import java.io.Serializable;
import java.util.HashMap;

public class MDTransition implements Serializable {
    private int mdStateIndex;
    private HashMap<Integer, String> positionForTransitions = new HashMap<>();
    private HashMap<Integer, String> positionForNextDestinations = new HashMap<>();
    private HashMap<String, Double> transitionProbabilities = new HashMap<>();

    public double getDToNextDProbability(){
        return transitionProbabilities.get("DD" + (mdStateIndex+1));
    }

    public HashMap<Integer, String> getPositionForTransitions(){
        return positionForTransitions;
    }

    public HashMap<String, Double> getTransitionProbabilities(){return transitionProbabilities;}

    public MDTransition(int mdStateIndex){
        this.mdStateIndex = mdStateIndex;
    }

    // prepare data for generating probabilities probabilities
    public void setPositionForNextDestinations(HashMap<Integer, String> positionForNextDestinations){
        this.positionForNextDestinations = positionForNextDestinations;
    }

    public void setCurrentPositionForTransitions(HashMap<Integer, String> positionForTransitions){
        // set initial probabilities probabilities = 0.0
        transitionProbabilities.put("MI" + mdStateIndex, 0.0);
        transitionProbabilities.put("DI" + mdStateIndex, 0.0);
        transitionProbabilities.put("MM" + (mdStateIndex+1), 0.0);
        transitionProbabilities.put("MD" + (mdStateIndex+1), 0.0);
        transitionProbabilities.put("DM" + (mdStateIndex+1), 0.0);
        transitionProbabilities.put("DD" + (mdStateIndex+1), 0.0);
        // save position for each emission and gap in aligned column (@mdStateIndex-1)
        this.positionForTransitions = positionForTransitions;
    }

    // directly: don't add up to last value, but overwrite the existing value
    public void updateTransitionCountDirectly(String key, double value){
        transitionProbabilities.put(key, value);
    }

    public void generateTransitionProbabilities(){
        double MM = 0.0, MD = 0.0, DM = 0.0, DD = 0.0; // MI and DI are estimated in Ii-1
        for(Integer position: positionForTransitions.keySet()){
            // M->M
            String transition = positionForTransitions.get(position);
            if(transition.startsWith("M") ){ // e.g: from MM/MD
                if(transition.endsWith("M")){ //e.g: to DM/MM
                    MM++;
                }
                if(transition.endsWith("D")) { // e.g: to DD/MD
                    MD++;
                }
            }
            if(transition.startsWith("D") ){ // e.g: from DM/DD
                if(transition.endsWith("M")){ //e.g: to DM/MM
                    DM++;
                }
                if(transition.endsWith("D")) { // e.g: to DD/MD
                    DD++;
                }
            }
        }

        // estimate and correct probabilities probabilities
        // for M->*
        double MI = transitionProbabilities.get("MI" + mdStateIndex);
        double[] fromM = Correction.correctTransitionProbabilities(MM, MI, MD);
        // for D->*
        double DI = transitionProbabilities.get("DI" + mdStateIndex);
        double[] fromD = Correction.correctTransitionProbabilities(DM, DI, DD);

        transitionProbabilities.put("MM" + (mdStateIndex+1), fromM[0]);
        transitionProbabilities.put("MI" + mdStateIndex, fromM[1]);
        transitionProbabilities.put("MD" + (mdStateIndex+1), fromM[2]);

        transitionProbabilities.put("DM" + (mdStateIndex+1), fromD[0]);
        transitionProbabilities.put("DI" + mdStateIndex, fromD[1]);
        transitionProbabilities.put("DD" + (mdStateIndex+1), fromD[2]);
    }

    // re-estimate last match and delete probabilities probabilities after all combining insert columns
    // MI and DI is updated (in insert state after combining finishes)
    // position for next destinations is set (in insert state after combining finishes)
    public void reEstimateTransitionProbabilities(){
        /*
            Mi Ii        Mi+1
            A  -         A   (Mi->Mi+1)
            A  -         -   (Mi->Di+1)
            A  A (M->I)  A   (no probabilities from Mi->Mi+1)
            B  -         A   (Mi->Mi+1)
            -  A (Di->I) -   (no probabilities from Di->Di+1)
            =============================================
            MiMi+1 = 3/5   IiMi+1 = 2/5    DiMi+1 = 1/5
            MiIi = 1/5     IiIi = 1/5      DiIi = 2/5
            MiDi+1 = 1/5   IiDi+1= 2/5     DiDi+1 = 2/5
         */
        double MM = 0.0, MD = 0.0, DM = 0.0, DD = 0.0; // MI and DI are estimated in Ii-1
        for(Integer position: positionForTransitions.keySet()) {
            // M->M
            String from = positionForTransitions.get(position);
            String to = positionForNextDestinations.get(position);
            if (from.startsWith("M")) { // e.g: from MM/MD
                if (to.endsWith("M")) { //e.g: to MM/MD
                    MM++;
                }
                if (to.endsWith("D")) { // e.g: to DD/DM
                    MD++;
                }
            }
            if (from.startsWith("D")) { // e.g: from MM/MD
                if (to.endsWith("M")) { //e.g: to MM/MD
                    DM++;
                }
                if (to.endsWith("D")) { // e.g: to DD/DM
                    DD++;
                }
            }
        }

        // estimate and correct probabilities probabilities
        // for M->*
        double MI = transitionProbabilities.get("MI" + mdStateIndex);
        double[] fromM = Correction.correctTransitionProbabilities(MM, MI, MD);
        // for D->*
        double DI = transitionProbabilities.get("DI" + mdStateIndex);
        double[] fromD = Correction.correctTransitionProbabilities(DM, DI, DD);

        transitionProbabilities.put("MM" + (mdStateIndex+1), fromM[0]);
        transitionProbabilities.put("MI" + mdStateIndex, fromM[1]);
        transitionProbabilities.put("MD" + (mdStateIndex+1), fromM[2]);

        transitionProbabilities.put("DM" + (mdStateIndex+1), fromD[0]);
        transitionProbabilities.put("DI" + mdStateIndex, fromD[1]);
        transitionProbabilities.put("DD" + (mdStateIndex+1), fromD[2]);
    }

    public void generateTransitionProbabilitiesToStop(){
        /*
            M->I (combined)->End

            M->End
         */
        double ME = 0.0, DE = 0.0; // MI and DI are estimated in Ii-1
        for(Integer position: positionForTransitions.keySet()) {
            // M->M
            String transition = positionForTransitions.get(position);
            if ("ME".equals(transition)) {
                ME++;
            }
            if ("DE".equals(transition)) {
                DE++;
            }
        }

        // estimate and correct probabilities probabilities
        // for M->*
        double MI = transitionProbabilities.get("MI" + mdStateIndex);
        double[] fromM = Correction.correctTransitionProbabilitiesToStop(ME, MI);
        // for D->*
        double DI = transitionProbabilities.get("DI" + mdStateIndex);
        double[] fromD = Correction.correctTransitionProbabilitiesToStop(DE, DI);

        transitionProbabilities = new HashMap<>();
        transitionProbabilities.put("ME", fromM[0]);
        transitionProbabilities.put("MI" + mdStateIndex, fromM[1]);
        transitionProbabilities.put("DE", fromD[0]);
        transitionProbabilities.put("DI" + mdStateIndex, fromD[1]);
    }
}
