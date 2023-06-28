package cn.korostudio.interaction.base.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class URLUtil {
    private static final Pattern PATTERN = Pattern.compile("\\{(.*?)\\}");

    public static boolean validateURLWithTemplate(String uri, String template) {
        Matcher m = PATTERN.matcher(template);

        String regex = "^" + template;
        while (m.find()) {
            String key = m.group(1);
            regex = regex.replace("{" + key + "}", "(.*?)");
        }
        regex += "$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uri);

        return matcher.matches();
    }

    public static Map<String, String> getParamsFromURLTemplate(String uri, String template) {
        Map<String, String> map = new HashMap<>();
        Matcher m = PATTERN.matcher(template);

        String regex = "^" + template;
        while (m.find()) {
            String key = m.group(1);
            regex = regex.replace("{" + key + "}", "(.*?)");
        }
        regex += "$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(uri);

        if (matcher.find()) {
            m.reset();
            int group = 1;
            while (m.find()) {
                String key = m.group(1);
                String value = matcher.group(group++);
                map.put(key, value);
            }
        }

        return map;
    }
}
