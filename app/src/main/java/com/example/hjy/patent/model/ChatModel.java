package com.example.hjy.patent.model;

import org.w3c.dom.Comment;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public Map<String, Boolean> users = new HashMap<>(); //채팅방 유저들

    public Map<String, Comment> comments =  new HashMap<>(); //채팅방 대화내용

    public static class Comment {
        public String uid;
        public String message;
        public Object timestamp;

        public String getUid() {
            return uid;
        }

        public String getMessage() {
            return message;
        }

        public Object getTimestamp() {
            return timestamp;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setTimestamp(Object timestamp) {
            this.timestamp = timestamp;
        }
    }


}
