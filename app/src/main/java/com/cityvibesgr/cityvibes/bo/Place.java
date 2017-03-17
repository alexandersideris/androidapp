package com.cityvibesgr.cityvibes.bo;

/**
 * Created by alexsideris on 7/12/16.
 */
public class Place {
    private String name;
    private String profilePhotoUrl;
    private String facebookAccountLink;
    private String location;
    private String city;
    private String lastUpdate;
    private int views, fileUploads;

    private String type;
    private double drinkPrice, bottlePrice;

    private double winePrice, retsinaPrice;
    private double coffeePrice;
    private int id;

    public Place(){

    }

    public int getFileUploads() {
        return fileUploads;
    }

    public void setFileUploads(int fileUploads) {
        this.fileUploads = fileUploads;
    }

    public double getWinePrice() {
        return winePrice;
    }

    public void setWinePrice(double winePrice) {
        this.winePrice = winePrice;
    }

    public double getRetsinaPrice() {
        return retsinaPrice;
    }

    public void setRetsinaPrice(double retsinaPrice) {
        this.retsinaPrice = retsinaPrice;
    }

    public double getCoffeePrice() {
        return coffeePrice;
    }

    public void setCoffeePrice(double coffeePrice) {
        this.coffeePrice = coffeePrice;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setID(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public void setFacebookAccountLink(String facebookAccountLink) {
        this.facebookAccountLink = facebookAccountLink;
    }

    public void setDrinkPrice(double drinkPrice) {
        this.drinkPrice = drinkPrice;
    }

    public void setBottlePrice(double bottlePrice) {
        this.bottlePrice = bottlePrice;
    }

    public int getID(){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public String getFacebookAccountLink() {
        return facebookAccountLink;
    }

    public double getDrinkPrice() {
        return drinkPrice;
    }

    public double getBottlePrice() {
        return bottlePrice;
    }

    public void setCity(String city){
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public String toString(){
        String s = "Name: "+name+"\nID: "+id+"\nProfile Picture Url: "+profilePhotoUrl+"\nDrink Price: "+drinkPrice+"\nBottle Price: "+
                bottlePrice+"\nFacebook: "+facebookAccountLink;
        return s;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }
}
