package model;

public class LiftDetail {

  private String dayID;
  private String seasonID;
  private Integer resortID;
  private Integer vertical;

  private Integer time;

  public LiftDetail(String dayID, String seasonID, Integer resortID, Integer time) {
    this.dayID = dayID;
    this.seasonID = seasonID;
    this.resortID = resortID;
    this.time = time;
    this.vertical = 100;
  }

  public String getDayID() {
    return dayID;
  }

  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public Integer getResortID() {
    return resortID;
  }

  public Integer getVertical() {
    return vertical;
  }

  public void setVertical(Integer vertical) {
    this.vertical = vertical;
  }

  public void setResortID(Integer resortID) {
    this.resortID = resortID;
  }

  public Integer getTime() {
    return time;
  }

  public void setTime(Integer time) {
    this.time = time;
  }
}
