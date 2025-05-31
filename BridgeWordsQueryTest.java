import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.*;

public class BridgeWordsQueryTest {

  private DirectedGraph graph;

  @BeforeEach
  void setUp() throws Exception {
    // 构建基础测试图：hello -> world, hello -> java, java -> world
    List<String> words = Arrays.asList("hello", "world", "java", "hello");
    graph = new DirectedGraph(words);

    // 设置完整的邻接表
    Map<String, Map<String, Integer>> adjacencyMap = new HashMap<>();

    // hello的出边
    Map<String, Integer> helloEdges = new HashMap<>();
    helloEdges.put("world", 1);
    helloEdges.put("java", 1);
    adjacencyMap.put("hello", helloEdges);

    // java的出边
    Map<String, Integer> javaEdges = new HashMap<>();
    javaEdges.put("world", 1);
    adjacencyMap.put("java", javaEdges);

    // world没有出边（测试用）
    adjacencyMap.put("world", new HashMap<>());

    setPrivateField(graph, "adjacencyMap", adjacencyMap);

    // 设置节点集合
    Set<String> nodes = new HashSet<>(Arrays.asList("hello", "world", "java"));
    setPrivateField(graph, "nodes", nodes);
  }

  // 辅助方法：设置私有字段
  private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }

  // 辅助方法：获取私有字段
  private Object getPrivateField(Object obj, String fieldName) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(obj);
  }

  /* 语句覆盖测试（最基本） */
  @Test
  public void testStatementCoverage() throws Exception {
    System.out.println("\n=== 语句覆盖测试 ===");
    System.out.println("测试: 查询从'hello'到'world'的桥接词");
    List<String> result = graph.queryBridgeWords("hello", "world");
    System.out.println("期待输出: [java]");
    System.out.println("实际输出: " + result);
    assertEquals(Arrays.asList("java"), result);
  }

  /* 判定覆盖测试（分支覆盖） */
  @Test
  public void testDecisionCoverage() throws Exception {
    System.out.println("\n=== 判定覆盖测试 ===");

    // 分支1: word1不在图中
    System.out.println("测试1: word1不在图中");
    List<String> result1 = graph.queryBridgeWords("nonexistent", "world");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result1);
    assertTrue(result1.isEmpty());

    // 分支2: word2不在图中
    System.out.println("\n测试2: word2不在图中");
    List<String> result2 = graph.queryBridgeWords("hello", "nonexistent");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result2);
    assertTrue(result2.isEmpty());

    // 分支3: edgesFromWord1为null（设置一个没有出边的节点）
    System.out.println("\n测试3: edgesFromWord1为null");
    Set<String> nodes = (Set<String>) getPrivateField(graph, "nodes");
    nodes.add("isolated");
    List<String> result3 = graph.queryBridgeWords("isolated", "world");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result3);
    assertTrue(result3.isEmpty());
  }

  /* 条件覆盖测试 */
  @Test
  public void testConditionCoverage() throws Exception {
    System.out.println("\n=== 条件覆盖测试 ===");

    // 条件1: word1不在图中（true）
    System.out.println("测试1: word1不在图中");
    List<String> result1 = graph.queryBridgeWords("x", "world");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result1);
    assertTrue(result1.isEmpty());

    // 条件2: word1在图中（false）
    System.out.println("\n测试2: word1在图中");
    List<String> result2 = graph.queryBridgeWords("hello", "world");
    System.out.println("期待输出: [java]");
    System.out.println("实际输出: " + result2);
    assertFalse(result2.isEmpty());

    // 条件3: word2不在图中（true）
    System.out.println("\n测试3: word2不在图中");
    List<String> result3 = graph.queryBridgeWords("hello", "y");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result3);
    assertTrue(result3.isEmpty());

    // 条件4: word2在图中（false）
    System.out.println("\n测试4: word2在图中");
    List<String> result4 = graph.queryBridgeWords("hello", "world");
    System.out.println("期待输出: [java]");
    System.out.println("实际输出: " + result4);
    assertFalse(result4.isEmpty());
  }

  /* 判定/条件覆盖测试 */
  @Test
  public void testDecisionConditionCoverage() throws Exception {
    System.out.println("\n=== 判定/条件覆盖测试 ===");

    // 组合1: word1不在且word2不在
    System.out.println("测试1: word1不在且word2不在");
    List<String> result1 = graph.queryBridgeWords("x", "y");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result1);
    assertTrue(result1.isEmpty());

    // 组合2: word1在且word2不在
    System.out.println("\n测试2: word1在且word2不在");
    List<String> result2 = graph.queryBridgeWords("hello", "y");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result2);
    assertTrue(result2.isEmpty());

    // 组合3: word1不在且word2在
    System.out.println("\n测试3: word1不在且word2在");
    List<String> result3 = graph.queryBridgeWords("x", "world");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result3);
    assertTrue(result3.isEmpty());

    // 组合4: word1在且word2在
    System.out.println("\n测试4: word1在且word2在");
    List<String> result4 = graph.queryBridgeWords("hello", "world");
    System.out.println("期待输出: [java]");
    System.out.println("实际输出: " + result4);
    assertEquals(1, result4.size());
    assertTrue(result4.contains("java"));

    // 组合5: edgesFromWord1为null
    System.out.println("\n测试5: edgesFromWord1为null");
    Set<String> nodes = (Set<String>) getPrivateField(graph, "nodes");
    nodes.add("isolated");
    List<String> result5 = graph.queryBridgeWords("isolated", "world");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result5);
    assertTrue(result5.isEmpty());
  }

  /* 条件组合覆盖测试（最严格） */
  @Test
  public void testConditionCombinationCoverage() throws Exception {
    System.out.println("\n=== 条件组合覆盖测试 ===");

    // 准备测试数据：添加另一个桥接词路径 hello -> test -> world
    Map<String, Map<String, Integer>> adjMap = (Map<String, Map<String, Integer>>) getPrivateField(graph, "adjacencyMap");
    adjMap.get("hello").put("test", 1);
    adjMap.put("test", new HashMap<>());
    adjMap.get("test").put("world", 1);

    Set<String> nodes = (Set<String>) getPrivateField(graph, "nodes");
    nodes.add("test");

    // 测试所有条件组合：

    // 1. word1不在 + word2不在
    System.out.println("测试1: word1不在 + word2不在");
    List<String> result1 = graph.queryBridgeWords("x", "y");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result1);
    assertTrue(result1.isEmpty());

    // 2. word1不在 + word2在
    System.out.println("\n测试2: word1不在 + word2在");
    List<String> result2 = graph.queryBridgeWords("x", "world");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result2);
    assertTrue(result2.isEmpty());

    // 3. word1在 + word2不在
    System.out.println("\n测试3: word1在 + word2不在");
    List<String> result3 = graph.queryBridgeWords("hello", "y");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result3);
    assertTrue(result3.isEmpty());

    // 4. word1在 + word2在 + edgesFromWord1为null
    System.out.println("\n测试4: word1在 + word2在 + edgesFromWord1为null");
    nodes.add("isolated");
    List<String> result4 = graph.queryBridgeWords("isolated", "world");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result4);
    assertTrue(result4.isEmpty());

    // 5. word1在 + word2在 + edgesFromWord1非null + 有桥接词
    System.out.println("\n测试5: word1在 + word2在 + edgesFromWord1非null + 有桥接词");
    List<String> result5 = graph.queryBridgeWords("hello", "world");
    System.out.println("期待输出: [java, test] (顺序不重要)");
    System.out.println("实际输出: " + result5);
    assertEquals(2, result5.size());
    assertTrue(result5.containsAll(Arrays.asList("java", "test")));

    // 6. word1在 + word2在 + edgesFromWord1非null + 无桥接词
    System.out.println("\n测试6: word1在 + word2在 + edgesFromWord1非null + 无桥接词");
    adjMap.get("java").remove("world");
    adjMap.get("test").remove("world");
    List<String> result6 = graph.queryBridgeWords("hello", "world");
    System.out.println("期待输出: []");
    System.out.println("实际输出: " + result6);
    assertTrue(result6.isEmpty());

    // 7. edgesFromWord3为null的情况
    System.out.println("\n测试7: edgesFromWord3为null的情况");
    adjMap.get("hello").put("dummy", 1);
    List<String> result7 = graph.queryBridgeWords("hello", "world");
    System.out.println("期待输出: 不包含'dummy'");
    System.out.println("实际输出: " + result7);
    assertFalse(result7.contains("dummy")); // dummy没有出边，不应成为桥接词
  }
}