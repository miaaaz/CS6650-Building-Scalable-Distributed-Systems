package part2;

import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.*;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import part2.PhaseRunner.Count;

public class RequestThread implements Runnable {

  private int startSkierId;
  private int endSkierId;
  private int startLiftId;
  private int endLiftId;
  private int startTime;  // in minutes
  private int endTime;
  private int numRequests;  // number of requests to send
  private String serverAddress;


  int id;
  Map<Integer, Count> counter;

  private CountDownLatch tenPercentCompleted;
  private CountDownLatch completed;

  private List<PerformanceRecord> performanceRecords;

  public RequestThread(int startSkierId, int endSkierId,
      int startTime, int endTime, int numRequests,
      int id, int endLiftId, Map<Integer, Count> counter, CountDownLatch tenPercentCompleted,
      CountDownLatch completed, List<PerformanceRecord> performanceRecords, String serverAddress) {
    this.startSkierId = startSkierId;
    this.endSkierId = endSkierId;
    this.startLiftId = 1;
    this.endLiftId = endLiftId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numRequests = numRequests;
    this.serverAddress = serverAddress;
    this.id = id;
    this.counter = counter;
    this.tenPercentCompleted = tenPercentCompleted;
    this.completed = completed;
    this.performanceRecords = performanceRecords;
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread,
   * starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    SkiersApi apiInstance = new SkiersApi();
//    ApiClient client = apiInstance.getApiClient();
    ApiClient client = new ApiClient();
    client.setBasePath(serverAddress);

    apiInstance.setApiClient(client);

    for (int i = 0; i < this.numRequests; i++) {

//      System.out.println("Request " + i + "for phase " + phase + "thread " + threadNum + " starts");

      int failedTimes = 0;

      // retry up to 5 times for a failed request
      while (!sendPOSTRequest(apiInstance) && failedTimes != 5) {
        failedTimes++;

      }

      // count successful or failed request
      if (failedTimes == 5) {
        this.counter.getOrDefault(id, new Count()).failure++;
      } else {
        this.counter.getOrDefault(id, new Count()).success++;
      }
//      System.out.println("Requests remaining: " + (numRequests - i - 1));

//      System.out.println("Request " + i + "for phase " + phase + "thread " + threadNum + " finished");
    }

    // consume requests
    if (tenPercentCompleted != null) {
      tenPercentCompleted.countDown();
    }
    completed.countDown();
    System.out.println("Threads remaining: " + completed.getCount());


  }

  /**
   * Send a POST request to server
   *
   * @param apiInstance the api instance for this thread
   * @return true if the request is successfully sent, false otherwise.
   */
  private boolean sendPOSTRequest(SkiersApi apiInstance) {
    ApiResponse<Void> apiResponse = null;

    // hardcode for assignment1
    LiftRide body = new LiftRide();
    Integer resortID = 56;
    String seasonID = "2019";
    String dayID = "1";

    // randomly select skierID, liftID and a time value
    Integer skierID = getRandomNumFromRange(startSkierId, endSkierId);
    body.setLiftID(getRandomNumFromRange(startLiftId, endLiftId));
    body.setTime(getRandomNumFromRange(startTime, endTime));

    long startTimestamp = System.currentTimeMillis();
    try {
      apiResponse = apiInstance
          .writeNewLiftRideWithHttpInfo(body, resortID, seasonID, dayID, skierID);

    } catch (ApiException e) {
      System.err.println("Exception when calling SkiersApi#writeNewLiftRide");
      e.printStackTrace();
    }

    if (apiResponse == null) {
      return false;
    }

    int statusCode = apiResponse.getStatusCode();
    long endTimestamp = System.currentTimeMillis();
    long latency = endTimestamp - startTimestamp;

    PerformanceRecord record =
        new PerformanceRecord(startTimestamp, latency, "POST", statusCode);
    performanceRecords.add(record);

    // check if status code is 2xx.
    return statusCode / 100 == 2;
  }

  private int getRandomNumFromRange(int start, int end) {
    return ThreadLocalRandom.current().nextInt(start, end + 1);

  }

  public List<PerformanceRecord> getPerformanceRecords() {
    return performanceRecords;
  }
}
