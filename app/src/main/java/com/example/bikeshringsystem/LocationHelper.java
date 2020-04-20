package com.example.bikeshringsystem;

public class LocationHelper {
     private Double Latitute;

    public LocationHelper(Double latitute, Double longitute) {
        Latitute = latitute;
        Longitute = longitute;
    }

    public Double getLatitute() {
        return Latitute;
    }

    public void setLatitute(Double latitute) {
        Latitute = latitute;
    }

    public Double getLongitute() {
        return Longitute;
    }

    public void setLongitute(Double longitute) {
        Longitute = longitute;
    }

   private Double Longitute;

}
