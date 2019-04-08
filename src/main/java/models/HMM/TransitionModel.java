package models.HMM;
    /*
        Transitioning scenarios:
        curr state is start:
        - to match (sM)
        - to deletion (sD)
        - to insertion (sI)
        curr state is match:
        - to match (mM)
        - to deletion (mD)
        - to insertion (mI)
        (Separate, may exist) curr state is deletion:
        - to match (dM)
        - to deletion (dD)
        - to insertion (dI)
        (Separate, always exists) curr state is insertion:
        - to match (iM)
        - to deletion (iD)
        - to insertion (iI) // loop in insert state
     */

public class TransitionModel {
    private double sM, sI, sD, mM, mD, mI, dM, dD, dI, iM, iD, iI;

    public TransitionModel(){
        sM = sI = sD = mM = mD = mI = dM = dD = dI = iM = iD = iI = 0;
    }

    public void addSMCount(){sM++;}

    public void addSICount(){sI++;}

    public void addSDCount(){sD++;}

}
