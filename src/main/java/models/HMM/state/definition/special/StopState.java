package models.HMM.state.definition.special;


public class StopState extends AbstractSpecialState{
    public StopState(){
        super(null);
    }

    @Override
    public void generateTransitionProbabilities(double passingI) {
        // do nothing
    }

    @Override
    public String getStateName() {
        return "E";
    }
}
