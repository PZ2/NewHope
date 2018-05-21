package com.example.misio.newhope;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by student on 06.04.2018.
 */

public class RealmPulseReading extends RealmObject {
    // Primary key of this entity
    @PrimaryKey
    private long date;
    private int value;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}