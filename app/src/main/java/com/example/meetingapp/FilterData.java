package com.example.meetingapp;

import java.util.logging.Filter;

public class FilterData {
    public int startage;
    public int endage;
    public String location;

    public FilterData()
    {

    }

    public FilterData(int startage, int endage, String location)
    {
        this.startage = startage;
        this.endage = endage;
        this.location = location;
    }
}
