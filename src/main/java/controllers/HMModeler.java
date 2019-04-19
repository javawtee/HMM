package controllers;

import models.HMM.HMM;
import models.HMM.state.*;
import models.HMM.state.definition.InsertState;
import models.HMM.state.definition.MatchState;
import models.HMM.state.definition.special.AbstractSpecialState;
import models.HMM.state.definition.special.StartState;
import models.HMM.state.definition.special.StopState;
import models.HMM.state.probabilities.DLink;
import models.HMM.state.probabilities.MDTransition;
import models.SequenceModel;

import java.util.ArrayList;
import java.util.Objects;

class HMModeler {
    private ArrayList<StateModel> alignedColumns;
    private int mdStateIndex = 1, // an index of match and delete, this is also an index for transitions
                insertStateIndex = 0; // start at between Start state and Match state 1
    private ArrayList<MDTransition> mdTransitions = new ArrayList<>(); // hold transitions for Match and Delete states
    private InsertState combiningInsertState;
    private HMM hmm = new HMM();
    private DLink dLink = new DLink(); // store delete-delete transition
    private int sequencePosition = 0;

    public HMM getTrainedHMM(){return hmm;}

    private void processSequence(SequenceModel sequenceModel){
        int sequencePosition;
        char[] sequence = sequenceModel.getSequence();
        synchronized ((Object)this.sequencePosition){
            sequencePosition = this.sequencePosition;
            this.sequencePosition++;
        }
        for(int i = 0; i < sequence.length; i ++) {
            StateModel stateModel;
            synchronized (alignedColumns.get(i)) {
                stateModel = alignedColumns.get(i);
                stateModel.addEmissionCount(sequence[i]);
                String from = sequence[i] == '-' ? "D" : "M";
                String to;
                if (i + 1 < sequence.length) {
                    to = sequence[i + 1] == '-' ? "D" : "M";
                } else { // next state is stop
                    stateModel.markNextIsStop();
                    to = "E";
                }
                stateModel.addTransitionCount(sequencePosition, from.concat(to));
            }

            // only last thread (notified by endOfColumn()) uses these resources
            // process all data for the i-th column
            if (stateModel.endOfColumn()) {
                // check if next state is stop, finalize combining insert columns
                if (stateModel.isMatchState() || stateModel.isNextStop()) {
                    MatchState matchState = null;
                    //---- TRANSITIONS FOR MATCH AND DELETE STATES----
                    // if last column is not a match state or next state is stop => is finalizing combining insert columns
                    if(stateModel.isMatchState() || !stateModel.isNextStop()) {
                    // Don't add probabilities probabilities to match state because we need to estimate "jump" probabilities (delete transitions)
                        MDTransition mdTransition = new MDTransition(mdStateIndex);
                        mdTransition.setCurrentPositionForTransitions(stateModel.getPositionForTransitions());
                        mdTransitions.add(mdTransition);

                    //---- MATCH STATE ----
                        matchState = new MatchState(mdStateIndex, stateModel.getEmissionCounts(), stateModel.getNumOfSequences());
                        matchState.generateEmissionProbabilities();
                    }

                    //---- INSERT STATE ----
                    // If no insert state awaits for combination, set uniform Insert state
                        InsertState insertState;
                        insertState = Objects.requireNonNullElseGet(combiningInsertState, () -> new InsertState(
                            insertStateIndex,
                            true,
                            null,
                            stateModel.getNumOfSequences(),
                            null));

                        insertState.generateEmissionProbabilities();
                        combiningInsertState = null;
                        insertStateIndex++;

                    //---- DELETE STATE ----
                    // it is omitted, and we only consider probabilities probabilities of it
                    // transitions of delete state is estimated in MDTransition

                    // *************************************************************************************************
                    // Estimate probabilities probabilities
                        // -1: previous probabilities, -1: mdStateIndex starts at 1
                        MDTransition previousTransition = mdStateIndex > 1 ? mdTransitions.get(mdStateIndex - 2) : null;
                        // generate probabilities probabilities for I(insertStateIndex)
                        // update and generate probabilities probabilities for M(@ mdStateIndex) and D(@ mdStateIndex)
                        insertState.generateTransitionProbabilities(previousTransition); // mdStateIndex starts at 1
                        // not generating regular probabilities probabilities for last match and delete states
                        if(!stateModel.isNextStop()) {
                            // generate probabilities probabilities for M and D (@ mdStateIndex)
                            // -1: mdStateIndex starts at 1
                            mdTransitions.get(mdStateIndex - 1).generateTransitionProbabilities();
                            // since starting DLink at delete state index of 1, ignore start state
                            double DToNextDProbability = mdTransitions.get(mdStateIndex -1).getDToNextDProbability();
                            dLink.addDLink(mdStateIndex +1, DToNextDProbability);
                        }
                        // ERROR: passing NO probabilities probabilities here
                        // matchState.setTransitionProbabilities(previousTransition.getTransitionProbabilities());
                        //-----------------------------------------
                    //---- START STATE ----
                        if (mdStateIndex == 1) {
                            AbstractSpecialState startState = new StartState(stateModel.getPositionForTransitions());
                            // if insert state detected at first -> every emission in insert state column will be S->I
                            startState.generateTransitionProbabilities(insertState.getEmissionCountInCombinedColumn());
                            // add start state to hmm
                            hmm.addState(startState);
                        }
                    // *************************************************************************************************

                    // add states to hmm (ignored delete state because it have no emission, use DLink instead)
                    if(stateModel.isMatchState()) {
                        // add insert state
                        hmm.addState(insertState);
                        // add match state
                        hmm.addState(matchState);
                    }

                    //---- STOP STATE ----
                    if(stateModel.isNextStop()){
                        MDTransition lastMDTransition;
                        InsertState insertStateBeforeStop;
                        if(stateModel.isMatchState()){ // M->I(uniform)->E
                            insertStateBeforeStop = new InsertState(insertStateIndex,
                                    true,
                                    null,
                                    stateModel.getNumOfSequences(),
                                    null);
                            insertStateBeforeStop.generateEmissionProbabilities();
                            insertStateBeforeStop.generateTransitionProbabilitiesToStop(null);
                            lastMDTransition = mdTransitions.get(mdStateIndex - 1);
                        } else { // last column is insert state, combiningInsertState != null
                            insertStateBeforeStop = insertState; // insertState = combiningInsertState, emission probabilities are estimated
                            insertStateBeforeStop.generateTransitionProbabilitiesToStop(previousTransition);
                            lastMDTransition = mdTransitions.get(mdStateIndex - 2);
                            // remove DLink to the last delete state (mistakenly knew next state was not stop)
                            dLink.removeLastDLink();
                        }
                        lastMDTransition.generateTransitionProbabilitiesToStop();

                        AbstractSpecialState stopState = new StopState();

                        // add insert state
                        hmm.addState(insertStateBeforeStop);
                        // add stop state
                        hmm.addState(stopState);

                        // ---- any clean-ups go here
                        // remove DLink to the first delete state (index of 1), it is omitted to be starting of the link
                        dLink.removeFirstDLink();
                    }

                    // update index for next
                    mdStateIndex++;
                } else {
                    if (combiningInsertState == null) { // insert state happens after match state
                    /*
                        Mi Ii        Ii(combined)  Ii(combined) Mi+1
                        A  -         -             -            A
                        A  -         A (M->I)      A (ignored)  -
                        A  A (M->I)  A (ignored)   -            A
                        B  -         -             A (M->I)     A
                        -  A (Di->I) -             A (ignored)  -
                        =============================================
                        MiMi+1 = 3/8   IiMi+1 = 1/4    DiMi+1 = 1/5
                        MiIi = 4/8     IiIi = 1/4      DiIi = 2/5
                        MiDi+1 = 1/8   IiDi+1= 2/4     DiDi+1 = 2/5
                     */
                        combiningInsertState = new InsertState(
                                insertStateIndex,
                                false,
                                stateModel.getEmissionCounts(),
                                stateModel.getNumOfSequences(),
                                stateModel.getPositionForTransitions()
                        );

                    } else { // combining insert state
                        combiningInsertState.mergeEmissionCounts(stateModel.getEmissionCounts());
                        combiningInsertState.updateTransitionCounts(stateModel.getPositionForTransitions());
                    }
                }
            }
        }
    }

