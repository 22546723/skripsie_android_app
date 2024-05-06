package com.example.planthelper;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DataEntry {
    private double recNo;
    private long soilLvl;
    private long uvLvl;
    private Date date;
    private Calendar calendar;


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
//        Log.i("FBASE", temp);
        this.recNo = Double.parseDouble(temp);
        this.soilLvl = soil;
        this.uvLvl = uv;

        long temp2 = Long.parseLong(timestamp) * 1000;
        this.date = new Date(temp2);
        this.calendar = Calendar.getInstance();
        this.calendar.setTime(date);
        int tempM = calendar.get(Calendar.MONTH);
//        this.date = new Date(timestamp);
//
//        DateFormat df = DateFormat.getDateInstance();
////        df.setTimeZone(TimeZone.getTimeZone("South Africa/Cape Town"));
//        this.date = df.parse(timestamp);
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
