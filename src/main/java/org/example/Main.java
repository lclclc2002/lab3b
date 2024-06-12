package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Main class for the application.
 */
public class Main {
    /**
     * ��ʾͼ�е���֮��������.
     * ���ǵ��ʡ�ֵ�Ǹõ��ʿ���ָ��ĵ��ʼ���.
     */
    private static Map<String, Set<String>> graph;

    /**
     * �洢ÿ�����ʳ��ֵ�Ƶ��.
     * ���ǵ��ʣ�ֵ�Ǹõ��ʳ��ֵĴ���.
     */
    private static Map<String, Integer> wordFrequency;

    /**
     * �洢ͼ��ÿ���ߵ�Ȩ��.
     * ������ʼ���ʣ�ֵ����һ��ӳ�䣬�����Ŀ�ĵ��ʣ�ֵ�Ǳߵ�Ȩ��.
     */
    private static Map<String, Map<String, Integer>> edgeWeights;

    /**
     * һ������������ߵ�Randomʵ��.
     * ������Ϊfinal��ȷ���ڶ��̻߳����µİ�ȫ��.
     */
    private static final Random random = new Random();

    /**
     * ��ʾͼ�ж�����������.
     * ���ڳ�ʼ���������Ĵ�С.
     */
    private static int V;

    /**
     * �洢�����֮������·������.
     * ��ά���飬����dist[i][j]��ʾ�Ӷ���i������j�����·������.
     */
    private static volatile boolean stopRandomWalk = false;
    private static int[][] dist;

    /**
     * ���������ڵ㣬��ʼ�����ݽṹ����ȡ�ļ������ṩ�û������˵�.
     * <p>
     * �������ȴ�����������ݽṹ������ͼ������Ƶ�ʺͱ�Ȩ�ء�
     * Ȼ������ȡ������һ���ı��ļ�������ͼ��
     * ���������һ��ѭ�����ṩ�û�һ���˵����û����Դ���ѡ��ͬ�Ĳ�����
     * ����ʾ����ͼ����ѯ�ŽӴʡ��������ı����������·����ִ��������߻��˳�����
     * </p>
     *
     * @param args �����в�������ǰδʹ�á�
     */
    public static void main(final String[] args) {
        // ��ʼ�����ݽṹ
        graph = new HashMap<>();
        wordFrequency = new HashMap<>();
        edgeWeights = new HashMap<>();


        // ��ȡ�ı��ļ�������ͼ
        readTextFileAndBuildGraph("C:\\Users\\lc\\Desktop\\Lab1-main\\Lab1-main\\Lab1-main\\test\\test1.txt",graph,
                wordFrequency, edgeWeights);

        Scanner scanner = new Scanner(System.in);
        char choice;

        do {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Show Directed Graph");
            System.out.println("2. Query Bridge Words");
            System.out.println("3. Generate New Text");
            System.out.println("4. Calculate Shortest Path");
            System.out.println("5. Perform Random Walk");
            System.out.println("6. Exit");
            System.out.print("Enter your choice (1-6): ");
            choice = scanner.next().charAt(0);
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case '1' -> {
                    System.out.println("\nGraph built. Displaying directed graph...");
                    showDirectedGraph();
                }
                case '2' -> {
                    System.out.print("Enter word 1: ");
                    String word1 = scanner.nextLine();
                    System.out.print("Enter word 2: ");
                    String word2 = scanner.nextLine();
                    System.out.println("Bridge words from '" + word1 + "' to '"
                            + word2 + "': " + queryBridgeWords(graph, word1, word2));
                }
                case '3' -> {
                    System.out.println(
                            "Enter a line of text to generate new text:");
                    String inputText = scanner.nextLine();
                    System.out.println("Generated new text: "
                            + generateNewText(inputText));
                }
                case '4' -> {
                    System.out.print("Enter word 1: ");
                    String wordA = scanner.nextLine();
                    System.out.print("Enter word 2: ");
                    String wordB = scanner.nextLine();
                    showDirectedGraphWithShortestPath(wordA, wordB);
                }
                case '5' -> {
                    System.out.println(
                            "\nPerforming a random walk... Press 's' to stop.");
                    stopRandomWalk = false; // ����ֹͣ��־
                    // ʹ���߳�ִ���������
                    Thread randomWalkThread = new Thread(Main::randomWalk);
                    randomWalkThread.start();

                    // �ȴ��û�������ֹͣ���߻򷵻����˵�
                    while (!stopRandomWalk) {
                        System.out.print(
                                "Enter 's' to stop the random walk"
                                        +
                                        "or just press enter to continue: \n");
                        String input = scanner.nextLine().trim().toLowerCase();
                        if ("s".equals(input)) {
                            stopRandomWalk = true; // �û�����ֹͣ�������
                            break;
                        }
                    }
                }
                case '6' -> System.out.println("Exiting program.");
                default -> System.out.println("Invalid choice. "
                        +
                        "Please enter a number between 1 and 6.");
            }
        } while (choice != '6');

