package com.SNEWP.mainschedule;

import android.location.Location;
import android.provider.ContactsContract;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;

public class Apunte {
    private String name;
    private String accountId;
    private int startHour;
    private int startMinute;
    private Location location;
    private String plates;
    private String phoneNumber;
    private double lat;
    private double lng;
    private String route;

    public Apunte(String name, String accountId, String phoneNumber, int startHour, int startMinute, double lat, double lng){
        this.name = name;
        this.accountId = accountId;
        this.phoneNumber = phoneNumber.trim().replaceAll("\\s+","");
        this.lat = lat;
        this.lng = lng;
        this.startHour = startHour;
        this.startMinute = startMinute;
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
            return new Pair<>(startHour, startMinute);
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
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


    public String getAccountId() {
        return accountId;
    }
}
