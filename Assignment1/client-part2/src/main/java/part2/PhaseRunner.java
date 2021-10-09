package part2;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

  public static void main(String[] args) throws InterruptedException, IOException {
    final double TEN_PERCENTAGE = 0.1;

    // should be command line params, hardcode for convenience in this assignment
    int numThreads = 64;
    int numSkiers = 20000;
    int numRuns = 10;  // default 10
    int numLifts = 40;
    String serverAddress = "http://54.167.231.183:8080/server_war";

    int startSkierId = 1;
    List<PerformanceRecord> performanceRecords = new ArrayList<>();

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

    Map<Integer, Count> phase3RequestsCounter = new HashMap<>();
    CountDownLatch allPhaseCompleted = new CountDownLatch(
        startupThreads + peakPhaseThreads + cooldownPhaseThreads);

    // ==================== //
    // start running phases //
    // ==================== //

    // start running phases
    long start = System.currentTimeMillis();

    // phase 1
    Phase phase1 = new Phase(startupThreads, numSkiersPerPhase1Thread,
        numRequestsPerPhase1Thread, phase1RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        startPhase2,
        PHASE1_START_TIME, PHASE1_END_TIME, performanceRecords, serverAddress);
    phase1.start();


    startPhase2.await();


    // phase 2
    Phase phase2 = new Phase(peakPhaseThreads, numSkiersPerPhase2Thread,
        numRequestsPerPhase2Thread, phase2RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        startPhase3, PHASE2_START_TIME, PHASE2_END_TIME, performanceRecords, serverAddress);
    phase2.start();
    startPhase3.await();


    // phase 3
    Phase phase3 = new Phase(cooldownPhaseThreads, numSkiersPerPhase3Thread,
        numRequestsPerPhase3Thread, phase3RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        null, PHASE3_START_TIME, PHASE3_END_TIME, performanceRecords, serverAddress);
    phase3.start();

    // wait for all threads from all phases to complete
    allPhaseCompleted.await();

    // record the end time
    long end = System.currentTimeMillis();

    long wallTime = (end - start) / 1000;

    // write to csv file
    List<String[]> data = processPerformanceRecords(performanceRecords);
    writeToCsvFile(data, numThreads);

    // generate results
    StatsReport statsReport = new StatsReport(performanceRecords, wallTime, numThreads);
    statsReport.generate();
  }

  private static void writeToCsvFile(List<String[]> data, int numThreads) throws IOException {
    String fileName = numThreads + "_threads_performance.csv";
    File file = new File(fileName);
    FileWriter output = new FileWriter(file);
    CSVWriter csvWriter = new CSVWriter(output);

    csvWriter.writeAll(data);
    csvWriter.close();

  }

  private static List<String[]> processPerformanceRecords(List<PerformanceRecord> data) {

    List<String[]> result = new ArrayList<>();

    // add header
    String[] header = {"start time", "latency", "request type", "status code"};
    result.add(header);

    // add data
    for (PerformanceRecord record : data) {
      String startTime = record.getStartTimeString();
      String latency = record.getLatencyString();
      String requestType = record.getRequestType();
      String statusCode = record.getStatusCodeString();
      result.add(new String[] {startTime, latency, requestType, statusCode});
    }

    return result;

  }


}
