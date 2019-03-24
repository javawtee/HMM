package models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerModel {
    public String getEvent() {
        //convert eventTime to desired Date format
        String datePattern = "HH:mm:ss:SSS"; // equivalent to 23:59:59:999
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        return dateFormat.format(eventTime).concat("- " + event + "\n");
    }

    private String event;
    private Date eventTime;

    public LoggerModel(String event, Date eventTime){
        this.event = event;
        this.eventTime = eventTime;
    }
}
