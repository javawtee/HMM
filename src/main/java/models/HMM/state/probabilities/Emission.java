package models.HMM.state.probabilities;

import java.util.HashMap;

public abstract class Emission {
    private final int numOfAminoAcid; // number of known amino acid in the world
    protected HashMap<Character, Double> emissionProbabilities = new HashMap<>();
    private int numOfSequences;

    public Emission(HashMap<Character, Double> emissionCounts, int numOfSequences){
        numOfAminoAcid = 20;
        if(emissionCounts != null) { // case null: insert state is uniform, do nothing
            this.emissionProbabilities.putAll(emissionCounts);
        }
        this.numOfSequences = numOfSequences;
    }

    // estimate and correct emission probabilities for a column
    public void generateEmissionProbabilities(){
        double probabilityForAbsentEmission = 0.0; // represents probability for each absent amino acid
        emissionProbabilities.putIfAbsent('-', 0.0); // prevent exception: no gap appears in this state
        // number of present amino acids
        int numOfPresentEmissions = emissionProbabilities.keySet().size() - 1; // -1 for '-'
        // calculate number of absent amino acids, since no amino acid is more likely to be emitted than another
        int numOfAbsentEmissions = numOfAminoAcid - numOfPresentEmissions;
        // minus count of gaps because in-del is not considered as emission
        // added number of absent amino acid for pseudo-count use
        double newNumOfSequences = numOfSequences - emissionProbabilities.get('-') + numOfAbsentEmissions;
        // using pseudo-count to set positive probability for the absent amino acids
        // can't decide good value of e for pseudo-probabilities
        if(numOfAbsentEmissions > 0){ // if there is any absence
            probabilityForAbsentEmission = (double) 1/newNumOfSequences;
        }
        for(Character emission: emissionProbabilities.keySet()){
            //System.out.println("- emission " + emission + ": " + emissionProbabilities.get(emission));
            if(emission != '-') {
                emissionProbabilities.put(
                        emission,
                        emissionProbabilities.get(emission) / newNumOfSequences
                );
            }
        }
        // don't need '-'
        emissionProbabilities.remove('-');
        // '*' represent for absent amino acids
        emissionProbabilities.put('*', probabilityForAbsentEmission);
    }

    public HashMap<Character, Double> getEmissionProbabilities() {
        return emissionProbabilities;
    }
}
