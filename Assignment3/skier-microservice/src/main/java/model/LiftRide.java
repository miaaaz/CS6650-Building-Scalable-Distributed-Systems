package model;

import java.io.Serializable;

public class LiftRide implements Serializable {

  private Integer skierID;
  private String dayID;
  private String seasonID;
  private Integer resortID;
  private Integer liftID;
  private Integer time;

  public LiftRide(Integer skierID, String dayID, String seasonID, Integer resortID,
      Integer liftID, Integer time) {
    this.skierID = skierID;
    this.dayID = dayID;
    this.seasonID = seasonID;
    this.resortID = resortID;
    this.liftID = liftID;
    this.time = time;
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


  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public void setResortID(Integer resortID) {
    this.resortID = resortID;
  }

  public Integer getLiftID() {
    return liftID;
  }

  public void setLiftID(Integer liftID) {
    this.liftID = liftID;
  }

  public Integer getTime() {
    return time;
  }

  public void setTime(Integer time) {
    this.time = time;
  }
}
