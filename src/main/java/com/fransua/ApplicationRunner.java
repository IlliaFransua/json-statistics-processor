package com.fransua;

import com.fransua.config.ApplicationConfig;
import com.fransua.processor.JsonFileProcessor;
import com.fransua.service.StatisticCalculatorService;
import com.fransua.service.XmlReporterService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class ApplicationRunner {

  private final ApplicationConfig config;
  private final StatisticCalculatorService statisticService;

  public ApplicationRunner(ApplicationConfig config) {
    this.config = config;
    this.statisticService = new StatisticCalculatorService();
  }

  public void run() throws Exception {
    List<File> jsonFiles = findJsonFiles(config.inputDirectory().toPath());

    if (jsonFiles.isEmpty()) {
      throw new IllegalArgumentException("Json files in directory are required");
    }

    processJsonFiles(jsonFiles);
    XmlReporterService.createReport(statisticService.getStatisticSortedByCountThenByName(),
        config.attributeName());
  }

  private void processJsonFiles(List<File> jsonFiles) {
    ExecutorService executorService = ApplicationConfig.executorService();

    List<Future<Integer>> futures = new ArrayList<>();
    for (File file : jsonFiles) {
      JsonFileProcessor processor = new JsonFileProcessor(config, file, statisticService);
      futures.add(executorService.submit(processor));
    }

    printProcessedOrdersSum(futures);
    executorService.shutdown();
  }

  private void printProcessedOrdersSum(List<Future<Integer>> futures) {
    int sum = 0;
    for (Future<Integer> future : futures) {
      try {
        sum += future.get();
      } catch (InterruptedException | ExecutionException e) {
        System.err.println("Error summing processed orders: " + e);
      }
    }
    if (sum != 0) {
      System.out.println("Orders processed: " + sum);
    }
  }

  public static List<File> findJsonFiles(Path directory) {
    try (Stream<Path> pathStream = Files.walk(directory)) {
      return pathStream
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".json"))
          .map(Path::toFile)
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
