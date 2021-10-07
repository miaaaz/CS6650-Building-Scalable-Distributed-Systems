package part1;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PhaseRunner {

  static class Count {
    int success = 0, failure = 0;
  }

  public static void main(String[] args) throws InterruptedException {

    // TODO: command line params
    int numThreads = 64;
    int numSkiers = 1024;  // max 100000


    int numSuccess = 0;
    int numFailure = 0;


    // Phase 1 will launch numThreads/4 threads
    int startupThreads = numThreads / 4;

    // Move to phase 2 once 10% (rounded up) of the threads have completed
    int tenPercentThreads = (int) Math.ceil((startupThreads * 0.1));
    CountDownLatch phase1Completed = new CountDownLatch(tenPercentThreads);

    Map<Integer, Count> requestsCounter = new HashMap<>();

    int numSkiersPerThread = numSkiers / startupThreads;
    int startSkierId = 1;

    for (int i = 0; i < startupThreads; i++) {
      requestsCounter.put(i, new Count());
      int endSkierId = startSkierId + numSkiersPerThread - 1;
      RequestThread requestThread = new RequestThread(i, requestsCounter, startSkierId, endSkierId, 1, 90, 5, phase1Completed);
      new Thread(requestThread).start();

      startSkierId = endSkierId + 1;
    }

    phase1Completed.await();
    for (Count total: requestsCounter.values()) {
      numSuccess += total.success;
      numFailure += total.failure;
    }
    System.out.println("success: " + numSuccess);
    System.out.println("failure: " + numFailure);



  }

}
