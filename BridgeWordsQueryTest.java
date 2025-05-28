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
    List<String> result = graph.queryBridgeWords("hello", "world");
    assertEquals(Arrays.asList("java"), result);
  }

  /* 判定覆盖测试（分支覆盖） */
  @Test
  public void testDecisionCoverage() throws Exception {
    // 分支1: word1不在图中
    assertTrue(graph.queryBridgeWords("nonexistent", "world").isEmpty());

    // 分支2: word2不在图中
    assertTrue(graph.queryBridgeWords("hello", "nonexistent").isEmpty());

    // 分支3: edgesFromWord1为null（设置一个没有出边的节点）
    Set<String> nodes = (Set<String>) getPrivateField(graph, "nodes");
    nodes.add("isolated");
    assertTrue(graph.queryBridgeWords("isolated", "world").isEmpty());
  }

  /* 条件覆盖测试 */
  @Test
  public void testConditionCoverage() throws Exception {
    // 条件1: word1不在图中（true）
    assertTrue(graph.queryBridgeWords("x", "world").isEmpty());

    // 条件2: word1在图中（false）
    assertFalse(graph.queryBridgeWords("hello", "world").isEmpty());

    // 条件3: word2不在图中（true）
    assertTrue(graph.queryBridgeWords("hello", "y").isEmpty());

    // 条件4: word2在图中（false）
    assertFalse(graph.queryBridgeWords("hello", "world").isEmpty());
  }

  /* 判定/条件覆盖测试 */
  @Test
  public void testDecisionConditionCoverage() throws Exception {
    // 组合1: word1不在且word2不在
    assertTrue(graph.queryBridgeWords("x", "y").isEmpty());

    // 组合2: word1在且word2不在
    assertTrue(graph.queryBridgeWords("hello", "y").isEmpty());

    // 组合3: word1不在且word2在
    assertTrue(graph.queryBridgeWords("x", "world").isEmpty());

    // 组合4: word1在且word2在
    List<String> result = graph.queryBridgeWords("hello", "world");
    assertEquals(1, result.size());
    assertTrue(result.contains("java"));

    // 组合5: edgesFromWord1为null
    Set<String> nodes = (Set<String>) getPrivateField(graph, "nodes");
    nodes.add("isolated");
    assertTrue(graph.queryBridgeWords("isolated", "world").isEmpty());
  }

  /* 条件组合覆盖测试（最严格） */
  @Test
  public void testConditionCombinationCoverage() throws Exception {
    // 准备测试数据：添加另一个桥接词路径 hello -> test -> world
    Map<String, Map<String, Integer>> adjMap = (Map<String, Map<String, Integer>>) getPrivateField(graph, "adjacencyMap");
    adjMap.get("hello").put("test", 1);
    adjMap.put("test", new HashMap<>());
    adjMap.get("test").put("world", 1);

    Set<String> nodes = (Set<String>) getPrivateField(graph, "nodes");
    nodes.add("test");

    // 测试所有条件组合：

    // 1. word1不在 + word2不在
    assertTrue(graph.queryBridgeWords("x", "y").isEmpty());

    // 2. word1不在 + word2在
    assertTrue(graph.queryBridgeWords("x", "world").isEmpty());

    // 3. word1在 + word2不在
    assertTrue(graph.queryBridgeWords("hello", "y").isEmpty());

    // 4. word1在 + word2在 + edgesFromWord1为null
    nodes.add("isolated");
    assertTrue(graph.queryBridgeWords("isolated", "world").isEmpty());

    // 5. word1在 + word2在 + edgesFromWord1非null + 有桥接词
    List<String> result1 = graph.queryBridgeWords("hello", "world");
    assertEquals(2, result1.size());
    assertTrue(result1.containsAll(Arrays.asList("java", "test")));

    // 6. word1在 + word2在 + edgesFromWord1非null + 无桥接词
    adjMap.get("java").remove("world");
    adjMap.get("test").remove("world");
    List<String> result2 = graph.queryBridgeWords("hello", "world");
    assertTrue(result2.isEmpty());

    // 7. edgesFromWord3为null的情况
    adjMap.get("hello").put("dummy", 1);
    List<String> result3 = graph.queryBridgeWords("hello", "world");
    assertFalse(result3.contains("dummy")); // dummy没有出边，不应成为桥接词
  }
}