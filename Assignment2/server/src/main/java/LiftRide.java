import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


public class LiftRide {

  @JsonRequired private Integer time;
  @JsonRequired private Integer liftId;

  public LiftRide(int time, int liftId) {
    this.time = time;
    this.liftId = liftId;
  }

  public int getTime() {
    return time;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getLiftId() {
    return liftId;
  }

  public void setLiftId(int liftId) {
    this.liftId = liftId;
  }
}
