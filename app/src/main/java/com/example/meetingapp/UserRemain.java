package com.example.meetingapp;

public class UserRemain {
    public int request;
    public int refresh;
    public String lastremain;

    public UserRemain()
    {

    }

    public UserRemain(int request, int refresh)
    {
        this.request = request;
        this.refresh = refresh;
        this.lastremain = null;
    }
}
