package models.HMM.state.probabilities;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Emission implements Serializable {
    private final double e; // used for pseudo-probability
    private final int numOfAminoAcid; // number of known amino acid in the world
    protected HashMap<Character, Double> emissionProbabilities = new HashMap<>();

    public Emission(HashMap<Character, Double> emissionCounts){
        e = 0.1;
        numOfAminoAcid = 20;
        if(emissionCounts != null) { // case null: insert state is uniform, do nothing
            this.emissionProbabilities.putAll(emissionCounts);
        }
    }

    // estimate and correct emission probabilities for a column
    public void generateEmissionProbabilities(){
        int numOfTotalCount = 0;
        for(Character emission: emissionProbabilities.keySet()){
            if(emission != '-') { // ignore all gap counts
                numOfTotalCount += emissionProbabilities.get(emission);
            }
        }
        double probabilityForAbsentEmission = 0.0; // represents probability for each absent amino acid
        emissionProbabilities.putIfAbsent('-', 0.0); // used to determine numOfPresentEmissions
        // number of present amino acids
        int numOfPresentEmissions = emissionProbabilities.keySet().size() -1; // -1 for '-'
        // calculate number of absent amino acids, since no amino acid is more likely to be emitted than another
        int numOfAbsentEmissions = numOfAminoAcid - numOfPresentEmissions;
        if(numOfAbsentEmissions > 0){
            // number of absences = number of amino acid, then it is calculated for uniform insert state
            probabilityForAbsentEmission = numOfAbsentEmissions == numOfAminoAcid ?
                    (double) 1/numOfAminoAcid :
                    e/numOfAbsentEmissions;
        }
        for(Character emission: emissionProbabilities.keySet()){
            if(emission != '-') {
                double count = emissionProbabilities.get(emission);
                emissionProbabilities.put(
                        emission,
                        (count/numOfTotalCount) - (e/numOfPresentEmissions)
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
