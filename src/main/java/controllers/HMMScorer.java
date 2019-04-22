package controllers;

import models.HMM.HMM;
import models.HMM.state.definition.IStateDefinition;
import models.HMM.state.probabilities.DLink;
import models.HMM.state.probabilities.MDTransition;
import models.SequenceModel;

import java.util.ArrayList;
import java.util.LinkedList;

class HMMScorer {
    private ArrayList<IStateDefinition> states;
    private ArrayList<MDTransition> mdTransitions;
    private DLink dLink;
    private final ArrayList<Viterbi> viterbis = new ArrayList<>();

    double[] getMaxMin(){
        double max = viterbis.get(0).getBestHit(), min = viterbis.get(0).getBestHit();
        for(int i = 1; i < viterbis.size(); i++){
            Viterbi viterbi = viterbis.get(i);
            if(viterbi.getBestHit() >= max){
                max = viterbi.getBestHit();
            }
            if(viterbi.getBestHit() <= min){
                min = viterbi.getBestHit();
            }
        }
        return new double[]{max, min};
    }

    LinkedList<String> getViterbiPath(int sequencePostion) {return viterbis.get(sequencePostion).getViterbiPath();}

    HMMScorer(HMM hmm, ArrayList<SequenceModel> testSequenceModels){
        // number of sequences in testing set
        int numOfSequences = testSequenceModels.size();
        // prepare data from trained profile HMM
        states = hmm.getStates();
        mdTransitions = hmm.getMdTransitions();
        dLink = hmm.getdLink();

        // initialize viterbis
        for(int i = 0; i < numOfSequences; i++){
            viterbis.add(new Viterbi());
        }

        Thread[] sequenceThread = new Thread[numOfSequences];
        int threadID = 0;
        for(SequenceModel sequenceModel: testSequenceModels){
            final int sequencePosition = threadID; // index for viterbis
            sequenceThread[threadID] = new Thread(() -> score(sequencePosition, sequenceModel));
            sequenceThread[threadID].start();
            threadID++;
        }

        for(threadID = 0; threadID < numOfSequences; threadID++){
            try{
                sequenceThread[threadID].join();
            } catch (InterruptedException ex){
                // ignored
                //return ex.toString();
            }
        }
    }

    private void score(int sequencePosition, SequenceModel sequenceModel){
        int numOfStates = states.size();
        int sequenceLength = sequenceModel.getSequenceLength();
        VScore[][] vScores = new VScore[numOfStates][sequenceLength];
        Viterbi viterbi = new Viterbi();
        // initialize scores
        // first row has all 0, S has no emission P(E|S) = 0
        for(int i = 0; i < sequenceLength; i++){
            vScores[0][i] = new VScore(0.0, -1,"");
        }
        // ------------------
        // estimate start->X state for first observation
        // scores @ col=1
        double viterbiBestColumnHit = 0.0;
        for(int row = 1; row < numOfStates-1; row++){
            char observation = sequenceModel.getSequence()[0];
            // assumed previous cell score is 1, doesn't affect estimations
            // fromStateIndex = 0 => start state
            vScores[row][0] = new VScore(vCell(1, 0, row, observation), 0,"S");
            if(vScores[row][0].getScore() > viterbiBestColumnHit){
                viterbiBestColumnHit = vScores[row][0].getScore();
            }
        }

        int viterbiLastStateIndex = 1; // determine state index in the last column, used for trace back
        for(int col = 1; col < sequenceLength; col++){
            char observation = sequenceModel.getSequence()[col];
            int row = 1; // ignore row for Start state because S has no emission, then P(E|S) = 0
            double transitionToStopProbability = 0.0;
            int viterbiStateIndex = 1;
            viterbiBestColumnHit = 0.0;
            while(row < numOfStates -1) { // -1 => Stop state has no probability
                double max = 0.0;
                for (int stateCounter = 1; stateCounter < numOfStates -1; stateCounter++) { //-1 => Stop state has no probability
                    double vCellScore = vCell(vScores[stateCounter][col-1].getScore(), stateCounter, row, observation);
                    if(transitionToStopProbability == 0.0 && col == sequenceLength-1){
                        // estimate X->Stop/End
                        // scores @ col=n-1
                        transitionToStopProbability = getTransitionProbability(row, numOfStates-1);
                        vCellScore = vCellScore * transitionToStopProbability;
                    }
                    if (vCellScore >= max) {
                        max = vCellScore;
                        viterbiStateIndex = stateCounter;
                    }
                }
                // finished scoring a cell
                vScores[row][col] = new VScore(max, viterbiStateIndex, states.get(viterbiStateIndex).getStateName());
                //System.out.println("--- score @ [" + row + "][" + col + "] = " + max );
                if(max >= viterbiBestColumnHit){
                    viterbiBestColumnHit = max;
                    viterbiLastStateIndex = row;
                }
                row++;
                transitionToStopProbability = 0.0;
            }
            // finished scoring a column
            viterbi.setBestHit(viterbiBestColumnHit);
        }

        viterbi = traceBack(viterbi, vScores, viterbiLastStateIndex, sequenceLength-1);

        synchronized (viterbis) {
            viterbis.remove(sequencePosition);
            viterbis.add(sequencePosition, viterbi);
        }
    }

