package model;

public class LiftDetail {

  private Integer skierID;
  private String seasonID;
  private Integer liftID;
  private Integer time;

  public LiftDetail(Integer skierID, String seasonID, Integer liftID, Integer time) {
    this.skierID = skierID;
    this.seasonID = seasonID;
    this.liftID = liftID;
    this.time = time;
  }

  public Integer getSkierID() {
    return skierID;
  }

  public void setSkierID(Integer skierID) {
    this.skierID = skierID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
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
