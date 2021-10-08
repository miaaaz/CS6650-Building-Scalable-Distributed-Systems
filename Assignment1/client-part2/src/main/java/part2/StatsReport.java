package part2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsReport {

  private List<PerformanceRecord> dataToProcess;
  private int numThreads;
  private long wallTime;  // in seconds
  private int numRequests;
  private List<Long> sortedLatencyList;
//  private int meanResponseTime;
//  private int medianResponseTime;
//  private int throughput;
//  private int p99ResponseTime;
//  private int maxResponseTime;

  public StatsReport(List<PerformanceRecord> dataToProcess, long wallTime, int numThreads) {
    this.dataToProcess = dataToProcess;
    this.wallTime = wallTime;
    this.numThreads = numThreads;
    this.numRequests = dataToProcess.size();
    this.sortedLatencyList = getSortedListOfResponseTime();
  }

  public void generate() {
    long meanResponseTime = calculateMeanResponseTime();
    long medianResponseTime = calculateMedianResponseTime();
    long maxResponseTime = getMaxResponseTime();
    long p99ResponseTime = getP99ResponseTime();
    int throughput = (int) (this.numRequests / this.wallTime);

    System.out.println("-------------------------------------------");
    System.out.println("Number of Threads: " + this.numThreads);
    System.out.println("Mean response time (millisecs): " + meanResponseTime + " ms");
    System.out.println("Median response time (millisecs): " + medianResponseTime + " ms");
    System.out.println("P99 response time (millisecs): " + p99ResponseTime + " ms");
    System.out.println("Max response time (millisecs): " + maxResponseTime + " ms");
    System.out.println("Wall time: " + this.wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " seconds");
  }


  private long calculateMeanResponseTime() {
    long totalTime = 0;
    for (PerformanceRecord record : this.dataToProcess) {
      totalTime += record.getLatency();
    }
    return totalTime / this.numRequests;
  }

  private long calculateMedianResponseTime() {
    if (this.numRequests % 2 != 0) {
      return this.sortedLatencyList.get(this.numRequests / 2);
    }
    long firstNum = this.sortedLatencyList.get((this.numRequests - 1) / 2);
    long secondNum = this.sortedLatencyList.get(this.numRequests / 2);
    return (firstNum + secondNum) / 2;
  }

  private long getMaxResponseTime() {
    return this.sortedLatencyList.get(this.numRequests - 1);
  }

  private long getP99ResponseTime() {
    final double PERCENTILE_99 = 0.99;
    return this.sortedLatencyList.get((int) (this.numRequests * PERCENTILE_99));
  }

  private List<Long> getSortedListOfResponseTime() {
    List<Long> result = new ArrayList<>();
    for (PerformanceRecord record : this.dataToProcess) {
      result.add(record.getLatency());
    }
    Collections.sort(result);
    return result;
  }
}
