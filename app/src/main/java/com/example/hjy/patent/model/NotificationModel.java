package com.example.hjy.patent.model;

public class NotificationModel {

    public  String to;
    public Data data = new Data();
    public Notification notification= new Notification(); //초기화 해주지 않으면 에러

    public static class Notification{  //inner class
        public  String title;
        public String text;
    }

    public static  class Data {
        public String title;
        public String text;
    }
}
