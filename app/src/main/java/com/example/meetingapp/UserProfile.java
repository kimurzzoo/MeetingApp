package com.example.meetingapp;

public class UserProfile {
    public int age;
    public String authnumber;
    public boolean ban;
    public String email;
    public String job;
    public String location;
    public String memo;
    public String name;
    //private Report report;
    public int sex;
    public boolean extraprofiletutorial;
    public boolean pictureselecttutorial;
    public boolean introductiontutorial;

    public UserProfile(){

    }

    public UserProfile(int age, String authnumber, boolean ban, String email, String job, String location, String memo, String name, int sex, boolean extraprofiletutorial, boolean pictureselecttutorial, boolean introductiontutorial){
        this.age = age;
        this.authnumber = authnumber;
        this.ban = ban;
        this.email = email;
        this.job = job;
        this.location = location;
        this.memo = memo;
        this.name = name;
        //this.report = report;
        this.sex = sex;
        this.extraprofiletutorial = extraprofiletutorial;
        this.pictureselecttutorial = pictureselecttutorial;
        this.introductiontutorial = introductiontutorial;
    }

    public int getAge(){
        return age;
    }

    public void setAge(int age){
        this.age = age;
    }

    public boolean getBan(){
        return ban;
    }

    public void setBan(boolean ban){
        this.ban = ban;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getJob(){
        return job;
    }

    public void setJob(String job){
        this.job = job;
    }

    public String getMemo(){
        return memo;
    }

    public void setMemo(String memo){
        this.memo = memo;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getSex(){
        return sex;
    }

    public void setSex(int sex){
        this.sex = sex;
    }
}



class Report{
    Reportcontent[] reportcontent;
}

class Reportcontent{
    String userId;
    String content;
}
