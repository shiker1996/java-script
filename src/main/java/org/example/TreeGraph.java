package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 将列表转换为树并进行可视化
 * 你的csv文件中需要包含：当前父节点id，子节点id，父节点名称，子节点名称，子节点属性
 */
public class TreeGraph {

    static class Node {
        Long id;
        String nodeName;
        int level;
        String nodeProperties;
        List<Node> children = new ArrayList<>();

        Node(Long id, String nodeName, int level, String nodeProperties) {
            this.id = id;
            this.nodeName = nodeName;
            this.level = level;
            this.nodeProperties = nodeProperties;
        }
    }

    public static void main(String[] args) {
        File file = new File("/Users/001/Downloads/test.csv");
        if (!file.isFile() || !file.exists()) {
            System.out.println("文件不存在!");
            return;
        }
        List<String[]> relationships = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String lineTxt;
            while ((lineTxt = br.readLine()) != null) {
                String[] data = lineTxt.split(",");
                String parentId = data[0].trim();
                String childId = data[1].trim();
                String parentName = data[2].trim();
                String childName = data[3].trim();
                String level = data[4].trim();
                String childProperties = data[5].trim();
                relationships.add(new String[]{parentId, childId, parentName,childName, level, childProperties});
            }
        } catch (Exception e) {
            System.out.println("导入数据错误");
        }
        printList(relationships);
    }

    private static void printList(List<String[]> relationships){
        if(relationships == null||relationships.isEmpty()){
            return;
        }
        Map<Long, Node> nodes = new HashMap<>();

        for (String[] relationship : relationships) {
            Long id = Long.valueOf(relationship[0]);
            String nodeName = relationship[2];
            int nodeLevel = Integer.parseInt(relationship[4]);
            nodes.put(id, new Node(id, nodeName, nodeLevel, null));
        }

        for (String[] relationship : relationships) {
            Long id = Long.valueOf(relationship[1]);
            String nodeName = relationship[3];
            int nodeLevel = Integer.parseInt(relationship[4]);
            nodes.put(id, new Node(id, nodeName, nodeLevel + 1,relationship[5]));
        }

        for (String[] relationship : relationships) {
            String parentId = relationship[0];
            String childId = relationship[1];
            nodes.get(Long.valueOf(parentId)).children.add(nodes.get(Long.valueOf(childId)));
        }
        for (Node node: nodes.values()) {
            if(node.level == 1) {
                printTree(node, "", true);
            }
        }
    }

    private static void printTree(Node node, String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.nodeName + "(" + node.nodeProperties + ")");
        for (int i = 0; i < node.children.size() - 1; i++) {
            printTree(node.children.get(i), prefix + (isTail ? "    " : "│   "), false);
        }
        if (!node.children.isEmpty()) {
            printTree(node.children.get(node.children.size() - 1), prefix + (isTail ? "    " : "│   "), true);
        }
    }
}