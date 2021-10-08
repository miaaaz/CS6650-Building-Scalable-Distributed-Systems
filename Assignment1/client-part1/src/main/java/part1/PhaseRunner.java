package part1;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PhaseRunner {

  static class Count {
    int success = 0;
    int failure = 0;
  }

  public static void main(String[] args) throws InterruptedException {

    // should be command line params, hardcode for convenience in this assignment
    int numThreads = 32;
    int numSkiers = 20000;
    int numRuns = 11;  // default 10
    int numLifts = 40;

    int startSkierId = 1;
    int numSuccess = 0;
    int numFailure = 0;

    // Phase 1
    int startupThreads = numThreads / 4;

    // Move to phase 2 once 10% (rounded up) of the threads have completed
    int tenPercentPhase1Threads = (int) Math.ceil((startupThreads * 0.1));
    CountDownLatch startPhase2 = new CountDownLatch(tenPercentPhase1Threads);
    CountDownLatch phase1Completed = new CountDownLatch(startupThreads);

    Map<Integer, Count> phase1RequestsCounter = new HashMap<>();

    int numSkiersPerPhase1Thread = numSkiers / startupThreads;
    int numRequestsPerPhase1Thread = (int) (numRuns * 0.2 * numSkiersPerPhase1Thread);

//    System.out.println("phase1 threads: " + startupThreads);
//    System.out.println("requests per phase 1 thread: " + numRequestsPerPhase1Thread);
    long start = System.currentTimeMillis();

    Phase phase1 = new Phase(startupThreads, numSkiersPerPhase1Thread,
        numRequestsPerPhase1Thread, phase1RequestsCounter, startSkierId, numLifts, phase1Completed,
        startPhase2,
        1, 90);
    phase1.start();

//    for (int i = 0; i < startupThreads; i++) {
//      phase1RequestsCounter.put(i, new Count());
//      int endSkierId = startSkierId + numSkiersPerPhase1Thread - 1;
//      RequestThread requestThread = new RequestThread(startSkierId, endSkierId, 1, 90,
//          numRequestsPerPhase1Thread, i, numLifts, phase1RequestsCounter, startPhase2, phase1Completed);
//      new Thread(requestThread).start();
//
//      startSkierId = endSkierId + 1;
//    }

    startPhase2.await();
//    for (Count total : phase1RequestsCounter.values()) {
//      numSuccess += total.success;
//      numFailure += total.failure;
//    }

//    System.out.println("phase 1 success: " + phase1.getSuccessfulRequestsCount());
//    System.out.println("phase 1 failure: " + phase1.getFailedRequestsCount());

    // phase 2
    final int PHASE2_START_TIME = 91;
    final int PHASE2_END_TIME = 360;

    int peakPhaseThreads = numThreads;
    int numSkiersPerPhase2Thread = numSkiers / numThreads;
    int numRequestsPerPhase2Thread = (int) (numRuns * 0.6 * numSkiersPerPhase2Thread);
    int tenPercentPhase2Threads = (int) (peakPhaseThreads * 0.1);

    CountDownLatch startPhase3 = new CountDownLatch(tenPercentPhase2Threads);
    CountDownLatch phase2Completed = new CountDownLatch(peakPhaseThreads);
    Map<Integer, Count> phase2RequestsCounter = new HashMap<>();

    Phase phase2 = new Phase(peakPhaseThreads, numSkiersPerPhase2Thread,
        numRequestsPerPhase2Thread, phase2RequestsCounter, startSkierId, numLifts, phase2Completed,
        startPhase3, PHASE2_START_TIME, PHASE2_END_TIME);
    phase2.start();
    startPhase3.await();


    // phase 3
    final int PHASE3_START_TIME = 361;
    final int PHASE3_END_TIME = 420;

    // identical to phase 1
    int cooldownPhaseThreads = startupThreads;
    int numSkiersPerPhase3Thread = numSkiersPerPhase1Thread;

    int numRequestsPerPhase3Thread = (int) (numRuns * 0.1);
    CountDownLatch phase3Completed = new CountDownLatch(cooldownPhaseThreads);

    Map<Integer, Count> phase3RequestsCounter = new HashMap<>();


    Phase phase3 = new Phase(cooldownPhaseThreads, numSkiersPerPhase3Thread,
        numRequestsPerPhase3Thread, phase3RequestsCounter, startSkierId, numLifts, phase3Completed,
        null,
        PHASE3_START_TIME, PHASE3_END_TIME);
    phase3.start();

    // wait for all threads from all phases to complete
    phase1Completed.await();
    phase2Completed.await();
    phase3Completed.await();

    // record the end time
    long end = System.currentTimeMillis();

    numSuccess = phase1.getSuccessfulRequestsCount() + phase2.getSuccessfulRequestsCount() + phase3
        .getSuccessfulRequestsCount();
    numFailure = phase1.getFailedRequestsCount() + phase2.getFailedRequestsCount() + phase3
        .getFailedRequestsCount();
    long wallTime = (end - start) / 1000;
    int throughput = (int) ((numSuccess + numFailure) / wallTime);

//    numSuccess += phase2.getSuccessfulRequestsCount() + phase3
//        .getSuccessfulRequestsCount();
//    numFailure += phase2.getFailedRequestsCount() + phase3
//        .getFailedRequestsCount();

    // print results
    System.out.println("--------------------------------");
    System.out.println("Number of Runs: " + numRuns);
    System.out.println("Number of Threads: " + numThreads);
    System.out.println("Total success: " + numSuccess);
    System.out.println("Total failure: " + numFailure);

    System.out.println("Wall time:" + wallTime + " seconds");
    System.out.println("Throughput: " + throughput);

  }


}
