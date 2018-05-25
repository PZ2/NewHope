package com.example.misio.newhope;

/**
 * Created by student on 25.05.2018.
 */

public class BleDevice {

    private String name;
    private String address;

    public BleDevice(String name, String address){
        this.name = name;
        this.address = address;
    }

    public String getName(){
        return name;
    }

    public String getAddress() {
        return address;
    }
}
