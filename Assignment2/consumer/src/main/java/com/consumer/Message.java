package com.consumer;

import com.consumer.LiftRide;

public class Message {

  private Integer skierID;
  private String dayID;
  private String seasonID;
  private Integer resortID;
  private LiftRide body;

  public Message(Integer skierID, String dayID, String seasonID, Integer resortID, LiftRide body) {
    this.skierID = skierID;
    this.dayID = dayID;
    this.seasonID = seasonID;
    this.resortID = resortID;
    this.body = body;
  }

  public void setSkierID(Integer skierID) {
    this.skierID = skierID;
  }

  public Integer getSkierID() {
    return skierID;
  }

  public String getDayID() {
    return dayID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public Integer getResortID() {
    return resortID;
  }

  public LiftRide getBody() {
    return body;
  }

  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public void setResortID(Integer resortID) {
    this.resortID = resortID;
  }

  public void setBody(LiftRide body) {
    this.body = body;
  }
}
