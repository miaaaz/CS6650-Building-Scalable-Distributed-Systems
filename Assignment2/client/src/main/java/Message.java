public class Message {

  private Integer skierId;
  private String dayId;
  private String seasonId;
  private Integer resortId;
  private LiftRide body;

  public Message(Integer skierId, String dayId, String seasonId, Integer resortId, LiftRide body) {
    this.skierId = skierId;
    this.dayId = dayId;
    this.seasonId = seasonId;
    this.resortId = resortId;
    this.body = body;
  }

  public void setSkierId(Integer skierId) {
    this.skierId = skierId;
  }

  public void setDayId(String dayId) {
    this.dayId = dayId;
  }

  public Integer getSkierId() {
    return skierId;
  }

  public String getDayId() {
    return dayId;
  }

  public String getSeasonId() {
    return seasonId;
  }

  public Integer getResortId() {
    return resortId;
  }

  public LiftRide getBody() {
    return body;
  }

  public void setSeasonId(String seasonId) {
    this.seasonId = seasonId;
  }

  public void setResortId(Integer resortId) {
    this.resortId = resortId;
  }

  public void setBody(LiftRide body) {
    this.body = body;
  }
}
