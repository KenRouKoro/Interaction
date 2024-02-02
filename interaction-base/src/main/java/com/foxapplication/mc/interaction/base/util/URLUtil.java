package com.foxapplication.mc.interaction.base.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL工具类
 */
public class URLUtil {
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    /**
     * 根据模板验证URL是否匹配
     *
     * @param uri      要验证的URL
     * @param template URL模板
     * @return 是否匹配
     */
    public static boolean validateURLWithTemplate(String uri, String template) {
        Pattern pattern = buildPatternFromTemplate(template);
        Matcher matcher = pattern.matcher(uri);
        return matcher.matches();
    }

    /**
     * 从URL模板中获取参数
     *
     * @param uri      URL
     * @param template URL模板
     * @return 参数映射
     */
    public static Map<String, String> getParamsFromURLTemplate(String uri, String template) {
        Map<String, String> map = new HashMap<>();
        Pattern pattern = buildPatternFromTemplate(template);
        Matcher matcher = pattern.matcher(uri);

        if (matcher.find()) {
            Matcher m = PATTERN.matcher(template);
            int group = 1;
            while (m.find()) {
                String key = m.group(1);
                String value = matcher.group(group++);
                map.put(key, value);
            }
        }

        return map;
    }

    /**
     * 根据模板构建正则表达式
     *
     * @param template URL模板
     * @return 正则表达式
     */
    private static Pattern buildPatternFromTemplate(String template) {
        Matcher m = PATTERN.matcher(template);
        StringBuilder regexBuilder = new StringBuilder("^");

        int lastAppendPosition = 0;
        while (m.find()) {
            regexBuilder.append(Pattern.quote(template.substring(lastAppendPosition, m.start())));
            regexBuilder.append("(.*?)");
            lastAppendPosition = m.end();
        }
        regexBuilder.append(Pattern.quote(template.substring(lastAppendPosition)));
        regexBuilder.append("$");

        return Pattern.compile(regexBuilder.toString());
    }
}
