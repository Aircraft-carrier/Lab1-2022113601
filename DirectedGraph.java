import java.awt.Desktop;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DirectedGraph {
    private static final double D = 0.85;
    private Map<String, Map<String, Integer>> adjacencyMap;
    private Set<String> nodes;
    private Map<String, Integer> outDegrees;
    private static final double EPSILON = 0.0001;

    public DirectedGraph(String filePath) throws FileNotFoundException {
        adjacencyMap = new HashMap<>();
        nodes = new HashSet<>();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        StringBuilder textBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                textBuilder.append(scanner.nextLine().trim()).append(" ");
            }
        }

        String text = textBuilder.toString();
        String processedText = text.replaceAll("[^a-zA-Z]", " ").toLowerCase();
        String[] words = processedText.split("\\s+");
        nodes = Arrays.stream(words).filter(word -> !word.isEmpty()).collect(Collectors.toSet());

        outDegrees = new HashMap<>();
        for (int i = 0; i < words.length - 1; i++) {
            String current = words[i];
            String next = words[i + 1];
            Map<String, Integer> edges = adjacencyMap.get(current);
            if (edges == null) {
                edges = new HashMap<>();
                adjacencyMap.put(current, edges);
            }
            edges.put(next, edges.getOrDefault(next, 0) + 1);
            outDegrees.put(current, edges.size());
        }
    }
