package com.cityvibesgr.cityvibes.bo;

/**
 * Created by alexsideris on 8/2/16.
 */
public class PhotoSession {
    private int clubID;
    private int entrancePrice;
    private String time;
    private String musicGenre;
    private String status;
    private String stringTime;
    private int ID;

    public PhotoSession(){

    }

    public void setID(int ID){
        this.ID = ID;
    }

    public int getID(){
        return ID;
    }

    public void setClubID(int clubID){
        this.clubID = clubID;
    }

    public int getClubID(){
        return clubID;
    }

    public void setTime(String time){
        this.time = time;
    }

    public String getTime(){
        return time;
    }

    public void setEntrancePrice(int entrancePrice){
        this.entrancePrice = entrancePrice;
    }

    public int getEntrancePrice(){
        return entrancePrice;
    }

    public void setMusicGenre(String musicGenre){
        this.musicGenre = musicGenre;
    }

    public String getMusicGenre(){
        return musicGenre;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String getStatus(){
        return status;
    }

    public void setStringTime(String stringTime){
        this.stringTime = stringTime;
    }

    public String getStringTime(){
        return stringTime;
    }

    public String toString(){
        return (getStatus()+" "+getStringTime());
    }
}
