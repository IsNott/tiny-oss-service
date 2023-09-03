package com.nott.movie.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class SysMovieInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String pubName;

    private String otherName;

    private String year;

    private String movieDesc;

    private String countryRegion;

    private BigDecimal rateDb;

    private BigDecimal rateImdb;

    private String actors;

    private String coverImageId;

    public SysMovieInfo() {
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getCoverImageId() {
        return coverImageId;
    }

    public void setCoverImageId(String coverImageId) {
        this.coverImageId = coverImageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPubName() {
        return pubName;
    }

    public void setPubName(String pubName) {
        this.pubName = pubName;
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        this.otherName = otherName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMovieDesc() {
        return movieDesc;
    }

    public void setMovieDesc(String movieDesc) {
        this.movieDesc = movieDesc;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public BigDecimal getRateDb() {
        return rateDb;
    }

    public void setRateDb(BigDecimal rateDb) {
        this.rateDb = rateDb;
    }

    public BigDecimal getRateImdb() {
        return rateImdb;
    }

    public void setRateImdb(BigDecimal rateImdb) {
        this.rateImdb = rateImdb;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }
}
