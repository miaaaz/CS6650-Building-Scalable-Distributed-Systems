package model;

import util.JsonRequired;

public class LiftPayload {

  @JsonRequired
  private Integer time;
  @JsonRequired private Integer liftID;

  public LiftPayload(int time, int liftId) {
    this.time = time;
    this.liftID = liftId;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getLiftId() {
    return liftID;
  }

  public void setLiftId(int liftId) {
    this.liftID = liftId;
  }
}
