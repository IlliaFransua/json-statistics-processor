package com.fransua.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StatisticCalculatorService {

  private final Map<String, Integer> attributeCountStatistic = new ConcurrentHashMap<>();

  public void incrementAttributeCount(String attributeName) {
    if (attributeName == null || attributeName.isBlank()) {
      throw new IllegalArgumentException("Attribute name can't be empty");
    }
    attributeCountStatistic.merge(attributeName, 1, Integer::sum);
  }

  public Map<String, Integer> getStatisticSortedByCountThenByName() {
    return attributeCountStatistic.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
            .thenComparing(Map.Entry.comparingByKey()))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (o1, o2) -> o1,
            LinkedHashMap::new));
  }
}
