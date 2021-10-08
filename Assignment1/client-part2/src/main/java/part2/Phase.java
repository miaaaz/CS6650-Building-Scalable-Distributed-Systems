package part2;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import part2.PhaseRunner.Count;

public class Phase {

  private int phaseThreads;
  private int numSkiersPerThread;
  private int numRequestsPerThread;
  Map<Integer, Count> requestsCounter;
  private int numLifts;
  private int startSkierId;
  private CountDownLatch completed;
  private CountDownLatch tenPercentCompleted;
  private int startTime;
  private int endTime;
  private List<PerformanceRecord> performanceRecords;

  public Phase(int phaseThreads, int numSkiersPerThread, int numRequestsPerThread,
      Map<Integer, Count> requestsCounter, int startSkierId, int numLifts,
      CountDownLatch completed, CountDownLatch tenPercentCompleted, int startTime, int endTime,
      List<PerformanceRecord> performanceRecords
  ) {
    this.phaseThreads = phaseThreads;
    this.numSkiersPerThread = numSkiersPerThread;
    this.numRequestsPerThread = numRequestsPerThread;
    this.requestsCounter = requestsCounter;
    this.startSkierId = startSkierId;
    this.completed = completed;
    this.tenPercentCompleted = tenPercentCompleted;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numLifts = numLifts;
    this.performanceRecords = performanceRecords;
  }

  public void start() {
    for (int i = 0; i < this.phaseThreads; i++) {
      requestsCounter.put(i, new Count());
      int endSkierId = startSkierId + numSkiersPerThread - 1;

      RequestThread requestThread = new RequestThread(startSkierId, endSkierId, startTime, endTime,
          numRequestsPerThread, i, numLifts, requestsCounter, tenPercentCompleted, completed,
          performanceRecords);
      new Thread(requestThread).start();

      startSkierId = endSkierId + 1;
    }
  }

  public int getSuccessfulRequestsCount() {
    int total = 0;
    for (Count counts : requestsCounter.values()) {
      total += counts.success;
    }
    return total;
  }

  public int getFailedRequestsCount() {
    int total = 0;
    for (Count counts : requestsCounter.values()) {
      total += counts.failure;
    }
    return total;
  }
}