        scanner.close();
    }

    /**
     * ��ָ�����ı��ļ��ж�ȡ���ݣ�������ͼ�͵���Ƶ��ӳ��.
     * <p>
     * �÷���ʹ��BufferedReader���ж�ȡ�ļ�������ÿһ���е��ı�ת��ΪСд��
     * ͬʱ�����з���ĸ�ַ��滻Ϊ�հס�Ȼ������ÿһ�зָ�ɵ��ʣ�
     * ����ÿ�������ĵ�����Ϊͼ�е�һ������ߡ�����Ƶ��ӳ���¼ÿ�����ʳ��ֵ�Ƶ�ʣ�
     * ����Ȩ��ӳ���¼ÿ�����ʶ�֮��ߵ�Ȩ�ء�
     * </p>
     *
     * @param filePath Ҫ��ȡ���ı��ļ���·����
     * @throws IOException �����ȡ�ļ�ʱ����I/O����
     */
    public static void readTextFileAndBuildGraph(final String filePath,
                                                 Map<String, Set<String>> graph,
                                                 Map<String, Integer> wordFrequency,
                                                 Map<String, Map<String, Integer>> edgeWeights) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            while ((line = br.readLine()) != null) {
                // ת��ΪСд����������ĸ�ַ��滻Ϊ�ո�
                line = line.toLowerCase().replaceAll("[^a-z ]", " ");

                // �ָ��
                String[] words = line.split("\\s+");

                for (int i = 0; i < words.length - 1; i++) {
                    String currentWord = words[i];
                    String nextWord = words[i + 1];

                    // ���µ���Ƶ��
                    wordFrequency.put(currentWord, wordFrequency.getOrDefault(currentWord, 0) + 1);

                    // ��ӱ�
                    graph.computeIfAbsent(currentWord, k -> new HashSet<>()).add(nextWord);

                    // ����Ȩ��
                    updateWeight(edgeWeights, currentWord, nextWord);
                }
            }
            V = graph.size();
            dist = new int[V][V]; // ȷ���ⲿ����ͼ������ɺ���г�ʼ��


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ���� updateWeight �����Խ��� edgeWeights ӳ����Ϊ����
    private static void updateWeight(Map<String, Map<String, Integer>> edgeWeights, final String from, final String to) {
        // ��ȡ��from��to������Ȩ�أ����û�����ã���Ĭ��Ϊ0
        int currentWeight = edgeWeights.getOrDefault(from, new HashMap<>()).getOrDefault(to, 0);

        // Ȩ�ؼ�1����Ϊÿ�ε��ô˷�����ζ�� from �� to �����ڳ�����һ��
        currentWeight += 1;

        // ���±ߵ�Ȩ��
        edgeWeights.computeIfAbsent(from, k -> new HashMap<>()).put(to, currentWeight);
    }

    /**
     * ��ʾ����ͼ�Ŀ��ӻ�.
     * <p>
     * �÷������ȴ���һ��DOT�ļ������ļ�������ͼ�Ľṹ�ͽڵ�/�ߵ����ԡ�
     * Ȼ����ʹ��Graphviz����������й��߽�DOT�ļ�ת��Ϊͼ�α�ʾ��
     * ͨ����һ��PNGͼ���ļ���
     * </p>
     * <p>
     * �ڵ��ʾ���ʣ��߱�ʾ����֮���ת�ƣ��ߵ�Ȩ�ر�ʾת�Ʒ�����Ƶ�ʡ�
     * ���ɵ�ͼ���ļ����������û���Ŀ¼�¡�
     * </p>
     *
     * @see #escapeDotString(String)
     * @see #graph
     * @see #edgeWeights
     */
    public static void showDirectedGraph() {
        // DOT �ļ������������û�Ŀ¼��
        String dotFilePath = "graph.dot";
        String graphvizPath = "D:\\Graphviz-11.0.0-win64\\Graphviz\\bin\\dot.exe";

        String pngFilePath = "C:\\Users\\lc\\Desktop\\Lab1-main\\Lab1-main\\Lab1-main\\graph.png";

        // ����DOT�ļ�
        try (PrintWriter out = new PrintWriter(new FileWriter(dotFilePath))) {
            out.println("digraph G {");
            out.println("  rankdir=LR;"); // ����ͼ�ķ��������

            // ��ӽڵ�
            for (String node : graph.keySet()) {
                out.println(
                        "  \"" + escapeDotString(node) + "\" [shape=circle];");
            }

            // ��ӱߺ�Ȩ��
            for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
                String fromNode = entry.getKey();
                for (String toNode : entry.getValue()) {
                    // ��ȡ�ߵ�Ȩ��
                    int weight = edgeWeights.getOrDefault(
                            fromNode, new HashMap<>()).getOrDefault(toNode, 0);
                    // ��Ȩ����Ϊ��ǩ��ӵ���
                    out.printf(
                            "  \"%s\" -> \"%s\" [label=\"%d\"];\n",
                            escapeDotString(fromNode),
                            escapeDotString(toNode), weight);
                }
            }

            out.println("}");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // ʹ��Graphviz�����й�������ͼ��
        try {
            // ����dot����
            String command = graphvizPath
                    + " -Tpng " + dotFilePath + " -o " + pngFilePath;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println("Graph visualization generated as '"
                    + pngFilePath + "'");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ת��DOT���������ַ�
    private static String escapeDotString(final String input) {
        return input.replace("\"", "\\\"");
    }


    // ��ѯ�ŽӴ�
    public static String queryBridgeWords(
            final Map<String, Set<String>> graph ,final String word1, final String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        Set<String> successorsWord1 = graph.get(word1);
        Set<String> predecessorsWord2
                = new HashSet<>(graph.size()); // ��¼word2��ǰ���ڵ�

        // Ѱ��word2��ǰ���ڵ�
        for (String key : graph.keySet()) {
            Set<String> successors = graph.get(key);
            if (successors.contains(word2)) {
                predecessorsWord2.add(key);
            }
        }

        // ����word1�����к�̽ڵ㣬����Ƿ�Ҳ��word2��ǰ���ڵ�
        for (String successor : successorsWord1) {
            if (predecessorsWord2.contains(successor)) {
                bridgeWords.add(successor);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: "
                    + String.join(", ", bridgeWords);
        }
    }


    //    // ����bridge word�������ı�
    public static String generateNewText(String inputText) {
        // ʹ�ÿո�ָ������ı����õ���������
        String[] words = inputText.toLowerCase().split("\\s+");

        // ����б����ڴ洢���ı��ĵ���
        List<String> newWords = new ArrayList<>();

        // �������ѡ���ŽӴ�
        Random random = new Random();

        // �����������飬����ÿ�����ڵ��ʵ��ŽӴ�
        for (int i = 0; i < words.length - 1; i++) {
            // ��ӵ�ǰ����
            newWords.add(words[i]);

            // ��ѯ������ڵ��ʵ��ŽӴ�
            String bridgeWordsResult = queryBridgeWords(graph, words[i], words[i + 1]);
            // ����ŽӴʽ����"No bridge words"��ͷ�����ʾû���ŽӴ�
            if (bridgeWordsResult.startsWith("No")) {
                // �������κε��ʣ�����
                continue;
            }

            // ������ŽӴʣ�������ӵ�����б�
            // �ŽӴ��Զ��ŷָ���������Ҫ�ָ���ѡ��һ��
            String[] bridgeWordArray = bridgeWordsResult.substring(
                    bridgeWordsResult.indexOf(':') + 2).trim().split(", ");
            // ���ѡ��һ���ŽӴ�
            String bridgeWord =
                    bridgeWordArray[random.nextInt(bridgeWordArray.length)];
            newWords.add(bridgeWord);
        }

        // ������һ������
        newWords.add(words[words.length - 1]);

        // ������б�ת��Ϊ�ַ�����ʹ�ÿո����ӵ���
        return String.join(" ", newWords);
    }



    // ������������֮������·��
    public static String calcShortestPath(
            final String word1, final String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        // ת�����ڽӾ����������ߵ�Ȩ��Ϊ1

        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                dist[i][j] = (i == j) ? 0 : Integer.MAX_VALUE;
            }
        }

        // ����ڽӾ���
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String word = entry.getKey();
            Set<String> neighbors = entry.getValue();
            for (String neighbor : neighbors) {
                int index1 = getIndex(word);
                int index2 = getIndex(neighbor);
                if (index1 != -1 && index2 != -1) {
                    dist[index1][index2] = 1;
                }
            }
        }

        // ���������㷨������ж���Ե����·��
        for (int k = 0; k < V; k++) {
            for (int i = 0; i < V; i++) {
                for (int j = 0; j < V; j++) {
                    if (dist[i][k] != Integer.MAX_VALUE
                            && dist[k][j] != Integer.MAX_VALUE
                            && dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }

        // �ҵ�word1��word2��Ӧ������
        int index1 = getIndex(word1);
        int index2 = getIndex(word2);

        // �����̾����������˵��word1��word2������
        if (dist[index1][index2] == Integer.MAX_VALUE) {
            return "No path between " + word1 + " and " + word2 + ".";
        }
        // ��ȡ���·��
        List<String> shortestPath = extractShortestPath(word1, word2);

        // ����Ƿ���·��
        if (shortestPath.size() < 2) {
            return "No path between " + word1 + " and " + word2 + ".";
        }

        // ʹ��StringBuilder��������ͷ��·���ַ���
        StringBuilder pathWithArrows = new StringBuilder(
                "The shortest path from ").append(word1).append(" to ")
                .append(word2).append(" is: ");
        for (int i = 0; i < shortestPath.size(); i++) {
            pathWithArrows.append(shortestPath.get(i));
            if (i < shortestPath.size() - 1) {
                pathWithArrows.append(" �� ");
            }
        }

        System.out.println(pathWithArrows.toString()); // ��ӡ����ͷ��·��
        // ����word1��word2֮������·������
        return "The shortest path distance from "
                + word1 + " to " + word2 + " is: "
                + dist[index1][index2];
    }


    public static void showDirectedGraphWithShortestPath(
            final String word1, final String word2) {
        // ���ȣ��������·��
        String shortestPathResult = calcShortestPath(word1, word2);
        if (!shortestPathResult.startsWith("The shortest path distance")) {
            System.out.println(shortestPathResult);
            return;
        }

        // Ȼ�󣬴���DOT�ļ�����ӳ����ͼ�ṹ
        String dotFilePath = "graph.dot";
        String graphvizPath = "D:\\Graphviz-11.0.0-win64\\Graphviz\\bin\\dot.exe";

        String pngFilePath = "C:\\Users\\lc\\Desktop\\Lab1-main\\Lab1-main\\Lab1-main"
                +
                "\\graph_with_shortest_path.png";

        try (PrintWriter out = new PrintWriter(new FileWriter(dotFilePath))) {
            out.println("digraph G {");
            out.println("  rankdir=LR;");
            out.println("  node[shape=circle];");

            // ��ӽڵ�
            for (String node : graph.keySet()) {
                out.printf(
                        "  \"%s\" [style=filled, fillcolor=lightgray];\n",
                        escapeDotString(node));
            }

            // ��ӱ�
            for (
                    Map.Entry<String,
                            Set<String>> entry : graph.entrySet()) {
                String fromNode = entry.getKey();
                for (String toNode : entry.getValue()) {
                    int weight = edgeWeights.getOrDefault(
                            fromNode,
                            new HashMap<>()).getOrDefault(toNode, 0);
                    out.printf(
                            "  \"%s\" -> \"%s\" [label=\"%d\"];\n",
                            escapeDotString(fromNode),
                            escapeDotString(toNode),
                            weight);
                }
            }

            // �������·���㷨�����������ʾ���·���ϵıߺͽڵ�
            // �˴���Ҫ����calcShortestPath�����ľ���ʵ����ȷ�����·���ϵĽڵ�ͱ�
            // ���� shortestPath ��һ���������·���Ͻڵ���б�
            List<String> shortestPath =
                    extractShortestPath(word1, word2); // ��Ҫʵ���������

            for (String node : shortestPath) {
                out.printf(
                        "  \"%s\" [style=filled, fillcolor=red];\n",
                        escapeDotString(node));
            }
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                String fromNode = shortestPath.get(i);
                String toNode = shortestPath.get(i + 1);
                out.printf("  \"%s\" -> \"%s\" [color=red, style=bold];\n",
                        escapeDotString(fromNode), escapeDotString(toNode));
            }

            out.println("}");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // ʹ��Graphviz�����й�������ͼ��
        try {
            String command = graphvizPath
                    + " -Tpng " + dotFilePath + " -o " + pngFilePath;
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            System.out.println(
                    "Graph with highlighted shortest path generated as '"
                            + pngFilePath + "'");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static List<String> extractShortestPath(
            final String word1, final String word2) {
        List<String> path = new ArrayList<>();
        Map<String, Integer> indexMap = new HashMap<>();

        // ���ȹ���һ�������������ʵ�ӳ��
        for (int i = 0; i < V; i++) {
            String word = (String) graph.keySet().toArray()[i];
            indexMap.put(word, i);
        }

        int index1 = indexMap.get(word1);
        int index2 = indexMap.get(word2);

        // ��word1��ʼ������׷�����·��
        int at = index1;
        path.add(word1); // �����ʼ�ڵ�

        while (at != index2) {
            for (int to = 0; to < V; to++) {
                if (dist[at][to] == 1
                        && dist[to][index2] != Integer.MAX_VALUE) {
                    at = to;
                    path.add((String) graph.keySet().toArray()[at]);
                    break;
                }
            }
        }

        return path;
    }


    // �������������ڻ�ȡ�������ڽӾ����е�����
    private static int getIndex(final String word) {
        int index = 0;
        for (String w : graph.keySet()) {
            if (w.equals(word)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    // �������
    public static void randomWalk() {

        try {
            Set<String> visitedEdges = new HashSet<>();
            List<String> walkPath = new ArrayList<>();

            if (graph.isEmpty()) {
                System.out.println("The graph is empty!");
            }

            // ��ȡ���нڵ�
            Collection<String> allNodes = graph.keySet();

            // ���ѡ��һ����ʼ�ڵ�
            String startNode = allNodes.stream()
                    .skip(random.nextInt(allNodes.size()))
                    .findFirst()
                    .orElseThrow();

            walkPath.add(startNode);

            String currentNode = startNode;
            while (currentNode != null && !stopRandomWalk) {
                // ��ȡ��ǰ�ڵ�������ھӽڵ�
                Set<String> neighbors = graph.get(currentNode);
                // ���ѡ��һ���ھӽڵ���Ϊ��һ��
                String nextNode
                        = neighbors.isEmpty() ? null : neighbors
                        .toArray(new String[0])
                        [random.nextInt(neighbors.size())];

                // ����Ƿ��Ѿ����ʹ��ӵ�ǰ�ڵ㵽���ھӵı�
                String edgeKey = currentNode + "-" + nextNode; // �򻯵ı߱�ʾ����
                if (visitedEdges.contains(edgeKey)) {
                    break; // ��������ظ��ߣ���ֹͣ����
                }

                if (nextNode != null) {
                    walkPath.add(nextNode);
                    visitedEdges.add(edgeKey);
                    currentNode = nextNode;
                }
            }

            // ����û�����ֹͣ���������˳�
            if (stopRandomWalk) {
                System.out.println("Random walk stopped by user.");
            }

            // ������·��ת��Ϊ�ַ���
            System.out.println(String.join(" -> ", walkPath));
        } catch (Exception e) {
            System.out.println("No edge exist ,please enter 's'");
        }


    }


}
