package com.archivesmc.inter;

import org.apache.commons.lang.text.StrSubstitutor;

import java.util.HashMap;

public class Utils {
    public static String formatString(String input, HashMap<String, Object> values) {
        StrSubstitutor sub = new StrSubstitutor(values, "{", "}", '\\');

        return sub.replace(input);
    }
}
