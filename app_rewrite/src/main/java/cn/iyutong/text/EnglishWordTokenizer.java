package cn.iyutong.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnglishWordTokenizer {
    // 将驼峰式命名的字符串分词
    public static List<String> tokenizeCamelCase(String camelCaseStr) {
        List<String> tokens = new ArrayList<>();

        if (camelCaseStr == null || camelCaseStr.isEmpty()) {
            return tokens;
        }
        Pattern pattern = Pattern.compile("([A-Z]?[a-z]+)|([A-Z]+(?=[A-Z]|$))");
        Matcher matcher = pattern.matcher(camelCaseStr);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    // 将分词结果用空格连接成字符串
    public static String joinWithSpace(List<String> tokens) {
        return String.join(" ", tokens);
    }

    // 增强版下划线分词，会进一步处理下划线后的驼峰式部分
    public static List<String> enhancedTokenizeUnderscore(String underscoreStr) {
        List<String> tokens = new ArrayList<>();
        if (underscoreStr == null || underscoreStr.isEmpty()) {
            return tokens;
        }
        String[] parts = underscoreStr.split("_");
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (part.matches(".*[A-Z].*") && !part.equals(part.toUpperCase())) {
                    tokens.addAll(tokenizeCamelCase(part));
                } else {
                    tokens.add(part);
                }
            }
        }
        return tokens;
    }

    // 新增：增强版连字符分词，会进一步处理连字符后的驼峰式部分
    public static List<String> enhancedTokenizeHyphen(String hyphenStr) {
        List<String> tokens = new ArrayList<>();
        if (hyphenStr == null || hyphenStr.isEmpty()) {
            return tokens;
        }
        String[] parts = hyphenStr.split("-");
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (part.matches(".*[A-Z].*") && !part.equals(part.toUpperCase())) {
                    tokens.addAll(tokenizeCamelCase(part));
                } else {
                    tokens.add(part);
                }
            }
        }
        return tokens;
    }

    // 增强版智能分词，支持驼峰、下划线、连字符和空格分隔
    public static List<String> smartTokenizeEnhanced(String input) {
        if (input == null || input.isEmpty()) {
            return new ArrayList<>();
        }

        if (input.contains("_")) {
            return enhancedTokenizeUnderscore(input);
        } else if (input.contains("-")) {
            return enhancedTokenizeHyphen(input);
        } else if (input.matches(".*[A-Z].*") && !input.equals(input.toUpperCase())) {
            return tokenizeCamelCase(input);
        } else {
            List<String> tokens = new ArrayList<>();
            for (String word : input.split("\\s+")) {
                if (!word.isEmpty()) {
                    tokens.add(word);
                }
            }
            return tokens;
        }
    }

}
