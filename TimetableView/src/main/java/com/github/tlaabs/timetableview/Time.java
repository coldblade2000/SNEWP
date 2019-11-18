package com.github.tlaabs.timetableview;

import java.io.Serializable;

public class Time implements Serializable {
    private int hour = 0;
    private int minute = 0;

    public Time(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    /**
     * @param time retrieves time formatted as "hh:mm"
     */
    public Time(String time){
        String[] timePieces = time.split(":");
        this.hour = Integer.valueOf(timePieces[0]);
        this.minute = Integer.valueOf(timePieces[1]);
    }

    public Time() { }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