    // estimate probability for each emission per state
    private double vCell(double previousCellScore, int fromStateIndex, int toStateIndex, char emission){
        double transitionProbability = getTransitionProbability(fromStateIndex, toStateIndex);
        double emissionProbability = getEmissionProbability(toStateIndex, emission);
//        System.out.println("*** from: " + states.get(fromStateIndex).getStateName() +
//                " -- to: " + states.get(toStateIndex).getStateName() +
//                " -- tp: " + transitionProbability +
//                " -- previous score: " + previousCellScore +
//                " -- emission: " + emissionProbability);
        //System.out.println("** " + (Math.log(previousCellScore) + Math.log(transitionProbability) + Math.log(emissionProbability)));
        return previousCellScore * transitionProbability * emissionProbability; // estimate without log-odds
        //return Math.log(previousCellScore) + Math.log(transitionProbability) + Math.log(emissionProbability);
    }

    private double getTransitionProbability(int fromStateIndex, int toStateIndex){
        // charAt(0) to remove index from state name
        String fromStateName = Character.toString(states.get(fromStateIndex).getStateName().charAt(0));
        String toStateName = states.get(toStateIndex).getStateName();
        // fromStateIndex and toStateIndex are used to reference to array of states
        // convert to actual state indices to call name from mappings and mdStateIndex as reference to mdTransition
        int actualFromStateIndex = 0;
        int actualToStateIndex = 0;
        try {
            actualFromStateIndex = Integer.parseInt(states.get(fromStateIndex).getStateName().substring(1));
        } catch (NumberFormatException ex){
            // start state will get exception
            // ignore since actualFromStateIndex of Start = 0
        }
        try{
            actualToStateIndex = Integer.parseInt(toStateName.substring(1));
        } catch (NumberFormatException ex){
            // stop state will get exception
            if(toStateName.startsWith("E")){
                // -2 for start and I0 states
                // /2 to get number of match state
                // stop state index = (n+1)
                actualToStateIndex = ((toStateIndex -2) /2) + 1;
            }
        }

        if(actualFromStateIndex >= actualToStateIndex){
            if(actualFromStateIndex == actualToStateIndex && toStateName.startsWith("I")){
                if(!"M".equals(fromStateName)) {
                    // IiIi, loop
                    // S0I0
                    return states.get(fromStateIndex).getTransitionProbabilities().get(fromStateName + toStateName);
                } else {
                    // MiIi
                    return mdTransitions.get(actualFromStateIndex -1)
                            .getTransitionProbabilities()
                            .get("M" + toStateName);
                }
            }
            // Mi->Mi or Mi->Mi-1 or Ii->Ii-1, not possible
            return 0.0;
        }
        // X: M or I, i: actualFromStateIndex
        if(actualFromStateIndex == actualToStateIndex - 1
                && (toStateName.startsWith("M") || toStateName.startsWith("E"))){
            if("M".equals(fromStateName)) {
                if(toStateName.startsWith("E")){
                    return mdTransitions.get(actualFromStateIndex -1)
                            .getTransitionProbabilities()
                            .get("ME");
                }
                // Mi->Xi+1
                // i = actualFromStateIndex -1 (reference to array)
                return mdTransitions.get(actualFromStateIndex -1)
                        .getTransitionProbabilities()
                        .get("MM" + (actualFromStateIndex+1));
            } else { // from: S or I || to: E
                if(toStateName.startsWith("E")){
                    return states.get(fromStateIndex)
                            .getTransitionProbabilities()
                            .get("IE");
                }
                // S0->M1
                // Ii->Mi+1 goes here
                // Ii->Ii+1 => Ii -> Di+1 -> Ii+1 can't go in here
                return states.get(fromStateIndex)
                        .getTransitionProbabilities()
                        .get(fromStateName + "M" + (actualFromStateIndex + 1));
            }
        }
        // Xi->Xn, n: actualToStateIndex
        // get XDi+1
        // DD = P(Di+1 -> Dn-1)
        double XD, DD, DX;
        if(!fromStateName.startsWith("M")) {
            // S0->D1 or Ii->Di+1
            XD = states.get(fromStateIndex)
                    .getTransitionProbabilities()
                    .get(fromStateName + "D" + (actualFromStateIndex + 1));
        } else {
            // i = actualFromStateIndex -1 (reference to array)
            //System.out.println("********* " + fromStateName + actualFromStateIndex);
            XD = mdTransitions.get(actualFromStateIndex -1)
                    .getTransitionProbabilities()
                    .get(fromStateName + "D" + (actualFromStateIndex + 1));
        }
        // Mi -> Di+1 --- Dn-1 -> Mn or In-1 -> Dn -> In, n is a reference to array
        int n;
        if(toStateName.startsWith("I")){
            // Ii->In => Ii -> Di+1 -> Dn -> In
            // Di+1-> Dn
            DD = dLink.getALink(actualFromStateIndex+1, actualToStateIndex);
            // actual index of previous M = actualToStateIndex of I -1 (for referencing to array)
            n = actualToStateIndex - 1;
        } else {
            // Mi->Mn => Mi -> Di+1 -> Dn-1 -> Mn
            DD = dLink.getALink(actualFromStateIndex+1, actualToStateIndex-1);
            // actual index of previous M = actualToStateIndex of I -1 (for referencing to array) -1 (mdStateIndex starts at 1)
            n = actualToStateIndex - 2;
        }

        DX = mdTransitions.get(n).getTransitionProbabilities().get("D" + toStateName);
        // estimate probability
        return XD * DD * DX;
    }

