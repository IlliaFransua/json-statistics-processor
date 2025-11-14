package com.fransua;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fransua.service.StatisticCalculatorService;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatisticCalculatorServiceTest {

  private StatisticCalculatorService service;

  @BeforeEach
  public void setUp() {
    service = new StatisticCalculatorService();
  }

  @Test
  public void testIncrementAttributeCount_SingleAttribute() {
    String attributeName = "Classic Cheeseburger";

    service.incrementAttributeCount(attributeName);
    service.incrementAttributeCount(attributeName);

    Map<String, Integer> statistic = service.getStatisticSortedByCountThenByName();

    assertEquals(1, statistic.size());
    assertEquals(2, statistic.get(attributeName));
  }

  @Test
  public void testIncrementAttributeCount_MultipleAttributes() {
    String attributeName1 = "Classic Cheeseburger";
    String attributeName2 = "Double Cheese Deluxe";

    service.incrementAttributeCount(attributeName1);
    service.incrementAttributeCount(attributeName2);
    service.incrementAttributeCount(attributeName1);

    Map<String, Integer> statistic = service.getStatisticSortedByCountThenByName();
    assertEquals(2, statistic.size());
    assertEquals(2, statistic.get(attributeName1));
    assertEquals(1, statistic.get(attributeName2));
  }

  @Test
  public void testIncrementAttributeCount_ThrowExceptionIfAttributeNullOrBlank() {
    assertThrows(IllegalArgumentException.class, () -> service.incrementAttributeCount(null));
    assertThrows(IllegalArgumentException.class, () -> service.incrementAttributeCount(""));
    assertThrows(IllegalArgumentException.class,
        () -> service.incrementAttributeCount(" "));  // space
    assertThrows(IllegalArgumentException.class,
        () -> service.incrementAttributeCount("  ")); // tab

    Map<String, Integer> statistic = service.getStatisticSortedByCountThenByName();

    assertFalse(statistic.containsKey(null));
    assertFalse(statistic.containsKey(""));
    assertFalse(statistic.containsKey(" ")); // space
    assertFalse(statistic.containsKey(" ")); // tab
  }

  @Test
  public void testGetStatisticSortedByCountThenByName_SortsByCountDesc() {
    // A: 1, B: 3, C: 2 -> B, C, A
    service.incrementAttributeCount("A");
    service.incrementAttributeCount("B");
    service.incrementAttributeCount("B");
    service.incrementAttributeCount("B");
    service.incrementAttributeCount("C");
    service.incrementAttributeCount("C");

    Map<String, Integer> statistic = service.getStatisticSortedByCountThenByName();

    String[] expectedList = new String[]{"B", "C", "A"};
    assertArrayEquals(expectedList, statistic.keySet().toArray());

    assertEquals(3, statistic.get("B"));
    assertEquals(2, statistic.get("C"));
    assertEquals(1, statistic.get("A"));
  }

  @Test
  public void testGetStatisticSortedByCountThenByName_SortsByNameAscendingForEqualsCount() {
    // B: 1, C: 2, A: 2 -> A, C, B
    service.incrementAttributeCount("B");
    service.incrementAttributeCount("C");
    service.incrementAttributeCount("C");
    service.incrementAttributeCount("A");
    service.incrementAttributeCount("A");

    Map<String, Integer> statistic = service.getStatisticSortedByCountThenByName();

    String[] expectedList = new String[]{"A", "C", "B"};
    assertArrayEquals(expectedList, statistic.keySet().toArray());

    assertEquals(2, statistic.get("A"));
    assertEquals(2, statistic.get("C"));
    assertEquals(1, statistic.get("B"));
  }

  @Test
  public void testGetStatisticSortedByCountThenByName_EmptyStatic() {
    Map<String, Integer> statistic = service.getStatisticSortedByCountThenByName();
    assertTrue(statistic.isEmpty());
  }

  @Test
  public void testIncrementAttributeCount_ThreadSafety() throws InterruptedException {
    int threadsNumber = 10;
    int incrementsPerThread = 1000;
    String attributeName = "Classic Cheeseburger";

    ExecutorService executor = Executors.newFixedThreadPool(threadsNumber);

    for (int i = 0; i < threadsNumber; ++i) {
      executor.submit(() -> {
        for (int j = 0; j < incrementsPerThread; ++j) {
          service.incrementAttributeCount(attributeName);
        }
      });
    }

    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

    int expectedTotal = threadsNumber * incrementsPerThread;
    Map<String, Integer> statistic = service.getStatisticSortedByCountThenByName();

    assertEquals(expectedTotal, statistic.get(attributeName));
  }
}
