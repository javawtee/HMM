package models.HMM;

import java.util.HashMap;

public class StateModel {
    private final int numOfAminoAcid = 20; // number of known amino acid in the world
    private double probabilityForAbsentEmission = 0; // represent probability for each absent amino acid
    private int numOfSequences;
    HashMap<Character, Double> emissionProbabilities = new HashMap<>();

    // return false if number of gaps ('-') is more than 50% of sequences
    public boolean isMatchState(){ return emissionProbabilities.get('-') < 1/2; }

    // for thread use, to determine if the last thread runs this resource (StateModel)
    public boolean endOfState(){
        return emissionProbabilities.size() == numOfSequences;
    }

    public double getProbabilityOfEmission(char emission){
        if(emissionProbabilities.get('-') > 1/2){
            return 1/numOfAminoAcid; // this is probability for any emission in insert state
        } else {
            return emissionProbabilities.get(emission) == null ?
                    emissionProbabilities.get('*') :
                    emissionProbabilities.get(emission);
        }
    }

    public StateModel(int numOfSequences){
        this.numOfSequences = numOfSequences;
    }

    public void addEmissionCount(char emission){ // emission = amino acid or gap
        emissionProbabilities.putIfAbsent(emission, 0.0); // initialize emission
        emissionProbabilities.put(
            emission,
            emissionProbabilities.get(emission) + 1.0 // add count for the emission
        );
    }

    public void produceEmissionProbability(){
        emissionProbabilities.putIfAbsent('-', 0.0); // prevent exception: no gap appears in this state
        // number of present amino acids
        int numOfPresentEmissions = emissionProbabilities.keySet().size() - 1; // -1 for '-'
        // calculate number of absent amino acid, since no amino acid is more likely to be emitted than another
        int numOfAbsentEmissions = numOfAminoAcid - numOfPresentEmissions;
        // minus count of gaps because in-del is not considered as emission
        // added number of absent amino acid for pseudo-count use
        double newNumOfSequences = numOfSequences - emissionProbabilities.get('-') + numOfAbsentEmissions;
        // using pseudo-count to set positive probability for the absent amino acids
        // can't decide good value of e for pseudo-probabilities
        if(numOfAbsentEmissions > 0){ // if there is any absence
            probabilityForAbsentEmission = 1/newNumOfSequences;
        }
        // '*' represent for absent amino acids
        emissionProbabilities.put('*', probabilityForAbsentEmission);
        for(Character emission: emissionProbabilities.keySet()){
                emissionProbabilities.put(
                        emission,
                        emissionProbabilities.get(emission) / (emission == '-' ? numOfSequences : newNumOfSequences)
                );
        }
    }
}
