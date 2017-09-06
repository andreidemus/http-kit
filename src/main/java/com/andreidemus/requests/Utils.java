package com.andreidemus.requests;

import java.util.Map;
import java.util.stream.Collectors;

class Utils {
    static String prettyPrintMap(Map<String, ?> map) {
        if (map.isEmpty()) {
            return "{}";
        }
        return map.entrySet().stream()
                  .map(e -> "  " + e.getKey() + " : " + e.getValue())
                  .collect(Collectors.joining("\n", "{\n", "\n}"));
    }
}