//    查看是否包含
    public boolean containsWord(String word) {
        return nodes.contains(word);
    }

    public List<String> queryBridgeWords(String word1, String word2) {
        if (!nodes.contains(word1) || !nodes.contains(word2)) return new ArrayList<>();
        List<String> bridges = new ArrayList<>();
        Map<String, Integer> edgesFromWord1 = adjacencyMap.get(word1);
        if (edgesFromWord1 == null) return bridges;
        for (String word3 : edgesFromWord1.keySet()) {
            Map<String, Integer> edgesFromWord3 = adjacencyMap.get(word3);
            if (edgesFromWord3 != null && edgesFromWord3.containsKey(word2)) {
                bridges.add(word3);
            }
        }
        return bridges;
    }

    public String generateNewText(String inputText) {
        String processedInput = inputText.replaceAll("[^a-zA-Z]", " ").toLowerCase(); // 移除非字母字符（标点、数字等），替换为空格
        String[] words = processedInput.split("\\s+"); // 按空格分割成单词数组
        if (words.length == 0) return "";
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

    private static class NodeEntry implements Comparable<NodeEntry> {
        String node;
        double distance;

        public NodeEntry(String node, double distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeEntry other) {
            return Double.compare(this.distance, other.distance);
        }
    }

    private class DijkstraResult {
        Map<String, Double> distances;
        Map<String, String> predecessors;

        public DijkstraResult(Map<String, Double> distances, Map<String, String> predecessors) {
            this.distances = distances;
            this.predecessors = predecessors;
        }
    }
    // 单源最短路径，总是从最小堆中取出到源节点距离最短的节点，进行扩展
    // 直到找到目标节点，扩展过程中每次查询一个结点的时候说明当前节点已经找到最短路径
    // 保存前序节点最后输出
    private DijkstraResult dijkstra(String startWord) {
        Map<String, Double> dist = new HashMap<>();  // 存储从startWord到每个节点的最短距离
        Map<String, String> prev = new HashMap<>();  // 存储每个节点在最短路径上的前驱节点
        for (String node : nodes) {
            dist.put(node, Double.POSITIVE_INFINITY);
            prev.put(node, null);
        }
        dist.put(startWord, 0.0);

        // 使用优先队列（最小堆）来选择当前距离最小的节点
        PriorityQueue<NodeEntry> queue = new PriorityQueue<>();
        queue.add(new NodeEntry(startWord, 0.0));

        while (!queue.isEmpty()) {
            // 取出当前距离最小的节点
            NodeEntry currentEntry = queue.poll();
            String current = currentEntry.node;
            double currentDist = currentEntry.distance;

            if (currentDist > dist.get(current)) continue;
            // 获取当前节点的所有邻居节点和边的权重
            Map<String, Integer> edges = adjacencyMap.get(current);
            if (edges != null) {
                for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                    String neighbor = edge.getKey();
                    int weight = edge.getValue();
                    // 计算通过当前节点到达邻居节点的距离
                    double newDist = currentDist + weight;
                    // 如果找到更短的路径，更新距离和前驱节点
                    if (newDist < dist.get(neighbor)) {
                        dist.put(neighbor, newDist);
                        prev.put(neighbor, current);
                        queue.add(new NodeEntry(neighbor, newDist));
                    }
                }
            }
        }
        return new DijkstraResult(dist, prev);
    }

    public String calcShortestPath(String word1, String word2) {
        if (!nodes.contains(word1) || !nodes.contains(word2)) return "No such words in the graph.";
        DijkstraResult result = dijkstra(word1);
        Map<String, Double> dist = result.distances;
        Map<String, String> prev = result.predecessors;

        if (dist.get(word2) == Double.POSITIVE_INFINITY) return "No path from " + word1 + " to " + word2;

        List<String> path = new ArrayList<>();
        String current = word2;
        while (current != null) {
            path.add(0, current);
            current = prev.get(current);
        }

        if (!path.get(0).equals(word1) || !path.get(path.size() - 1).equals(word2)) return "Path not found.";

        StringBuilder sb = new StringBuilder();
        sb.append("Path: ");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) sb.append(" -> ");
        }
        sb.append("\nLength: ").append(dist.get(word2));
        return sb.toString();
    }


    // 初始PR值按入度比例加权分配，进入迭代
    public double calPageRank(String word) {
        if (!nodes.contains(word)) return 0.0;

        int N = nodes.size();
        Map<String, Double> pr = new HashMap<>();
        // 初始PR值均匀分配
        // 初始PR值加权分配（示例：按入度比例）
        double totalInDegree = nodes.stream().mapToDouble(node ->
                adjacencyMap.values().stream()
                        .filter(edges -> edges.containsKey(node))
                        .count()
        ).sum();

        for (String node : nodes) {
            double inDegree = adjacencyMap.values().stream()
                    .filter(edges -> edges.containsKey(node))
                    .count();
            // 平滑处理：避免入度为0的节点初始PR为0
            pr.put(node, (inDegree + 1) / (totalInDegree + N));
        }

        for (int i = 0; i < 100; i++) {
            // 外层循环：进行100次PageRank迭代计算
            Map<String, Double> newPr = new HashMap<>();
            double danglingSum = 0.0; // 收集出度为0的节点的PR值

            for (String node : nodes) {
                // 内层循环1：遍历图中的所有节点
                double sum = 0.0; // 累积
                // 计算所有指向当前节点的节点的贡献值
                // d * Σ(PR(v)/L(v))，其中v∈Bu(Bu是指向node的所有节点)
                for (String B : nodes) {
                    Map<String, Integer> edgesOfB = adjacencyMap.get(B);
                    if (edgesOfB != null && edgesOfB.containsKey(node)) {
                        int outDegreeB = edgesOfB.size();  // L(v) - 节点v的出度
                        if (outDegreeB == 0) continue;
                        sum += pr.get(B) / outDegreeB;    // 累加PR(v)/L(v)
                    }
                }
                // PageRank核心公式:
                // PR(u) = (1-d)/N + d * Σ(PR(v)/L(v))
                // 其中:
                //   - PR(u): 当前节点(node)的PageRank值
                //   - N: 图中节点总数(nodes.size())
                //   - d: 阻尼因子(通常设为0.85)
                //   - Σ(PR(v)/L(v)): 所有指向node的节点的PR值除以其出度的和(上面计算的sum)

                newPr.put(node, (1 - D)/N + D * sum);

                // 检查当前节点是否是出度为0的节点
                Map<String, Integer> edges = adjacencyMap.get(node);
                if (edges == null || edges.isEmpty()) {
                    danglingSum += pr.get(node);
                }
            }

            // 将出度为0的节点的PR值均分给所有节点
            if (danglingSum > 0) {
                double danglingContribution = D * danglingSum / N;
                for (String node : nodes) {
                    newPr.put(node, newPr.get(node) + danglingContribution);
                }
            }

            // 检查收敛条件
            double difference = 0.0;
            for (String node : nodes) {
                difference += Math.abs(newPr.get(node) - pr.get(node));
            }
            if (difference < EPSILON) break;

            pr = newPr;
        }

        return pr.get(word);
    }

    public String randomWalk() {
        List<String> path = new ArrayList<>();
        Random rand = new Random();
        List<String> nodeList = new ArrayList<>(nodes);
        String current = nodeList.get(rand.nextInt(nodeList.size()));
        path.add(current);

        while (true) {
            Map<String, Integer> edges = adjacencyMap.get(current);
            if (edges == null || edges.isEmpty()) break;

            int totalWeight = edges.values().stream().mapToInt(Integer::intValue).sum();
            if (totalWeight == 0) break;

            int random = rand.nextInt(totalWeight);
            int accumulated = 0;
            String nextNode = null;
            for (Map.Entry<String, Integer> entry : edges.entrySet()) {
                accumulated += entry.getValue();
                if (random < accumulated) {
                    nextNode = entry.getKey();
                    break;
                }
            }

            if (nextNode == null) break;
            if (path.contains(nextNode)) break;
            path.add(nextNode);
            current = nextNode;
        }
        return String.join(" -> ", path);
    }

    public static void showDirectedGraph(DirectedGraph G) {
        try {
            String dotFile = "graph.dot";
          String pngFile = "graph.png";
            G.saveAsDot(dotFile);

            // 新增高清参数
            G.exportToImage(dotFile, pngFile, "png",
                    300,    // DPI
                    2.0,    // 缩放因子
                    true);  // 抗锯齿

            File imageFile = new File(pngFile);
            if (Desktop.isDesktopSupported() && imageFile.exists()) {
                Desktop.getDesktop().open(imageFile);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("可视化失败: " + e.getMessage());
        }
    }

    public void saveAsDot(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("digraph G {");
            // 增强样式参数
            writer.println("    rankdir=LR;");
            writer.println("    node [shape=circle, style=filled, color=\"#007BFF\",");
            writer.println("          fontname=\"Arial\", fontsize=12, width=1.2, height=1.2];");
            writer.println("    edge [arrowsize=1.2, color=\"#6C757D\", fontname=\"Arial\",");
            writer.println("          fontsize=10, penwidth=1.2];");
            writer.println("    graph [nodesep=0.8, ranksep=1.2, dpi=300];");

            // 声明节点时添加额外属性
            for (String node : nodes) {
                writer.printf("    \"%s\" [label=\"%s\", xlabel=\"PageRank: %.3f\"];\n",
                        node, node, calPageRank(node));
            }

            // 边关系定义
            adjacencyMap.forEach((source, edges) -> {
                edges.forEach((target, weight) -> {
                    writer.printf("    \"%s\" -> \"%s\" [label=\"%d\", labelfloat=true];\n",
                            source, target, weight);
                });
            });
            writer.println("}");
        }
    }

    private void exportToImage(String dotFile, String outputFile, String format,
                               int dpi, double scale, boolean antialias)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "dot",
                "-Gdpi=" + dpi,
                "-Gsize=" + (scale * 10) + "," + (scale * 7.5), // 控制画布尺寸
                "-Nfontname=Arial",
                "-Efontname=Arial",
                "-Eantialias=" + antialias,
                "-Gantialias=" + antialias,
                "-T" + format,
                dotFile,
                "-o", outputFile
        );

        // 错误处理增强
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
            throw new IOException("Graphviz执行失败 (" + exitCode + "):\n" + errorMsg);
        }
    }

    public void saveWalkToFile(String filename, String path) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Random Walk Path:");
            writer.println(path);
        }
    }

}

