import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a directed graph with words as nodes and their transitions as edges.
 */

public class DirectedGraph {
  private static final double DAMPING_FACTOR = 0.85;
  private static final double EPSILON = 0.0001;

  private final Map<String, Map<String, Integer>> adjacencyMap;
  private final Set<String> nodes;

  /**
   * Constructs a directed graph from a text file.
   *
   * @param filePath path to the input text file
   * @throws IOException if there's an error reading the file
   */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public DirectedGraph(String filePath) throws IOException {
    this(filePath, false);
  }

  /**
  * Constructor with test mode option (for unit testing).
  *
  * @param filePath path to the input text file
  * @param isTestMode if true, bypasses some security checks for testing
  * @throws IOException if there's an error reading the file
  */
  @SuppressFBWarnings({"CT_CONSTRUCTOR_THROW", "PATH_TRAVERSAL_IN"})
  public DirectedGraph(String filePath, boolean isTestMode) throws IOException {
    // 1. 输入验证
    if (filePath == null || filePath.isEmpty()) {
      throw new IllegalArgumentException("File path cannot be null or empty");
    }

    // 2. 路径规范化与安全校验
    Path requestedPath = Paths.get(filePath).normalize();

    if (!isTestMode) {
      Path safeBaseDir = Paths.get("").toAbsolutePath(); // 限制为项目根目录
      // 防止路径遍历攻击
      if (!requestedPath.startsWith(safeBaseDir)) {
        throw new SecurityException("Access denied: Potential path traversal attack detected");
      }
    }

    // 3. 安全文件访问
    File file = requestedPath.toFile();
    if (!file.exists()) {
      throw new FileNotFoundException("File not found: " + requestedPath);
    }
    if (!file.isFile()) {
      throw new SecurityException("Path must be a regular file");
    }
    if (!file.canRead()) {
      throw new SecurityException("No read permission for file: " + requestedPath);
    }

    // 4. 初始化数据结构
    this.adjacencyMap = new HashMap<>();
    this.nodes = new HashSet<>();

    // 5. 处理文件内容
    String text = readFileContents(file);
    String processedText = text.replaceAll("[^a-zA-Z]", " ").toLowerCase();
    String[] words = processedText.split("\\s+");

    nodes.addAll(Arrays.stream(words)
            .filter(word -> !word.isEmpty())
            .collect(Collectors.toSet()));

    buildGraph(words);
  }

  /**
   * Alternative constructor that accepts direct text input (for testing).
   *
   * @param words the text content to build the graph from
   */
  public DirectedGraph(List<String> words) {
    this.adjacencyMap = new HashMap<>();
    this.nodes = new HashSet<>();

    nodes.addAll(words.stream()
            .filter(word -> !word.isEmpty())
            .collect(Collectors.toSet()));

    buildGraph(words.toArray(new String[0]));
  }
  /**
   * Visualizes the graph using Graphviz.
   *
   */

  public static void showDirectedGraph(DirectedGraph graph) {
    try {
      String dotFile = "graph.dot";
      String pngFile = "graph.png";
      graph.saveAsDot(dotFile);

      graph.exportToImage(dotFile, pngFile);  // Anti-aliasing

      File imageFile = new File(pngFile);
      if (Desktop.isDesktopSupported() && imageFile.exists()) {
        Desktop.getDesktop().open(imageFile);
      }
    } catch (IOException | InterruptedException e) {
      System.err.println("Visualization failed: " + e.getMessage());
    }
  }

