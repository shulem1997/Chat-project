package com.example.chat;

import android.media.Image;

public class Triple {
    private Image img;
    private String name;
    private String date;

    public Triple() {
        img = null;
        name = null;
        date = null;
    }
    public Image getImg() {
        return img;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImg(Image img) {
        this.img = img;
    }

    public void setName(String name) {
        this.name = name;
    }
}
