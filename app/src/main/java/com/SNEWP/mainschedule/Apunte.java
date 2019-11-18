package com.SNEWP.mainschedule;

import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Apunte {
    static final int MON = 0;
    static final int TUE = 1;
    static final int WED = 2;
    static final int THU = 3;
    static final int FRI = 4;
    static final int SAT = 5;
    static final int SUN = 6;

    private String name;
    private String accountId;
    private String apunteID;
    private ArrayList<Integer> days;
    private int startHour;
    private int startMinute;
    private String plates;
    private String phoneNumber;
    private GeoPoint location;
    private double lat;
    private double lng;
    private String route;
    private ArrayList<String> zonasID;

    public Apunte(String name, String accountId, String phoneNumber, ArrayList<Integer> days,
                  int startHour, int startMinute, GeoPoint location, ArrayList<String> zonasID){
        this.name = name;
        this.accountId = accountId;
        this.phoneNumber = phoneNumber.trim().replaceAll("\\s+","");
        this.days = days;
        this.location = location;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.zonasID = zonasID;
    }
    public Apunte(Map<String, Object> map, String accountId){
        name = (String) map.get("name");
        this.accountId = accountId;
        phoneNumber = (String) map.get("phoneNumber");
        location = (GeoPoint) map.get("location");
        lat= location.getLatitude();
        lng= location.getLongitude();
        ArrayList<Long> longs = (ArrayList<Long>) map.get("days");
        ArrayList<Integer> dayList = new ArrayList<>();
        if (longs != null) {
            for (Long lon : longs) {
                dayList.add(lon.intValue());
            }
        }
        days = dayList;
        String[] time = ((String) map.get("time")).split(":");
        startHour = Integer.valueOf(time[0]);
        startMinute = Integer.valueOf(time[1]);
        if(map.get("route") != null)
            route = (String) map.get("route");
        if(map.get("plates") != null)
            plates = (String) map.get("plates");
        zonasID = (ArrayList<String>) map.get("zonasID");
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Pair<Integer, Integer> getStartTime(){
        return new Pair<>(startHour, startMinute);
    }

    public Pair<Integer, Integer> getEndTime(){
        if(startMinute >= 40){
            return new Pair<>(startHour+1, startMinute-40);
        }else {
            return new Pair<>(startHour, startMinute+20);
        }
    }

    public int getStartHour() {
        return startHour;
    }

    public LatLng getLatLng(){
        return new LatLng(lat, lng);
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getPlates() {
        return plates;
    }

    public void setPlates(String plates) {
        this.plates = plates;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber.trim().replaceAll("\\s+","");
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getTime(){
        String hour = String.valueOf(startHour);
        String minute = String.valueOf(startMinute);
        if(hour.length()==1)
            hour = "0"+hour;
        if(minute.length()==1)
            minute = "0"+minute;
        String time = String.format("%s:%s", hour, minute );
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.US);
            Date dateObj = sdf.parse(time);
            return new SimpleDateFormat("K:mm a", Locale.US).format(dateObj);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public String getAccountId() {
        return accountId;
    }

    public Map<String, Object> getMap(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("accountId", accountId);
        map.put("days", days);
        map.put("time", startHour+":"+startMinute);
        map.put("plates", plates);
        map.put("phoneNumber",phoneNumber );
        map.put("location", location);
        map.put("route",route );
        map.put("zonasID", zonasID);
        return map;
    }

    public ArrayList<Integer> getDays() {
        return days;
    }

    public void setDays(ArrayList<Integer> days) {
        this.days = days;
    }

    public String getApunteID() {
        return apunteID;
    }

    public void setApunteID(String apunteID) {
        this.apunteID = apunteID;
    }
}
