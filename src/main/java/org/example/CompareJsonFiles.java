package org.example;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.dto.DemoDto;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 比较两个文件：
 * 1. 文件比较大，无法讲所有文本放入内存中进行比较
 * 2. 忽略文件中文本顺序，如果一行文本在另一行中存在，则认为两者相等
 * 3. 如果不存在时，则找最相近的一条并打印出不匹配的字段
 */
public class CompareJsonFiles {

    public static void main(String[] args) {

        ClassLoader classLoader = CompareJsonFiles.class.getClassLoader();
        String file1Path = classLoader.getResource("compare-json-file/test1.txt").getPath();
        String file2Path = classLoader.getResource("compare-json-file/test2.txt").getPath();

        try {
            compareFiles(file1Path, file2Path, 100, DemoDto.class, Collections.singletonList(""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 比较两个json大文件中不一致的json行，在忽略指定不校验字段的情况下，通过滑动窗口的方式进行比较，最后输出不一致的json行总数，
     * 并打印出不一致的文本行及其最匹配的结果，只打印前5条。
     * @param file1Path 文件1路径
     * @param file2Path 文件2路径
     * @param bufferSize 窗口大小
     * @param clazz 文件中的对象存储格式
     * @param unCheckedFields 不校验的字段
     * @param <T> 对象
     * @throws IOException
     */
    private static <T> void compareFiles(String file1Path, String file2Path, int bufferSize, Class<T> clazz, List<String> unCheckedFields) throws IOException {
        //读取文件1中的文本块
        Set<String> diffResult = findDiffResult(file1Path, file2Path, bufferSize, clazz, unCheckedFields);
        //结果打印
        System.out.println("查询不一致的数据条数: " + diffResult.size());
        //不匹配的结果中找到最为相近的结果进行打印
        //查询
        Map<String, Integer> line2DistanceMap = new HashMap<>();
        Map<String, String> line2ResultMap = new HashMap<>();
        try (BufferedReader file2Reader = new BufferedReader(new FileReader(file2Path))) {
            String file2Line;
            while ((file2Line = file2Reader.readLine()) != null) {
                int count = 0;
                for (String diffLine : diffResult) {
                    if (count > 5) {
                        break;
                    }
                    int distance = calculateLevenshteinDistance(file2Line, diffLine);
                    line2DistanceMap.putIfAbsent(diffLine, distance);
                    line2ResultMap.putIfAbsent(diffLine, file2Line);
                    if (distance < line2DistanceMap.get(diffLine)) {
                        line2DistanceMap.put(diffLine, distance);
                        line2ResultMap.put(diffLine, file2Line);
                    }
                    count++;
                }
            }
        }
        for (String line : line2ResultMap.keySet()) {
            printDifference(line2ResultMap.get(line), line);
        }
    }

    private static <T> Set<String> findDiffResult(String file1Path, String file2Path, int bufferSize, Class<T> clazz, List<String> unCheckedFields) throws IOException {
        Set<String> file1Lines = new HashSet<>();
        Set<String> diffResult = new HashSet<>();
        String file1Line;
        BufferedReader fileReader1 = new BufferedReader(new FileReader(file1Path));
        while ((file1Line = fileReader1.readLine()) != null) {
            //如果文本快读取完成
            file1Lines.add(file1Line);
            //判断文本快中的元素是否在文件夹2中存在
            if (file1Lines.size() == bufferSize) {
                compareInFile2(file2Path, clazz, unCheckedFields, file1Lines, diffResult);
                file1Lines = new HashSet<>();
            }
        }
        compareInFile2(file2Path, clazz, unCheckedFields, file1Lines, diffResult);
        return diffResult;
    }

    private static <T> void compareInFile2(String file2Path, Class<T> clazz, List<String> unCheckedFields, Set<String> file1Lines, Set<String> diffResult) throws IOException {
        try (BufferedReader file2Reader = new BufferedReader(new FileReader(file2Path))) {
            String line;
            while ((line = file2Reader.readLine()) != null) {
                T file2Item = JSONObject.parseObject(line, clazz);
                file1Lines.removeIf(item -> {
                    T file1Item = JSONObject.parseObject(item, clazz);
                    return compareFields(file1Item, file2Item, unCheckedFields) == null;
                });
            }
            if (!file1Lines.isEmpty()) {
                diffResult.addAll(file1Lines);
            }
        }
    }

    private static int calculateLevenshteinDistance(String str1, String str2) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        return levenshteinDistance.apply(str1, str2);
    }

    private static void printDifference(String bestMatch, String line) {
        int minLength = Math.min(bestMatch.length(), line.length());
        StringBuilder diffChars = new StringBuilder();
        // 打印包含不同字符的行
        for (int i = 0; i < minLength; i++) {
            if (bestMatch.charAt(i) != line.charAt(i)) {
                diffChars.append("*");
            } else {
                diffChars.append(bestMatch.charAt(i));
            }
        }
        System.out.println("新文件中查询不到的数据为: " + line);
        System.out.println("在新文件中匹配到的数据为: " + bestMatch);
        System.out.println("在新文件中不一致的字段为: " + diffChars);
    }

    public static Field compareFields(Object from, Object to, List<String> unCheckedFields) {
        Field[] fromFields = from.getClass().getDeclaredFields();
        Field[] toFields = to.getClass().getDeclaredFields();
        HashMap<String, Object> fromKey2Value = new HashMap<>();
        Field result = null;

        try {
            for (Field fromField : fromFields) {
                if (unCheckedFields.contains(fromField.getName())) {
                    continue;
                }
                fromField.setAccessible(true);
                Object fieldValue = fromField.get(from);
                if (fieldValue == null) {
                    continue;
                }
                fromKey2Value.put(fromField.getName(), fromField.get(from));
            }

            for (Field toField : toFields) {
                toField.setAccessible(true);
                if (fromKey2Value.containsKey(toField.getName())) {
                    if (fromKey2Value.get(toField.getName()) instanceof Date) {
                        Long fromTimeStamp = ((Date) fromKey2Value.get(toField.getName())).getTime();
                        Long toTimestamp = ((Date) toField.get(to)).getTime();
                        if (Math.abs(fromTimeStamp - toTimestamp) > 3000) {
                            result = toField;
                        }
                    } else if (fromKey2Value.get(toField.getName()) instanceof BigDecimal) {
                        BigDecimal fromValue = (BigDecimal) fromKey2Value.get(toField.getName());
                        BigDecimal toValue = (BigDecimal) toField.get(to);
                        if (fromValue.compareTo(toValue) != 0){
                            result = toField;
                        }
                    } else if (!fromKey2Value.get(toField.getName()).equals(toField.get(to))) {
                        result = toField;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }
}