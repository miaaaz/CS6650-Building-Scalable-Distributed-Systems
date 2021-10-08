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

    // should be command line params, hardcode for convenience in this assignment
    int numThreads = 64;
    int numSkiers = 20000;
    int numRuns = 10;  // default 10
    int numLifts = 40;

    int startSkierId = 1;

    // =================================== //
    // calculate parameters for each phase //
    // =================================== //

    // parameters of phase 1
    int startupThreads = numThreads / 4;

    // Move to phase 2 once 10% (rounded up) of the threads have completed
    int tenPercentPhase1Threads = (int) Math.ceil((startupThreads * 0.1));
    CountDownLatch startPhase2 = new CountDownLatch(tenPercentPhase1Threads);
    CountDownLatch phase1Completed = new CountDownLatch(startupThreads);

    Map<Integer, Count> phase1RequestsCounter = new HashMap<>();

    int numSkiersPerPhase1Thread = numSkiers / startupThreads;
    int numRequestsPerPhase1Thread = (int) (numRuns * 0.2 * numSkiersPerPhase1Thread);

    // parameters of phase 2
    final int PHASE2_START_TIME = 91;
    final int PHASE2_END_TIME = 360;

    int peakPhaseThreads = numThreads;
    int numSkiersPerPhase2Thread = numSkiers / numThreads;
    int numRequestsPerPhase2Thread = (int) (numRuns * 0.6 * numSkiersPerPhase2Thread);
    int tenPercentPhase2Threads = (int) (peakPhaseThreads * 0.1);

    CountDownLatch startPhase3 = new CountDownLatch(tenPercentPhase2Threads);
    CountDownLatch phase2Completed = new CountDownLatch(peakPhaseThreads);
    Map<Integer, Count> phase2RequestsCounter = new HashMap<>();

    // parameters of phase 3
    final int PHASE3_START_TIME = 361;
    final int PHASE3_END_TIME = 420;

    // identical to phase 1
    int cooldownPhaseThreads = startupThreads;
    int numSkiersPerPhase3Thread = numSkiersPerPhase1Thread;

    int numRequestsPerPhase3Thread = (int) (numRuns * 0.1);
    CountDownLatch phase3Completed = new CountDownLatch(cooldownPhaseThreads);

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
        1, 90);
    phase1.start();

    System.out.println("total threads: " + phase1Completed.getCount());

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
//    phase2Completed.await();
//    phase3Completed.await();

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

    long wallTime = (end - start) / 1000;
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