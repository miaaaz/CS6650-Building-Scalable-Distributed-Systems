package com.consumer;

public class LiftRide {

  private Integer time;
  private Integer liftID;

  public LiftRide(int time, int liftId) {
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
