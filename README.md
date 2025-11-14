# Json Statistics Processor 📊

For a better understanding, I recommend opening the project code in parallel with reading the README file. The following information about the program's key components and logic is presented below.

## Project Structure

- **App** (`main`): The entry point.
- **ApplicationConfig**: Storage for configuration and passed arguments.
- **ApplicationRunner**: Coordinates JSON processing and statistics collection.
- **JsonFileProcessor**: Processes a single JSON file in a separate thread.
- **StatisticCalculatorService**: A service for thread-safe attribute counting.
- **XmlReporterService**: Generates the final XML report.
- **Domain Models**: `Ingredient`, `Burger`, `Order`.

## Core Program Logic

### 1. Program Startup (`App`)

Program execution starts with the `main` method in the `App` class. It is responsible for **validating the input arguments** (`args`):

1. It checks for the presence of exactly two arguments: the **path to the directory** containing JSON files and the **attribute name** for statistics collection (for example, for counting orders that include the ingredient "Swiss Cheese").
2. It verifies whether the passed attribute name is included in the list of **allowed attributes** in `ApplicationConfig`.
3. In case of success, `App` initializes `ApplicationRunner`, passing it the `ApplicationConfig` object, which contains the directory path and the attribute name.

> `ApplicationConfig` is a general configuration store (settings for multi-threading via `ExecutorService`, the list of allowed attributes, the name of the reports directory, etc.). It also stores the passed arguments for easy access by other classes.

### 2. File Processing and Statistics Collection (`ApplicationRunner`)

The main process takes place in the `ApplicationRunner` class:

1. The `findJsonFiles` method **recursively traverses** the passed directory. It filters the found paths, keeping only files with the `.json` or `.JSON` extension.
2. If the list of files is not empty, a separate **thread** is allocated for each found file in the `ExecutorService`.
3. Each task is represented by a `JsonFileProcessor` object, which implements the `Callable<Integer>` interface. This allows the system to:

* **Retrieve** the total number of processed orders from each file.
* **Wait** for all threads to complete (by calling `get()` on the `Future<Integer>`).

> **Queue Handling:** Regardless of the chosen `ExecutorService` implementation, if the number of JSON files exceeds the maximum number of available threads, the extra tasks are automatically placed in a processing queue.

### 3. JSON File Processing (`JsonFileProcessor`)

`JsonFileProcessor` implements the `Callable<Integer>` interface, which allows it to return an `Integer` value (in the context of this application, this represents the count of processed orders).

1. `ObjectReader` is used for data reading, which the `JsonFileProcessor` retrieves from `ApplicationConfig`.
2. To efficiently process large files and avoid loading the entire file into memory, `ObjectReader` provides a `MappingIterator`.
3. The iterator allows the system to read and process **each `Order` sequentially**, releasing memory after each object has been handled.
4. The `processOrder` method implements the statistics collection logic: it determines which attribute (burger name, ingredient name, or ingredient price) should be accounted for and calls the increment method in the thread-safe `StatisticCalculatorService`.

## Testing 🧪

To confirm the quality of the project's core functionality, unit tests have been implemented for the **file parsing logic** (`ApplicationRunnerTest`) and **statistics generation** (`StatisticCalculatorServiceTest`).

## Unit Testing

### 1. Testing File Parsing Logic

The `ApplicationRunnerTest` class verifies the correctness of the recursive JSON file search (`findJsonFiles`).

* **Setup (`setUpTmpDirectory`):** A temporary, multi-level directory structure is created with JSON files at different levels of nesting.
* **Test (`testFindJsonFiles_RecursivelyFindJsonFiles`):** The recursive parsing method is executed. The result is compared against the expected list of paths, checking that all necessary files (`.json`/`.JSON`) are found and unnecessary ones are ignored.
* **Cleanup (`cleanupTmpDirectory`):** All created temporary files and directories are deleted.

### 2. Testing of Statistics Generation

The `StatisticCalculatorServiceTest` class verifies the following:

* The correctness of counter increments for various attributes.
* The proper sorting of statistics (by count in descending order, then by attribute name in ascending order).
* The **thread-safety** of the incrementing method, confirming that all multi-threading tests pass successfully thanks to the use of `ConcurrentHashMap`.

## Performance and Multi-threading Testing

Performance testing of the application was conducted on a test dataset using various thread pool configurations.