class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the file path: ");
        String filePath = scanner.nextLine();
        DirectedGraph graph;
        try {
            graph = new DirectedGraph(filePath);
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Show Directed Graph");
            System.out.println("2. Query Bridge Words - Find intermediates between two words");
            System.out.println("3. Generate New Text - Create text using bridge words");
            System.out.println("4. Calculate Shortest Path - Find path between two words");
            System.out.println("5. Calculate PageRank - PageRank scores for words");
            System.out.println("6. Random Walk - Perform a random walk on the graph");
            System.out.println("7. Save Graph to Dot File - Save graph structure for visualization");
            System.out.println("8. Exit");
            System.out.print("Enter choice (1-8): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    DirectedGraph.showDirectedGraph(graph);
                    break;
                case 2:
                    System.out.print("Enter word1 and word2 (e.g., 'hello world'): ");
                    String[] words = scanner.nextLine().split(" ");
                    if (words.length != 2) {
                        System.out.println("Invalid input. Please enter exactly two words.");
                    } else {
                        List<String> bridges = graph.queryBridgeWords(words[0].toLowerCase(), words[1].toLowerCase());
                        if (bridges.isEmpty()) {
                            // Check if either of the words is not in the graph
                            if (!graph.containsWord(words[0]) || !graph.containsWord(words[1])) {
                                System.out.println("No \"" + words[0] + "\" or \"" + words[1] + "\" in the graph!");
                            } else {
                                System.out.println("No bridge words from \"" + words[0] + "\" to \"" + words[1] + "\"!");
                            }
                        } else {
                            String result;
                            if (bridges.size() > 1) {
                                result = String.join(", ", bridges.subList(0, bridges.size() - 1)) + " and " + bridges.get(bridges.size() - 1);
                            } else {
                                result = bridges.get(0);
                            }
                            System.out.println("The bridge words from " + words[0] + " to " + words[1] + " are: " + result);
                        }
                    }
                    break;
                case 3:
                    System.out.print("Enter text (e.g., 'hello world'): ");
                    String inputText = scanner.nextLine();
                    System.out.println("Generated text: " + graph.generateNewText(inputText));
                    break;
                case 4:
                    System.out.print("Enter word1 and word2 (e.g., 'start end'): ");
                    String[] pathWords = scanner.nextLine().split(" ");
                    if (pathWords.length != 2) {
                        System.out.println("Invalid input. Please enter exactly two words.");
                        break;
                    }
                    System.out.println(graph.calcShortestPath(pathWords[0].toLowerCase(), pathWords[1].toLowerCase()));
                    break;
                case 5:
                    System.out.print("Enter word (e.g., 'example'): ");
                    String prWord = scanner.nextLine().toLowerCase();
                    System.out.printf("PageRank of '%s': %.4f%n", prWord, graph.calPageRank(prWord));
                    break;
                case 6:
                    String walkResult = graph.randomWalk();
                    System.out.println("Path: " + walkResult);
                    System.out.print("Save to file? (y/n): ");
                    if (scanner.nextLine().equalsIgnoreCase("y")) {
                        System.out.print("Filename (e.g., walk.txt): ");
                        String filename = scanner.nextLine();
                        try {
                            graph.saveWalkToFile(filename, walkResult);
                            System.out.println("Saved successfully.");
                        } catch (IOException e) {
                            System.out.println("Error saving file: " + e.getMessage());
                        }
                    }
                    break;
                case 7:
                    System.out.print("Enter filename (e.g., graph.dot): ");
                    String dotFilename = scanner.nextLine();
                    try {
                        graph.saveAsDot(dotFilename);
                        System.out.println("Graph saved to " + dotFilename);
                    } catch (IOException e) {
                        System.out.println("Error saving file: " + e.getMessage());
                    }
                    break;
                case 8:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please select 1-8.");
            }
        }
    }
}