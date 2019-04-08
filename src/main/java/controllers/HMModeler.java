package controllers;

import models.HMM.HMM;
import models.HMM.StateModel;
import models.HMM.TransitionModel;
import models.SequenceModel;

import java.util.ArrayList;
import java.util.Collections;

class HMModeler {
    private int numOfSequences;
    private ArrayList<StateModel> states;
    private ArrayList<TransitionModel> transitions;

    public int getStatesSize(){return states.size();}

    private void processSequence(SequenceModel sequenceModel){
        char[] sequence = sequenceModel.getSequence();
        for(int i = 0; i < sequence.length; i ++) {
            StateModel state = states.get(i);
            synchronized (state){
                state.addEmissionCount(sequence[i]);
            }
            TransitionModel transition = transitions.get(i);
            synchronized(transition) {
                if (i + 1 < sequence.length) { // next state is not Stop
                    switch(sequence[i+1]){ // next state
                        case '-': // is deletion
                            if(i == 0){  // prev state is Start
                                transition.addSDCount();
                                break;
                            }

                            break;
                        default: // is perhaps match or insert
                            if(i == 0){ // prev state is Start
                                if(state.isMatchState()) {
                                    transition.addSMCount();
                                } else {
                                    transition.addSICount();
                                }
                                break;
                            }

                    }
                } else { // next state is Stop

                }
            }
            if(state.endOfState()){ // last emission is added
                state.produceEmissionProbability(); // calculate probability for each emission
                // produce transition probabilities
                //transition.setProbabilites();
            }
        }
    }

    String train(ArrayList<SequenceModel> sequenceModels){
        numOfSequences = sequenceModels.size();
        // initialize list of states of length of an aligned sequence with the same StateModel's initialization
        states = new ArrayList<>(
                Collections.nCopies(
                    sequenceModels.get(0).getSequenceLength(),
                    new StateModel(numOfSequences)
                ));
        // initialize list of transition of length of an aligned sequence with the same TransitionModel's initialization
        transitions = new ArrayList<>(
                Collections.nCopies(
                        sequenceModels.get(0).getSequenceLength(),
                        new TransitionModel()
                ));
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
        return null; // finished train
    }

}
