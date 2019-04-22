package views;

import models.HMM.HMM;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class RawHMMView extends JFrame {
    private JPanel mainContainer;
    private JTextPane hmmViewer;

    public RawHMMView(HMM hmm){
        this.setMinimumSize(new Dimension(300, 600));
        this.setPreferredSize(new Dimension(300, 600));
        this.setMaximumSize(new Dimension(300, 600));

        this.add(mainContainer);
        try {
            printRawHMMData(hmm);
        } catch (BadLocationException ex){
            // ignored
        }

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void printRawHMMData(HMM hmm) throws BadLocationException {
        // prepare document
        StyledDocument doc = hmmViewer.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.WHITE);
        StyleConstants.setBackground(keyWord, Color.BLUE);
        StyleConstants.setBold(keyWord, true);

        // BASIC INFORMATION
        doc.insertString(0, "HMM size: " + hmm.getHMMSize() + "\n", keyWord);
        doc.insertString(doc.getLength(),"Number of match states: " + ((hmm.getHMMSize() -3) /2) + "\n", keyWord);
        doc.insertString(doc.getLength(),"--------------------------------------------------------------\n", null);

        // DATA DETAILS
        for(int i = 0; i < hmm.getHMMSize(); i++){
            String stateName = hmm.getState(i).getStateName();
            doc.insertString(doc.getLength(),"##### " + stateName + " #####\n", keyWord);
            for(Character emission : hmm.getState(i).getEmissionProbabilities().keySet()){
                doc.insertString(doc.getLength(),
                            emission + ": " + hmm.getState(i).getEmissionProbabilities().get(emission) + "\n",
                             null);
            }
            if(stateName.startsWith("M")) {
                int index = Integer.parseInt(stateName.substring(1, stateName.length()));
                for (String probabilities : hmm.getMdTransitions().get(index - 1).getTransitionProbabilities().keySet()) {
                    doc.insertString(doc.getLength(),
                                "--- " +
                                    probabilities + ": " +
                                    hmm.getMdTransitions().get(index - 1).getTransitionProbabilities().get(probabilities) +
                                    "\n",
                                 null);
                }
            } else {
                for(String probabilities: hmm.getState(i).getTransitionProbabilities().keySet()){
                    doc.insertString(doc.getLength(),
                                "--- " +
                                    probabilities + ": " +
                                    hmm.getState(i).getTransitionProbabilities().get(probabilities) +
                                    "\n",
                                 null) ;
                }
            }
        }
    }
}
