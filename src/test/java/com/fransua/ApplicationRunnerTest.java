package com.fransua;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ApplicationRunnerTest {

  private Path tmpDirectory;
  private ApplicationRunner applicationRunner;

  @BeforeEach
  public void setUpTmpDirectory() throws IOException {
    tmpDirectory = Files.createTempDirectory("test_dir");
    applicationRunner = null;

    Files.createFile(tmpDirectory.resolve("order_root_1.json"));
    Files.createFile(tmpDirectory.resolve("ignore.txt"));
    Files.createFile(tmpDirectory.resolve("order_root_2.JSON"));

    Path subDir1 = Files.createDirectory(tmpDirectory.resolve("data_x"));
    Files.createFile(subDir1.resolve("order_a_1.json"));
    Files.createFile(subDir1.resolve("report.xml"));

    Path subDir2 = Files.createDirectory(tmpDirectory.resolve("data_y"));
    Files.createFile(subDir2.resolve("order_b_1.json"));
    Files.createFile(subDir2.resolve("order_b_2.JSON"));

    Files.createDirectory(tmpDirectory.resolve("empty_dir"));
  }

  @AfterEach
  public void cleanupTmpDirectory() throws IOException {
    try (Stream<Path> pathStream = Files.walk(tmpDirectory)) {
      pathStream.sorted(Comparator.reverseOrder())
          .map(Path::toFile)
          .forEach(File::delete);
    }
  }

  @Test
  public void testFindJsonFiles_RecursivelyFindJsonFiles() {
    List<File> foundFiles = ApplicationRunner.findJsonFiles(tmpDirectory);

    assertEquals(5, foundFiles.size());

    List<String> foundNames = foundFiles.stream()
        .map(File::getName)
        .toList();

    assertFalse(foundNames.contains("ignore.txt"));
    assertFalse(foundNames.contains("report.xml"));

    assertTrue(foundNames.contains("order_root_1.json"));
    assertTrue(foundNames.contains("order_root_2.JSON"));
    assertTrue(foundNames.contains("order_a_1.json"));
    assertTrue(foundNames.contains("order_b_1.json"));
    assertTrue(foundNames.contains("order_b_2.JSON"));
  }
}
