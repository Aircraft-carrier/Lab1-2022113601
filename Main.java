import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Main class for interacting with the directed graph.
 */
public class Main {
  /**
  * The main program serves as a functional menu.
  *
  * @param args Input options
  */
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    System.out.print("Enter the file path: ");
    String filePath = scanner.nextLine();

    DirectedGraph graph;
    try {
      graph = new DirectedGraph(filePath);
    } catch (FileNotFoundException e) {
      System.out.println("Error: " + e.getMessage());
      return;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    while (true) {
      printMenu();
      int choice = getIntInput(scanner);

      switch (choice) {
        case 1:
          DirectedGraph.showDirectedGraph(graph);
          break;
        case 2:
          handleBridgeWordsQuery(scanner, graph);
          break;
        case 3:
          handleTextGeneration(scanner, graph);
          break;
        case 4:
          handleShortestPath(scanner, graph);
          break;
        case 5:
          handlePageRank(scanner, graph);
          break;
        case 6:
          handleRandomWalk(scanner, graph);
          break;
        case 7:
          handleSaveGraph(scanner, graph);
          break;
        case 8:
          System.out.println("Exiting...");
          return;
        default:
          System.out.println("Invalid choice. Please select 1-8.");
      }
    }
  }

  private static void printMenu() {
    System.out.println("\nChoose an option:");
    System.out.println("1. Show Directed Graph");
    System.out.println("2. Query Bridge Words - Find intermediates between two words");
    System.out.println("3. Generate New Text - Create text using bridge words");
    System.out.println("4. Calculate Shortest Path - Find path between two words");
    System.out.println("5. Calculate PageRank - PageRank scores for words");
    System.out.println("6. Random Walk - Perform a random walk on the graph");
    System.out.println("7. Save Graph to Dot File - Save graph structure for visualization");
    System.out.println("8. Exit");
  }

  private static int getIntInput(Scanner scanner) {
    System.out.print("Enter choice (1-8): ");
    while (!scanner.hasNextInt()) {
      System.out.println("Invalid input. Please enter a number.");
      scanner.next();
      System.out.print("Enter choice (1-8): ");
    }
    int choice = scanner.nextInt();
    scanner.nextLine(); // Consume newline
    return choice;
  }

  private static void handleBridgeWordsQuery(Scanner scanner, DirectedGraph graph) {
    System.out.print("Enter word1 and word2 (e.g., 'hello world'): ");
    String[] words = scanner.nextLine().split(" ");
    if (words.length != 2) {
      System.out.println("Invalid input. Please enter exactly two words.");
      return;
    }

    List<String> bridges = graph.queryBridgeWords(
            words[0].toLowerCase(),
            words[1].toLowerCase());

    if (bridges.isEmpty()) {
      if (graph.containsWord(words[0]) || graph.containsWord(words[1])) {
        System.out.println("No \"" + words[0] + "\" or \"" + words[1] + "\" in the graph!");
      } else {
        System.out.println("No bridge words from \"" + words[0] + "\" to \"" + words[1] + "\"!");
      }
    } else {
      String result = formatBridgeWordsOutput(bridges);
      System.out.println("The bridge words from \""
              + "\" to \""
              + words[0]
              + words[1]
              + "\" is: "
              + result);
    }
  }

  private static String formatBridgeWordsOutput(List<String> bridges) {
    if (bridges.size() > 1) {
      return String.join(", ", bridges.subList(0, bridges.size() - 1))
              + " and "
              + bridges.getLast();
    }
    return bridges.getFirst();
  }

  private static void handleTextGeneration(Scanner scanner, DirectedGraph graph) {
    System.out.print("Enter text (e.g., 'hello world'): ");
    String inputText = scanner.nextLine();
    System.out.println("Generated text: " + graph.generateNewText(inputText));
  }

  private static void handleShortestPath(Scanner scanner, DirectedGraph graph) {
    System.out.print("Enter word1 and word2 (e.g., 'start end'): ");
    String[] pathWords = scanner.nextLine().split(" ");
    if (pathWords.length != 2) {
      System.out.println("Invalid input. Please enter exactly two words.");
      return;
    }
    System.out.println(graph.calculateShortestPath(
            pathWords[0].toLowerCase(),
            pathWords[1].toLowerCase()));
  }

  private static void handlePageRank(Scanner scanner, DirectedGraph graph) {
    System.out.print("Enter word (e.g., 'example'): ");
    String prWord = scanner.nextLine().toLowerCase();
    System.out.printf("PageRank of '%s': %.4f%n",
            prWord, graph.calculatePageRank(prWord));
  }

  private static void handleRandomWalk(Scanner scanner, DirectedGraph graph) {
    String walkResult = graph.randomWalk();
    System.out.println("Path: " + walkResult);

    System.out.print("Save to file? (y/n): ");
    if (scanner.nextLine().equalsIgnoreCase("y")) {
      System.out.print("Filename (e.g., walk.txt): ");
      String filename = scanner.nextLine();
      try {
        graph.saveWalkToFile(filename, walkResult);
        System.out.println("Saved successfully.");
      } catch (Exception e) {
        System.out.println("Error saving file: " + e.getMessage());
      }
    }
  }

  private static void handleSaveGraph(Scanner scanner, DirectedGraph graph) {
    System.out.print("Enter filename (e.g., graph.dot): ");
    String dotFilename = scanner.nextLine();
    try {
      graph.saveAsDot(dotFilename);
      System.out.println("Graph saved to " + dotFilename);
    } catch (Exception e) {
      System.out.println("Error saving file: " + e.getMessage());
    }
  }
}