```java
public class AppTest {

  private final static String datasetDirName = "test_dataset";

  @Test
  public void testDatasets() throws IOException {
    for (String attributeName : ApplicationConfig.availableAttributeNames()) {
      String[] args = new String[]{datasetDirName, attributeName};
      App.main(args);
    }
  }

  @BeforeAll
  public static void createDataset() {
    DatasetUtil.createTestDataset(datasetDirName);
  }

  @AfterAll
  public static void deleteDataset() {
    DatasetUtil.deleteTestDataset(datasetDirName);
  }
}
```

The testing was performed by calling `App.main()` for all available attributes (`burger-name`, `ingredient-name`, `ingredient-price`) using the test dataset (`test_dataset`).

> **Important:** Execution with the `burger-name` attribute consistently takes longer. This is because, being the first element processed, it takes over the **"warm-up"** time of the **Java Virtual Machine (JVM)**, which requires time for code compilation and optimization.

| **ExecutorService Configuration** | **burger-name (before "warm-up") (ms)** | **ingredient-name (ms)** | **ingredient-price (ms)** | **burger-name (after "warm-up") (ms)** |
|-----------------------------------|-----------------------------------------|--------------------------|---------------------------|----------------------------------------|
| `newFixedThreadPool(2)`           | 205                                     | 11                       | 10                        | 6                                      |
| `newFixedThreadPool(4)`           | 191                                     | 9                        | 8                         | 5                                      |
| `newFixedThreadPool(8)`           | 190                                     | 8                        | 7                         | 4                                      |
| `newFixedThreadPool(10)`          | 190                                     | 7                        | 7                         | 5                                      |
| `newCachedThreadPool()`           | 194                                     | 9                        | 9                         | 5                                      |

### Performance Conclusions

1. **JVM "Warm-up" Effect:** The time difference in execution between `burger-name` (the first run) and the remaining attributes (`ingredient-name`, `ingredient-price`) clearly demonstrates the initial JVM warm-up.
2. **Scaling:** Increasing the thread pool from **2 to 8** yielded a noticeable improvement: the time for `burger-name` dropped from 205 ms to 190 ms. Further increasing the pool to **10 threads** did not provide significant gain, which may indicate that the bottleneck shifted to the **I/O speed (file reading)** or the **maximum speed of the processor**.
3. `CachedThreadPool`: The `newCachedThreadPool` showed results comparable to mid-sized fixed pools (4–8 threads), which is logical since it creates an optimal number of threads on demand.

## Additional Information

The project root contains the directories `statistic` and `test_dataset`. The `test_dataset` directory is used to demonstrate the application's operation and as a basis for automatic testing examples.

### Running and Demonstration

To execute the program in the terminal and demonstrate the report generation, use the following commands:

```bash
git clone https://github.com/IlliaFransua/json-statistics-processor
cd ./json-statistics-processor

mvn clean compile package
rm -rf ./statistic

java -jar target/json-statistics-processor-1.0-SNAPSHOT-jar-with-dependencies.jar "test_dataset" "burger-name"
java -jar target/json-statistics-processor-1.0-SNAPSHOT-jar-with-dependencies.jar "test_dataset" "ingredient-name"
java -jar target/json-statistics-processor-1.0-SNAPSHOT-jar-with-dependencies.jar "test_dataset" "ingredient-price"
```

After executing these commands, a `statistic` directory will be created, containing three generated files with statistics in the `.xml` format.

### Manual Statistics Verification

To manually confirm the correctness of the collected statistics, you can follow this process:

1. Open a file, for example, `statistic_by_ingredient-name.xml`.
2. Find an element of interest, for instance:
   ```xml
   <item>  
     <ingredientname>Brioche Bun</ingredientname>  
     <count>21</count>  
   </item>
   ```
3. Copy the attribute's value (`Brioche Bun`).
4. Use a content search function (e.g., `ctrl+f` or `cmd+f`) across all files in the `test_dataset` directory to find all occurrences of this ingredient.
5. Sum its count in each file:

* `orders_part_1.json`: 7
* `orders_part_2.json`: 8
* `orders_part_3.json`: 6
* **Total:** $7 + 8 + 6 = 21$.

6. The resulting sum of **21** matches the `<count>21</count>` value in the report, confirming the correct collection of statistics for the ingredient `Brioche Bun`.

### Limitations of Automated Testing

> Full automation of **End-to-End testing** (running `App` –\> validating output XML –\> comparing with initial JSON) would require **duplicate parsing functionality**. Since the developer has already implemented one working JSON parser, creating a second, independent parser solely for testing is **redundant** and an irrational use of resources. Therefore, to verify the final result, a **semi-automated** approach using manual verification of the output XML files was chosen.