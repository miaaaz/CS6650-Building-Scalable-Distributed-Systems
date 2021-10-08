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

    // should be command line params, hardcode for convenience in this assignment
    int numThreads = 64;
    int numSkiers = 20000;
    int numRuns = 10;  // default 10
    int numLifts = 40;

    int startSkierId = 1;
    List<PerformanceRecord> performanceRecords = new ArrayList<>();

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

    Map<Integer, Count> phase3RequestsCounter = new HashMap<>();
    CountDownLatch allPhaseCompleted = new CountDownLatch(
        startupThreads + peakPhaseThreads + cooldownPhaseThreads);


    // start running phases
    long start = System.currentTimeMillis();


    // phase 1
    Phase phase1 = new Phase(startupThreads, numSkiersPerPhase1Thread,
        numRequestsPerPhase1Thread, phase1RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        startPhase2,
        1, 90, performanceRecords);
    phase1.start();


    startPhase2.await();


    // phase 2
    Phase phase2 = new Phase(peakPhaseThreads, numSkiersPerPhase2Thread,
        numRequestsPerPhase2Thread, phase2RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        startPhase3, PHASE2_START_TIME, PHASE2_END_TIME, performanceRecords);
    phase2.start();
    startPhase3.await();


    // phase 3
    Phase phase3 = new Phase(cooldownPhaseThreads, numSkiersPerPhase3Thread,
        numRequestsPerPhase3Thread, phase3RequestsCounter, startSkierId, numLifts, allPhaseCompleted,
        null, PHASE3_START_TIME, PHASE3_END_TIME, performanceRecords);
    phase3.start();

    // wait for all threads from all phases to complete
//    phase1Completed.await();
//    phase2Completed.await();
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
