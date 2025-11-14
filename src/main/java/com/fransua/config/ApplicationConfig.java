package com.fransua.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fransua.model.Order;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public record ApplicationConfig(File inputDirectory, String attributeName) {

  public static ExecutorService executorService() {
    return Executors.newCachedThreadPool();
  }

  public static List<String> availableAttributeNames() {
    return List.of("burger-name", "ingredient-name", "ingredient-price");
  }

  public static String statisticDirectoryName() {
    return "statistic";
  }

  public static String getStatisticFileNameFor(String attributeName) {
    return "statistic_by_" + attributeName + ".xml";
  }

  public static ObjectReader objectReader() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    return mapper.readerFor(Order.class);
  }

}
