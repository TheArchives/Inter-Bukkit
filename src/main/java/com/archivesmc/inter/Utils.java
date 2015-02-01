package com.archivesmc.inter;

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.Map;

public class Utils {
    public static String formatString(String input, Map<String, Object> values) {
        StrSubstitutor sub = new StrSubstitutor(values, "{", "}", '\\');

        return sub.replace(input);
    }
}
