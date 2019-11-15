package com.SNEWP.mainschedule;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.lang.reflect.Array;
import java.util.ArrayList;

import androidx.core.graphics.ColorUtils;

public class Zona {
    private ArrayList<String> nombres;
    private String zonaID;
    private ArrayList<Apunte> apuntes;
    private ArrayList<GeoPoint> puntos;
    private int color;

    public Zona(){}

    public Zona(ArrayList<String> nombres, String zonaID, ArrayList<Apunte> apuntes, ArrayList<GeoPoint> puntos, int color){

        this.setNombres(nombres);
        this.setZonaID(zonaID);
        this.setApuntes(apuntes);
        this.setPuntos(puntos);
        this.color = color;
    }

    public static int getMatColor(Context context, String typeColor, int alpha)
    {
        int returnColor = Color.BLACK;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getPackageName());

        if (arrayId != 0)
        {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return ColorUtils.setAlphaComponent(returnColor, alpha);
//        return returnColor;
    }

    public ArrayList<LatLng> getLatLngPoints(){
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for(GeoPoint p : puntos){
            latLngs.add(new LatLng(p.getLatitude(), p.getLongitude()));
        }
        return latLngs;
    }

    public ArrayList<String> getNombres() {
        return nombres;
    }

    public void setNombres(ArrayList<String> nombres) {
        this.nombres = nombres;
    }

    public String getNombresAsString(){
        String retString = "";

        for (int i = nombres.size()-1; i >= 0; i--) {
            retString = nombres.get(i)+", " +retString;
        }
        return retString.substring(0, retString.length()-2);
    }

    public String getZonaID() {
        return zonaID;
    }

    public void setZonaID(String zonaID) {
        this.zonaID = zonaID;
    }

    public ArrayList<Apunte> getApuntes() {
        return apuntes;
    }

    public void setApuntes(ArrayList<Apunte> apuntes) {
        this.apuntes = apuntes;
    }

    public ArrayList<GeoPoint> getPuntos() {
        return puntos;
    }

    public void setPuntos(ArrayList<GeoPoint> puntos) {
        this.puntos = puntos;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
