package com.example.zaki_berouk.savedbythebell.model;

import java.util.Date;

public class Event {

    private String name;
    private Date date;
    private String location;
    private String descr;
    private Date departureTime;

    public Event(String name, Date date, String location, String descr) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.descr = descr;
    }

    public Event(String name, Date date, String location, String descr, Date departureTime) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.descr = descr;
        this.departureTime = departureTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }
}
