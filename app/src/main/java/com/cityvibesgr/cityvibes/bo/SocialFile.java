package com.cityvibesgr.cityvibes.bo;

/**
 * Created by alexsideris on 06/03/2017.
 */

public class SocialFile {
    private String url;
    private String placeName;
    private String actualTime;
    private String approximateTime;
    private String instagramUsername;
    private String instagramFullName;
    private String instagramProfilePictureLink;
    private String instagramProfileLink;

    private int id;
    private String day;

    public SocialFile(){

    }

    public String getApproximateTime() {
        return approximateTime;
    }

    public void setApproximateTime(String approximateTime) {
        this.approximateTime = approximateTime;
    }

    public String getInstagramUsername() {
        return instagramUsername;
    }

    public void setInstagramUsername(String instagramUsername) {
        this.instagramUsername = instagramUsername;
    }

    public String getInstagramFullName() {
        return instagramFullName;
    }

    public void setInstagramFullName(String instagramFullName) {
        this.instagramFullName = instagramFullName;
    }

    public String getInstagramProfilePictureLink() {
        return instagramProfilePictureLink;
    }

    public void setInstagramProfilePictureLink(String instagramProfilePictureLink) {
        this.instagramProfilePictureLink = instagramProfilePictureLink;
    }

    public String getInstagramProfileLink() {
        return instagramProfileLink;
    }

    public void setInstagramProfileLink(String instagramProfileLink) {
        this.instagramProfileLink = instagramProfileLink;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getActualTime() {
        return actualTime;
    }

    public void setActualTime(String actualTime) {
        this.actualTime = actualTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

}
