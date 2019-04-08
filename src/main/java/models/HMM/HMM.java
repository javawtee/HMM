package models.HMM;

import java.io.Serializable;
import java.util.ArrayList;

public class HMM implements Serializable {
    private ArrayList<String> states;

    public HMM(ArrayList<String> states){
        this.states = states;
    }
}
