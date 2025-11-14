package com.fransua;

import com.fransua.config.ApplicationConfig;
import java.io.File;
import java.nio.file.Path;

public class App {

  public static void main(String[] args) {
    long startTime = System.currentTimeMillis();
    try {
      ApplicationConfig config = parseArguments(args);
      ApplicationRunner runner = new ApplicationRunner(config);
      runner.run();
    } catch (IllegalArgumentException e) {
      System.err.println("Arguments Error: " + e.getMessage());
      System.err.println(
          "Usage: java -jar ... <path/to/json-data/inputDirectory> <attribute-name>");
      System.err.println(
          "Available attribute names: " + ApplicationConfig.availableAttributeNames());
    } catch (Exception e) {
      System.err.println("Error: " + e);
    } finally {
      long endTime = System.currentTimeMillis();
      long durationTime = endTime - startTime;
      System.out.println(
          "Execution with '" + args[1] + "' attribute is completed in " + durationTime
              + " ms");
    }
  }

  private static ApplicationConfig parseArguments(String[] args) {
    if (args.length < 2) {
      throw new IllegalArgumentException("Required 2 arguments");
    }
    File inputDirectory = Path.of(args[0]).toFile();
    String attributeName = args[1];
    if (ApplicationConfig.availableAttributeNames().stream()
        .noneMatch(attributeName::equals)) {
      throw new IllegalArgumentException("Attribute '" + attributeName + "' is not supported");
    }
    return new ApplicationConfig(inputDirectory, attributeName);
  }
}
