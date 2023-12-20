package org.example;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 比较两个文件：
 * 1. 文件比较大，无法讲所有文本放入内存中进行比较
 * 2. 忽略文件中文本顺序，如果一行文本在另一行中存在，则认为两者相等
 * 3. 如果不存在时，则找最相近的一条并打印出不匹配的字段
 */
public class CompareFiles {

    public static void main(String[] args) {

        ClassLoader classLoader = CompareFiles.class.getClassLoader();
        String file1Path = classLoader.getResource("compare-file/test1.txt").getPath();
        String file2Path = classLoader.getResource("compare-file/test2.txt").getPath();

        try {
            compareFiles(file1Path, file2Path, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void compareFiles(String file1Path, String file2Path, int bufferSize) throws IOException, InterruptedException {
        Set<String> file1Lines = readLinesToSet(file1Path);

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        try (BufferedReader file2Reader = new BufferedReader(new FileReader(file2Path))) {
            String line;
            int count = 0;
            while ((line = file2Reader.readLine()) != null) {
                String finalLine = line;
                Set<String> finalFile1Lines = file1Lines;
                executorService.execute(() -> compareAndPrintDifference(finalFile1Lines, finalLine));
                count++;
                // 当达到缓冲区大小时，重新读取文件1的内容，清空计数
                if (count == bufferSize) {
                    file1Lines = readLinesToSet(file1Path);
                    count = 0;
                }
            }

        }
        executorService.shutdown();
    }

    private static Set<String> readLinesToSet(String filePath) throws IOException {
        Set<String> lines = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static void compareAndPrintDifference(Set<String> file1Lines, String line) {
        String bestMatch = findBestMatch(file1Lines, line);

        if (bestMatch != null) {
            printDifference(bestMatch, line);
        }
    }

    private static String findBestMatch(Set<String> file1Lines, String line) {
        int minDistance = Integer.MAX_VALUE;
        String bestMatch = null;

        for (String file1Line : file1Lines) {
            int distance = calculateLevenshteinDistance(file1Line, line);

            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = file1Line;
            }
        }

        return bestMatch;
    }

    private static int calculateLevenshteinDistance(String str1, String str2) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return levenshteinDistance.apply(str1, str2);
    }

    private static void printDifference(String bestMatch, String line) {
        int minLength = Math.min(bestMatch.length(), line.length());
        StringBuilder diffChars = new StringBuilder();
        // 打印包含不同字符的行
        if (!bestMatch.equals(line)) {
            for (int i = 0; i < minLength; i++) {
                if (bestMatch.charAt(i) != line.charAt(i)) {
                    diffChars.append("*");
                } else {
                    diffChars.append(bestMatch.charAt(i));
                }
            }
            System.out.println("查询失败的数据为: " + line);
            System.out.println("在新文件中匹配到的数据为: " + bestMatch);
            System.out.println("在新文件中不一致的字段为: " + diffChars);
            System.out.println();
        }
    }


}
