package models;

public class SequenceModel {
    private String sequenceName;
    private String sequence;

    public SequenceModel(String sequenceName, String sequence){
        this.sequenceName = sequenceName;
        this.sequence = sequence;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public char[] getSequence(){
        return sequence.toCharArray();
    }

    public int getSequenceLength(){
        return sequence.length();
    }
}
