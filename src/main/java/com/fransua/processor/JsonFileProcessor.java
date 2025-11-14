package com.fransua.processor;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fransua.config.ApplicationConfig;
import com.fransua.model.Ingredient;
import com.fransua.model.Order;
import com.fransua.service.StatisticCalculatorService;
import java.io.File;
import java.math.BigDecimal;
import java.util.concurrent.Callable;

public record JsonFileProcessor(
    ApplicationConfig config,
    File file,
    StatisticCalculatorService statisticService) implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    int processedOrders = 0;
    ObjectReader reader = ApplicationConfig.objectReader();

    try (MappingIterator<Order> iterator = reader.readValues(file)) {
      while (iterator.hasNext()) {
        Order order = iterator.next();
        ++processedOrders;
        processOrder(order);
      }
    }

    return processedOrders;
  }

  private void processOrder(Order order) {
    switch (config.attributeName().toLowerCase()) {
      case "burger-name" -> {
        order.burgers().forEach(burger ->
            statisticService.incrementAttributeCount(burger.name()));
      }

      case "ingredient-name" -> {
        order.burgers().stream()
            .flatMap(burger -> burger.ingredients().stream())
            .map(Ingredient::name)
            .forEach(statisticService::incrementAttributeCount);
      }

      case "ingredient-price" -> {
        order.burgers().stream()
            .flatMap(burger -> burger.ingredients().stream())
            .map(Ingredient::unitPrice)
            .map(BigDecimal::toString)
            .forEach(statisticService::incrementAttributeCount);
      }
    }
  }
}
