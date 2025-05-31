import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TestDirectedGraph {
  private DirectedGraph graph;

  @BeforeEach
  void setUp() throws Exception {
    // 使用直接文本输入方式构建测试图（推荐方式）
    List<String> words = Arrays.asList("hello", "world", "java", "hello");
    graph = new DirectedGraph(words);

  }

  private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }

  @Test
  @DisplayName("测试桥接词生成")
  void testGenerateNewText() {
    System.out.println("\n[测试] 生成新文本");
    String input = "hello java";
    System.out.println("输入: " + input);

    String result = graph.generateNewText(input);
    System.out.println("结果: " + result);

    assertTrue(result.contains("world"), "应包含桥接词'world'");
    System.out.println("✓ 测试通过");
  }

  @Test
  @DisplayName("正常情况: 存在桥接词时插入")
  void generateNewText_WithBridgeWords_InsertsBridge() {
    System.out.println("\n[测试1] 存在桥接词的情况");
    String input = "hello java";
    System.out.println("输入文本: \"" + input + "\"");

    System.out.println("预期桥接路径: hello -> world -> java");
    String result = graph.generateNewText(input);

    System.out.println("生成结果: \"" + result + "\"");
    assertAll(
            () -> assertTrue(result.startsWith("hello"), "应以hello开头"),
            () -> assertTrue(result.endsWith("java"), "应以java结尾"),
            () -> assertTrue(result.contains("world"), "应包含桥接词world")
    );
    System.out.println("✓ 测试通过: 结果包含桥接词");
  }

  @Test
  @DisplayName("边界情况: 无桥接词时不插入")
  void generateNewText_NoBridgeWords_ReturnsOriginal() {
    System.out.println("\n[测试2] 无桥接词的情况");
    String input = "java hello";
    System.out.println("输入文本: \"" + input + "\"");
    System.out.println("预期输出应与输入相同（无桥接词）");

    String result = graph.generateNewText(input);
    System.out.println("生成结果: \"" + result + "\"");


    assertEquals(input, result);
    System.out.println("✓ 测试通过: 输出与输入一致");
  }

  @Test
  @DisplayName("边界情况: 输入单个单词时原样返回")
  void generateNewText_SingleWordInput_ReturnsOriginal() {
    System.out.println("\n[测试3] 单单词输入");
    String input = "hello";
    System.out.println("输入文本: \"" + input + "\"");

    String result = graph.generateNewText(input);
    System.out.println("生成结果: \"" + result + "\"");

    assertEquals(input, result);
    System.out.println("✓ 测试通过: 单单词原样返回");
  }

  @Test
  @DisplayName("异常情况: 输入包含非字母字符时正常处理")
  void generateNewText_WithNonAlphabetic_ProcessesCorrectly() {
    System.out.println("\n[测试4] 含非字母字符的输入");
    String input = "Hello, Java! How are you?";
    System.out.println("原始输入: \"" + input + "\"");
    System.out.println("预期处理: 转换为小写并过滤非字母字符");

    String result = graph.generateNewText(input.toLowerCase());
    System.out.println("生成结果: \"" + result + "\"");

    assertAll(
            () -> assertTrue(result.contains("hello"), "应包含处理后的hello"),
            () -> assertTrue(result.contains("java"), "应包含处理后的java"),
            () -> assertFalse(result.matches(".*[^a-z ].*"), "不应包含非字母字符")
    );
    System.out.println("✓ 测试通过: 特殊字符处理正确");
  }

  @Test
  @DisplayName("随机性验证: 多次运行结果包含可能的桥接词")
  void generateNewText_RandomBridgeSelection_CoversPossibilities() throws Exception {
    System.out.println("\n[测试5] 随机桥接词选择");

    // 使用直接文本输入方式构建特殊测试图
    DirectedGraph testGraph = new DirectedGraph(Arrays.asList("start", "a", "end", "start", "b", "end"));

    // 或者使用反射设置内部状态（保持原有逻辑）
    Map<String, Map<String, Integer>> multiBridgeMap = new HashMap<>();
    multiBridgeMap.put("start", Map.of("a", 1, "b", 1));
    multiBridgeMap.put("a", Map.of("end", 1));
    multiBridgeMap.put("b", Map.of("end", 1));
    setPrivateField(testGraph, "adjacencyMap", multiBridgeMap);

    String input = "start end";
    System.out.println("输入文本: \"" + input + "\"");
    System.out.println("可能的结果: [start a end] 或 [start b end]");

    Set<String> possibleResults = Set.of("start a end", "start b end");
    System.out.println("\n开始随机测试（20次尝试）:");

    boolean foundA = false, foundB = false;
    for (int i = 1; i <= 20; i++) {
      String result = testGraph.generateNewText(input);
      System.out.printf("尝试 %2d: %s%n", i, result);

      if (result.equals("start a end")) foundA = true;
      if (result.equals("start b end")) foundB = true;
      if (foundA && foundB) break;
    }

    System.out.println("\n统计结果:");
    System.out.println("找到桥接词a: " + (foundA ? "✓" : "✗"));
    System.out.println("找到桥接词b: " + (foundB ? "✓" : "✗"));

    boolean finalFoundA = foundA;
    boolean finalFoundB = foundB;
    assertAll(
            () -> assertTrue(finalFoundA, "应包含桥接词a"),
            () -> assertTrue(finalFoundB, "应包含桥接词b")
    );
  }

  @Test
  @DisplayName("测试空输入处理")
  void generateNewText_EmptyInput_ReturnsEmpty() {
    System.out.println("\n[测试6] 空输入测试");
    String input = "";
    System.out.println("输入文本: \"" + input + "\"");

    String result = graph.generateNewText(input);
    System.out.println("生成结果: \"" + result + "\"");

    assertEquals("", result);
    System.out.println("✓ 测试通过: 空输入返回空字符串");
  }

  @AfterEach
  void tearDown() {
    // 清理资源（如果有）
    graph = null;
  }
}