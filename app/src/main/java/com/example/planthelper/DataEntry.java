package com.example.planthelper;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class DataEntry {
    private final double recNo;
    private final long soilLvl;
    private final long uvLvl;
    private final Date date;
    private final Calendar calendar;


    /**
     * Object representing an entry in the firestore database
     *
     * @param recName Document name
     * @param soil Soil moisture level
     * @param uv UV exposure level
     * @param timestamp Epoch timestamp
     */
    public DataEntry(String recName, long soil, long uv, String timestamp) throws ParseException {
        String temp = recName.substring(3);
        this.recNo = Double.parseDouble(temp);
        this.soilLvl = soil;
        this.uvLvl = uv;

        long temp2 = Long.parseLong(timestamp) * 1000;
        this.date = new Date(temp2);
        this.calendar = Calendar.getInstance();
        this.calendar.setTime(date);
    }

    public double getRecNo() {
        return recNo;
    }

    public double getSoilLvl() {
        return soilLvl;
    }

    public double getUvLvl() {
        return uvLvl;
    }

    public Date getDate() {
        return date;
    }

    public Calendar getCalendar() {
        return calendar;
    }
}