    private double getEmissionProbability(int stateIndex, char emission){
        return states.get(stateIndex).getEmissionProbabilities().containsKey(emission) ?
                states.get(stateIndex).getEmissionProbabilities().get(emission) :
                states.get(stateIndex).getEmissionProbabilities().get('*');
    }

    private Viterbi traceBack(Viterbi viterbi, VScore[][] vScores, int lastStateIndex, int colIndex){
        if(lastStateIndex == 0){
            return viterbi;
        }
        int previousStateIndex = vScores[lastStateIndex][colIndex].getFromStateIndex();
        viterbi.addStateToPath(states.get(previousStateIndex).getStateName());
        return traceBack(viterbi, vScores, previousStateIndex, colIndex-1);
    }

    private class Viterbi {
        private LinkedList<String> viterbiPath = new LinkedList<>();
        private double bestHit;

        void setBestHit(double bestHit) {
            this.bestHit = bestHit;
        }
        double getBestHit(){ return bestHit;}

        void addStateToPath(String stateName){ viterbiPath.addFirst(stateName); }
        LinkedList<String> getViterbiPath(){return viterbiPath;}
    }

    private class VScore {
        private double score;
        private int fromStateIndex;
        private String fromStateName;
        
        VScore(double score, int fromStateIndex, String fromStateName){
            this.score = score;
            this.fromStateIndex = fromStateIndex;
            this.fromStateName = fromStateName;
        }

        double getScore(){return score;}
        int getFromStateIndex(){return fromStateIndex;}
    }
}
