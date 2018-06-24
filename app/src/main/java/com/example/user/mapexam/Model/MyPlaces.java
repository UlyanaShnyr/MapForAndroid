package com.example.user.mapexam.Model;

public class MyPlaces {
    private String address;

    private Devices[] devices;

    private String city;

    public String getAddress ()
    {
        return address;
    }

    public void setAddress (String address)
    {
        this.address = address;
    }

    public Devices[] getDevices ()
    {
        return devices;
    }

    public void setDevices (Devices[] devices)
    {
        this.devices = devices;
    }

    public String getCity ()
    {
        return city;
    }

    public void setCity (String city)
    {
        this.city = city;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [address = "+address+", devices = "+devices+", city = "+city+"]";
    }


}
