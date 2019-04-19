package models.HMM.state.probabilities;

public class Correction {
    // correction if there is any zero probability
    // using "add-one" rule
    public static double[] correctTransitionProbabilities(double first, double second, double third){
        double total = first + second + third;
        if(first == 0 || second == 0 || third == 0){
            first = (first + 1) / (total + 3);
            second = (second + 1) / (total + 3);
            third = (third + 1) / (total + 3);
        } else {
            first = first / total;
            second = second / total;
            third = third / total;
        }
        return new double[]{first, second, third};
    }

    public static double[] correctTransitionProbabilitiesToStop(double first, double second){
        double total = first + second;
        if(first == 0 || second == 0){
            first = (first + 1) / (total + 2);
            second = (second + 1) / (total + 2);
        } else {
            first = first / total;
            second = second / total;
        }
        return new double[]{first, second};
    }
}