    String train(ArrayList<SequenceModel> sequenceModels){
        int numOfSequences = sequenceModels.size();
        // stretching sequence into aligned columns
        alignedColumns = new ArrayList<>();
        for(int i = 0; i < sequenceModels.get(0).getSequenceLength() + 2; i++){ // +1 for Start, +1 for Stop
            alignedColumns.add(new StateModel(numOfSequences));
        }
        Thread[] sequenceThread = new Thread[numOfSequences];
        int threadID = 0;
        for(SequenceModel sequenceModel: sequenceModels){
            sequenceThread[threadID] = new Thread(() -> processSequence(sequenceModel));
            sequenceThread[threadID].start();
            threadID++;
        }

        for(threadID = 0; threadID < numOfSequences; threadID++){
            try{
                sequenceThread[threadID].join();
            } catch (InterruptedException ex){
                return ex.toString();
            }
        }

        // finalize data for profile HMM
        // set Training set (input data in aligned columns)
        hmm.setSequenceModels(sequenceModels);
        // set array of transition probabilities for match and delete to next state
        hmm.setMdTransition(mdTransitions);
        // set map of transition probabilities from delete state @ index of 1 to other delete states
        hmm.setdLink(dLink);

        System.out.println("HMM size: " + hmm.getHMMSize());
        for(int i = 0; i < hmm.getHMMSize(); i++){
            String stateName = hmm.getState(i).getStateName();
            System.out.println("--- " + stateName);
            if(stateName.startsWith("M")) {
//                for(Character emission : hmm.getState(i).getEmissionProbabilities().keySet()){
//                    System.out.println(emission + ": " + hmm.getState(i).getEmissionProbabilities().get(emission));
//                }
                int index = Integer.parseInt(stateName.substring(1, stateName.length()));
                for (String probabilities : mdTransitions.get(index - 1).getTransitionProbabilities().keySet()) {
                    System.out.println("--- " + probabilities + ": " + mdTransitions.get(index - 1).getTransitionProbabilities().get(probabilities));
                }
            } else {
                for(String probabilities: hmm.getState(i).getTransitionProbabilities().keySet()){
                    System.out.println("--- " + probabilities + ": " + hmm.getState(i).getTransitionProbabilities().get(probabilities)) ;
                }
            }
        }

        return null; // finished train
    }

}
