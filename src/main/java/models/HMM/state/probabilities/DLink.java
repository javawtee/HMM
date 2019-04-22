package models.HMM.state.probabilities;

import java.io.Serializable;
import java.util.HashMap;

public class DLink implements Serializable {
    // from delete state index of 1
    // HashMap explained: <toIndex, probability>>
    // how to calculate
    // D1->D2->D3->D4->D5
    // in-view of HashMap
    // -----------
    // 2    |  0.1
    // 3    |  0.1
    // ...
    // D5 = D1D2 * D2D3 * D3D4 * D4D5
    // get D3->D5 probability
    // D3->D5 = D5/D3; D3 = D1->D3 = D1D2 * D2D3
    private HashMap<Integer, Double> dLink = new HashMap<>();

    // i.e.: D5 -> lastIndex = 5
    public int lastIndex = 1;

    public HashMap<Integer, Double> getDLink(){return dLink;}

    public DLink(){
        // add a fake loop at delete state index of 1
        // so that next addDlink is able to refer (toIndex -1)
        // D1D1 = 1
        // D1D2 = D1D1 * D1D2 = 1 * D1D2
        dLink.put(1, 1.0);
    }

    // first DLink is S->D1
    public void updateFirstDLink(double probability){
        dLink.put(1, probability);
        if(dLink.containsKey(2)) {
            // update next dLink D1->D2 because D2 goes before D1 is updated
            double lastD2Probability = dLink.get(2);
            dLink.put(2, probability * lastD2Probability);
        }
    }

    public void addDLink(int toIndex, double probability){
        // estimate probability from D1->D(toIndex)
        // D1->D(toIndex) = D(toIndex -1) * D(toIndex)
        double lastToIndexProbability = dLink.get(toIndex-1);
        dLink.put(toIndex, lastToIndexProbability * probability);
        // store last index
        this.lastIndex = toIndex;
    }

    // D5 = D1D2 * D2D3 * D3D4 * D4D5
    // get D3->D5 probability
    // D3->D5 = D5/D3; D3 = D1->D3 = D1D2 * D2D3
    public double getALink(int fromIndex, int toIndex){
        // fromIndex == toIndex => 1
        // D2->D2 or known as M1->D2
        return fromIndex == toIndex ? 1 : (double) dLink.get(toIndex)/ dLink.get(fromIndex);
    }

    // remove DLink to the last delete state (mistakenly knew next state was not stop, but insert
    // <-> DD then generated to DE)
    public void removeLastDLink(){
        dLink.remove(lastIndex);
    }
}
