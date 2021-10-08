package part2;

public class PerformanceRecord {

  private long startTime;
  private long latency;
  private String requestType;
  private int statusCode;

  public PerformanceRecord(long startTime, long latency, String requestType, int statusCode) {
    this.startTime = startTime;
    this.latency = latency;
    this.requestType = requestType;
    this.statusCode = statusCode;
  }

  public String getStartTimeString() {
    return String.valueOf(startTime);
  }

  public String getLatencyString() {
    return String.valueOf(latency);
  }

  public String getRequestType() {
    return requestType;
  }

  public String getStatusCodeString() {
    return String.valueOf(statusCode);
  }

  public long getStartTime() {
    return startTime;
  }

  public long getLatency() {
    return latency;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
