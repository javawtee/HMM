package models.HMM.state;

import java.util.HashMap;

public class StateModel {
    private int numOfSequences; // represents for number of input proteins
    private boolean nextIsStop = false;
    private int counter = 0; // to determine end of an aligned column read

    public void markNextIsStop(){
        nextIsStop = true;
    }

    public boolean isNextStop() {
        return nextIsStop;
    }

    public StateModel(int numOfSequences){
        this.numOfSequences = numOfSequences;
    }

    public int getNumOfSequences(){return numOfSequences;}

// EMISSIONS
    // Character: name of amino acid, Double: probability
    private HashMap<Character, Double> emissionCounts = new HashMap<>();

    public HashMap<Character, Double> getEmissionCounts(){return emissionCounts;}

    // return false if number of gaps ('-') is more than 50% of sequences
    public boolean isMatchState(){
        emissionCounts.putIfAbsent('-', 0.0);
        return emissionCounts.get('-') < (numOfSequences/2);
    }

    // for thread use, to determine if the last thread runs this resource (StateModel)
    public boolean endOfColumn(){
        return counter == numOfSequences;
    }

    // emission: amino acid or gap (to determine whether it is match state or insert state)
    public void addEmissionCount(char emission){
        emissionCounts.putIfAbsent(emission, 0.0);
        emissionCounts.put(
                emission,
                emissionCounts.get(emission) + 1
        );
        counter++;
    }

// TRANSITIONS
    private HashMap<Integer, String> positionForTransitions = new HashMap<>();

    public HashMap<Integer, String> getPositionForTransitions(){
        return positionForTransitions;
    }

    // Transition types: Mi->Mi+1, Mi->Di+1, Di->Mi+1, Di->Di+1
    // Mi->Ii = Di->Ii is calculated in DeleteState
    public void addTransitionCount(int position, String transitionType){
        // position (0,1,2):
        //  M
        //0 A
        //1 -
        //2 A
        positionForTransitions.put(position, transitionType);
    }
}