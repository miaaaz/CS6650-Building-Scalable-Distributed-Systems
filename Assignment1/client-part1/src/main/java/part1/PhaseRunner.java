package part1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PhaseRunner {

  static class Count {

    int success = 0;
    int failure = 0;
  }

  public static void main(String[] args) throws InterruptedException {
    final double TEN_PERCENTAGE = 0.1;
    final int MS_PER_SECOND = 1000;

    // should be command line params, hardcode for convenience in this assignment
    int numThreads = 64;
    int numSkiers = 20000;
    int numRuns = 10;  // default 10
    int numLifts = 40;
    String serverAddress = "http://54.167.231.183:8080/server_war";

    int startSkierId = 1;

    // =================================== //
    // calculate parameters for each phase //
    // =================================== //

    // parameters of phase 1
    final int PHASE1_START_TIME = 1;
    final int PHASE1_END_TIME = 90;
    final double PHASE1_REQUESTS_FACTOR = 0.2;

    int startupThreads = numThreads / 4;

    // Move to phase 2 once 10% (rounded up) of the threads have completed
    int tenPercentPhase1Threads = (int) Math.ceil((startupThreads * TEN_PERCENTAGE));
    CountDownLatch startPhase2 = new CountDownLatch(tenPercentPhase1Threads);

    Map<Integer, Count> phase1RequestsCounter = new HashMap<>();

    int numSkiersPerPhase1Thread = numSkiers / startupThreads;
    int numRequestsPerPhase1Thread = (int) (numRuns * PHASE1_REQUESTS_FACTOR * numSkiersPerPhase1Thread);

    // parameters of phase 2
    final int PHASE2_START_TIME = 91;
    final int PHASE2_END_TIME = 360;
    final double PHASE2_REQUESTS_FACTOR = 0.6;

    int peakPhaseThreads = numThreads;
    int numSkiersPerPhase2Thread = numSkiers / numThreads;
    int numRequestsPerPhase2Thread = (int) (numRuns * PHASE2_REQUESTS_FACTOR * numSkiersPerPhase2Thread);
    int tenPercentPhase2Threads = (int) (peakPhaseThreads * TEN_PERCENTAGE);

    CountDownLatch startPhase3 = new CountDownLatch(tenPercentPhase2Threads);
    Map<Integer, Count> phase2RequestsCounter = new HashMap<>();

    // parameters of phase 3
    final int PHASE3_START_TIME = 361;
    final int PHASE3_END_TIME = 420;
    final double PHASE3_REQUESTS_FACTOR = 0.1;

    int cooldownPhaseThreads = startupThreads;  // identical to phase 1
    int numSkiersPerPhase3Thread = numSkiersPerPhase1Thread;  // identical to phase

    int numRequestsPerPhase3Thread = (int) (numRuns * PHASE3_REQUESTS_FACTOR);

    CountDownLatch allPhaseCompleted = new CountDownLatch(
        startupThreads + peakPhaseThreads + cooldownPhaseThreads);

    Map<Integer, Count> phase3RequestsCounter = new HashMap<>();

    // ==================== //
    // start running phases //
    // ==================== //
    long start = System.currentTimeMillis();

    // phase 1
    Phase phase1 = new Phase(startupThreads, numSkiersPerPhase1Thread,
        numRequestsPerPhase1Thread, phase1RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        startPhase2,
        PHASE1_START_TIME, PHASE1_END_TIME);
    phase1.start();

    startPhase2.await();

    // phase 2
    Phase phase2 = new Phase(peakPhaseThreads, numSkiersPerPhase2Thread,
        numRequestsPerPhase2Thread, phase2RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        startPhase3, PHASE2_START_TIME, PHASE2_END_TIME);
    phase2.start();

    startPhase3.await();

    // phase 3
    Phase phase3 = new Phase(cooldownPhaseThreads, numSkiersPerPhase3Thread,
        numRequestsPerPhase3Thread, phase3RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        null, PHASE3_START_TIME, PHASE3_END_TIME);
    phase3.start();

    // wait for all threads from all phases to complete
    allPhaseCompleted.await();

    // record the end time
    long end = System.currentTimeMillis();

    // ================= //
    // calculate results //
    // ================= //
    int numSuccess =
        phase1.getSuccessfulRequestsCount() + phase2.getSuccessfulRequestsCount() + phase3
            .getSuccessfulRequestsCount();
    int numFailure = phase1.getFailedRequestsCount() + phase2.getFailedRequestsCount() + phase3
        .getFailedRequestsCount();

    long wallTime = (end - start) / MS_PER_SECOND;  // in seconds

    int throughput = (int) ((numSuccess + numFailure) / wallTime);

    // ============== //
    // print results  //
    // ============== //
    System.out.println("--------------------------------");
    System.out.println("Number of Runs: " + numRuns);
    System.out.println("Number of Threads: " + numThreads);
    System.out.println("Total success: " + numSuccess);
    System.out.println("Total failure: " + numFailure);

    System.out.println("Wall time:" + wallTime + " seconds");
    System.out.println("Throughput: " + throughput);

  }


}
