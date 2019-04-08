package controllers;

import models.LoggerModel;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.util.Date;

class Logger {
    private JScrollPane scrollPane;
    private JTextArea loggerView;

    Logger(JScrollPane scrollPane, JTextArea loggerView){
        this.scrollPane = scrollPane;
        this.loggerView = loggerView;
    }

    void addEvent(String event){
        LoggerModel loggerModel = new LoggerModel(event, new Date());
        //try{
            // add new event to the top of loggerView
            //loggerView.getDocument().insertString(0, loggerModel.getEvent(), null);
            loggerView.append(loggerModel.getEvent());
            // reload loggerView
            scrollPane.repaint();
            scrollPane.revalidate();
        //} catch (BadLocationException ex){
            //ignored
        //}
    }
}
