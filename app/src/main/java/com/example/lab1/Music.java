package com.example.lab1;

public class Music {
    private int id;
    private String image;
    private String name;
    private String name_casi;
    public Music(){}
    public Music(int id,String image,String name,String name_casi){
        this.id=id;
        this.image=image;
        this.name=name;
        this.name_casi=name_casi;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName_casi() {
        return name_casi;
    }

    public void setName_casi(String name_casi) {
        this.name_casi = name_casi;
    }
}