  private String readFileContents(File file) throws IOException {  // 修改为 throws IOException
    StringBuilder textBuilder = new StringBuilder();
    try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8)) {
      while (scanner.hasNextLine()) {
        textBuilder.append(scanner.nextLine().trim()).append(" ");
      }
    }
    return textBuilder.toString();
  }

  private void buildGraph(String[] words) {
    for (int i = 0; i < words.length - 1; i++) {
      String current = words[i];
      String next = words[i + 1];

      Map<String, Integer> edges = adjacencyMap.computeIfAbsent(
              current, new Function<String, Map<String, Integer>>() {
                @Override
                public Map<String, Integer> apply(String k) {
                  return new HashMap<>();
                }
              });
      edges.put(next, edges.getOrDefault(next, 0) + 1);
    }
  }

  /**
   * Determines whether the graph contains the specified word as a node.
   *
   * @param word the word to locate in the graph
   * @return {@code true} if the graph contains the word, {@code false} otherwise
   */
  public boolean containsWord(String word) {
    return !nodes.contains(word);
  }

  /**
   * Finds bridge words between two given words.
   *
   * @param word1 first word
   * @param word2 second word
   * @return list of bridge words between word1 and word2
   */
  public List<String> queryBridgeWords(String word1, String word2) {
    if (!nodes.contains(word1) || !nodes.contains(word2)) {
      return new ArrayList<>();
    }

    List<String> bridges = new ArrayList<>();
    Map<String, Integer> edgesFromWord1 = adjacencyMap.get(word1);
    if (edgesFromWord1 == null) {
      return bridges;
    }

    for (String word3 : edgesFromWord1.keySet()) {
      Map<String, Integer> edgesFromWord3 = adjacencyMap.get(word3);
      if (edgesFromWord3 != null && edgesFromWord3.containsKey(word2)) {
        bridges.add(word3);
      }
    }
    return bridges;
  }

  /**
   * Generates new text by inserting bridge words where possible.
   *
   * @param inputText the input text to process
   * @return the generated text with bridge words inserted
   */
  @SuppressFBWarnings({"DMI_RANDOM_USED_ONLY_ONCE", "PREDICTABLE_RANDOM"})
  public String generateNewText(String inputText) {
    String processedInput = inputText.replaceAll("[^a-zA-Z]", " ").toLowerCase();
    String[] words = processedInput.split("\\s+");
    if (words.length == 0) {
      return "";
    }

    List<String> result = new ArrayList<>();
    result.add(words[0]);

    for (int i = 0; i < words.length - 1; i++) {
      String current = words[i];
      String next = words[i + 1];
      List<String> bridges = queryBridgeWords(current, next);

      if (!bridges.isEmpty()) {
        Random rand = new Random();
        String bridge = bridges.get(rand.nextInt(bridges.size()));
        result.add(bridge);
      }
      result.add(next);
    }
    return String.join(" ", result);
  }

  /**
   * Calculates the shortest path between two words.
   *
   * @param word1 starting word
   * @param word2 ending word
   * @return string describing the path and its length
   */
  public String calculateShortestPath(String word1, String word2) {
    if (!nodes.contains(word1) || !nodes.contains(word2)) {
      return "No such words in the graph.";
    }

    DijkstraResult result = dijkstra(word1);
    Map<String, Double> distances = result.distances;
    Map<String, String> predecessors = result.predecessors;

    if (distances.get(word2) == Double.POSITIVE_INFINITY) {
      return "No path from " + word1 + " to " + word2;
    }

    List<String> path = buildPath(word2, predecessors);
    if (!path.getFirst().equals(word1) || !path.getLast().equals(word2)) {
      return "Path not found.";
    }

    return formatPathOutput(path, distances.get(word2));
  }

  private List<String> buildPath(String end, Map<String, String> predecessors) {
    List<String> path = new ArrayList<>();
    String current = end;
    while (current != null) {
      path.addFirst(current);
      current = predecessors.get(current);
    }
    return path;
  }

  private String formatPathOutput(List<String> path, double distance) {
    StringBuilder sb = new StringBuilder();
    sb.append("Path: ");
    for (int i = 0; i < path.size(); i++) {
      sb.append(path.get(i));
      if (i < path.size() - 1) {
        sb.append(" -> ");
      }
    }
    sb.append("\nLength: ").append(distance);
    return sb.toString();
  }

  /**
  * Calculates the PageRank for a given word.
  *
  * @param word the word to calculate PageRank for
  * @return the PageRank score
  */
  public double calculatePageRank(String word) {
    if (!nodes.contains(word)) {
      return 0.0;
    }

    int nodeCount = nodes.size();
    Map<String, Double> pageRank = initializePageRank(nodeCount);

    for (int i = 0; i < 100; i++) {
      Map<String, Double> newPageRank = new HashMap<>();
      double danglingSum = 0.0;

      for (String node : nodes) {
        double sum = calculateIncomingPageRankSum(node, pageRank);
        newPageRank.put(node, (1 - DAMPING_FACTOR) / nodeCount + DAMPING_FACTOR * sum);

        if (isDanglingNode(node)) {
          danglingSum += pageRank.get(node);
        }
      }

      distributeDanglingRank(newPageRank, nodeCount, danglingSum);

      if (hasConverged(pageRank, newPageRank)) {
        break;
      }
      pageRank = newPageRank;
    }

    return pageRank.get(word);
  }

  private Map<String, Double> initializePageRank(int nodeCount) {
    Map<String, Double> pageRank = new HashMap<>();
    double totalInDegree = calculateTotalInDegree();

    for (String node : nodes) {
      double inDegree = adjacencyMap.values().stream()
              .filter(edges -> edges.containsKey(node))
              .count();
      pageRank.put(node, (inDegree + 1) / (totalInDegree + nodeCount));
    }
    return pageRank;
  }

  private double calculateTotalInDegree() {
    return nodes.stream().mapToDouble(node ->
            adjacencyMap.values().stream()
                    .filter(edges -> edges.containsKey(node))
                    .count()
    ).sum();
  }

  private double calculateIncomingPageRankSum(String node, Map<String, Double> pageRank) {
    double sum = 0.0;
    for (String source : nodes) {
      Map<String, Integer> edges = adjacencyMap.get(source);
      if (edges != null && edges.containsKey(node)) {
        int outDegree = edges.size();
        sum += pageRank.get(source) / outDegree;
      }
    }
    return sum;
  }

  private boolean isDanglingNode(String node) {
    Map<String, Integer> edges = adjacencyMap.get(node);
    return edges == null || edges.isEmpty();
  }

  private void distributeDanglingRank(Map<String, Double> pageRank, int nodeCount,
                                      double danglingSum) {
    if (danglingSum > 0) {
      double danglingContribution = DAMPING_FACTOR * danglingSum / nodeCount;
      for (String node : nodes) {
        pageRank.put(node, pageRank.get(node) + danglingContribution);
      }
    }
  }

  private boolean hasConverged(Map<String, Double> oldRank, Map<String, Double> newRank) {
    double difference = 0.0;
    for (String node : nodes) {
      difference += Math.abs(newRank.get(node) - oldRank.get(node));
    }
    return difference < EPSILON;
  }

  /**
   * Performs a random walk on the graph.
   *
   * @return string representing the path taken
   */
  @SuppressFBWarnings("PREDICTABLE_RANDOM")
  public String randomWalk() {
    List<String> path = new ArrayList<>();
    Random rand = new Random();
    List<String> nodeList = new ArrayList<>(nodes);
    String current = nodeList.get(rand.nextInt(nodeList.size()));
    path.add(current);

    while (true) {
      Map<String, Integer> edges = adjacencyMap.get(current);
      if (edges == null || edges.isEmpty()) {
        break;
      }

      int totalWeight = edges.values().stream().mapToInt(Integer::intValue).sum();
      if (totalWeight == 0) {
        break;
      }

      String nextNode = selectNextNode(edges, totalWeight, rand);
      if (nextNode == null || path.contains(nextNode)) {
        break;
      }

      path.add(nextNode);
      current = nextNode;
    }
    return String.join(" -> ", path);
  }

  private String selectNextNode(Map<String, Integer> edges, int totalWeight, Random rand) {
    int random = rand.nextInt(totalWeight);
    int accumulated = 0;
    for (Map.Entry<String, Integer> entry : edges.entrySet()) {
      accumulated += entry.getValue();
      if (random < accumulated) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * Saves the graph to a DOT format file.
   *
   * @param filename name of the file to save to
   * @throws IOException if there's an error writing the file
   */
  @SuppressFBWarnings({"DM_DEFAULT_ENCODING", "PATH_TRAVERSAL_OUT"})
  public void saveAsDot(String filename) throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
      writer.println("digraph G {");
      writer.println("    rankdir=LR;");
      writer.println("    node [shape=circle, style=filled, color=\"#007BFF\",");
      writer.println("          fontname=\"Arial\", fontsize=12, width=1.2, height=1.2];");
      writer.println("    edge [arrowsize=1.2, color=\"#6C757D\", fontname=\"Arial\",");
      writer.println("          fontsize=10, penwidth=1.2];");
      writer.println("    graph [nodesep=0.8, ranksep=1.2, dpi=300];");

      writeNodes(writer);
      writeEdges(writer);

      writer.println("}");
    }
  }

  private void writeNodes(PrintWriter writer) {
    for (String node : nodes) {
      writer.printf("    \"%s\" [label=\"%s\", xlabel=\"PageRank: %.3f\"]%n",
              node, node, calculatePageRank(node));
    }
  }

  private void writeEdges(PrintWriter writer) {
    adjacencyMap.forEach((source, edges) -> {
      edges.forEach((target, weight) -> {
        writer.printf("    \"%s\" -> \"%s\" [label=\"%d\", labelfloat=true];%n",
                source, target, weight);
      });
    });
  }

  @SuppressFBWarnings({"COMMAND_INJECTION", "DM_DEFAULT_ENCODING", "DM_DEFAULT_ENCODING"})
  private void exportToImage(String dotFile, String outputFile) throws IOException,
          InterruptedException {
    ProcessBuilder pb = new ProcessBuilder(
            "dot",
            "-Gdpi=" + 300,
            "-Gsize=" + (2.0 * 10) + "," + (2.0 * 7.5),
            "-Nfontname=Arial",
            "-Efontname=Arial",
            "-Eantialias=" + true,
            "-Gantialias=" + true,
            "-T" + "png",
            dotFile,
            "-o", outputFile
        );

    Process process = pb.start();
    StringBuilder errorMsg = new StringBuilder();

    Thread errorThread = new Thread(() -> {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getErrorStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          errorMsg.append(line).append("\n");
        }
      } catch (IOException e) {
        errorMsg.append("Error reading error stream: ").append(e.getMessage());
      }
    });
    errorThread.start();

    int exitCode = process.waitFor();
    errorThread.join();

    if (exitCode != 0) {
      throw new IOException("Graphviz execution failed (" + exitCode + "):\n" + errorMsg);
    }
  }

  /**
   * Saves a random walk path to a file.
   *
   * @param filename name of the file to save to
   * @param path the path to save
   * @throws IOException if there's an error writing the file
   */
  @SuppressFBWarnings({"DM_DEFAULT_ENCODING", "PATH_TRAVERSAL_OUT"})
  public void saveWalkToFile(String filename, String path) throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
      writer.println("Random Walk Path:");
      writer.println(path);
    }
  }

  private DijkstraResult dijkstra(String startWord) {
    Map<String, Double> distances = new HashMap<>();
    Map<String, String> predecessors = new HashMap<>();

    initializeDistances(startWord, distances, predecessors);

    PriorityQueue<NodeEntry> queue = new PriorityQueue<>();
    queue.add(new NodeEntry(startWord, 0.0));

    while (!queue.isEmpty()) {
      NodeEntry currentEntry = queue.poll();
      String current = currentEntry.node;
      double currentDist = currentEntry.distance;

      if (currentDist > distances.get(current)) {
        continue;
      }

      relaxEdges(current, currentDist, distances, predecessors, queue);
    }

    return new DijkstraResult(distances, predecessors);
  }

  private void initializeDistances(String startWord, Map<String, Double> distances,
                                   Map<String, String> predecessors) {
    for (String node : nodes) {
      distances.put(node, Double.POSITIVE_INFINITY);
      predecessors.put(node, null);
    }
    distances.put(startWord, 0.0);
  }

  private void relaxEdges(String current, double currentDist,
                          Map<String, Double> distances, Map<String, String> predecessors,
                          PriorityQueue<NodeEntry> queue) {
    Map<String, Integer> edges = adjacencyMap.get(current);
    if (edges != null) {
      for (Map.Entry<String, Integer> edge : edges.entrySet()) {
        String neighbor = edge.getKey();
        int weight = edge.getValue();
        double newDist = currentDist + weight;

        if (newDist < distances.get(neighbor)) {
          distances.put(neighbor, newDist);
          predecessors.put(neighbor, current);
          queue.add(new NodeEntry(neighbor, newDist));
        }
      }
    }
  }

  private record NodeEntry(String node, double distance) implements Comparable<NodeEntry> {

    @Override
    public int compareTo(NodeEntry other) {
      return Double.compare(this.distance, other.distance);
    }
  }

  private record DijkstraResult(Map<String, Double> distances, Map<String, String> predecessors) {
  }
}