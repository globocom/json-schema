package org.everit.json.schema;

import org.json.JSONObject;

import java.util.Iterator;

public class JSONObjectUtils {
    public static String[] getNames(JSONObject json) {
        int length = json.length();
        if (length == 0) {
            return null;
        }
        Iterator<String> iterator = json.keys();
        String[] names = new String[length];

        for (int i = 0; iterator.hasNext(); ++i) {
            names[i] = (String) iterator.next();
        }

        return names;
    }

    public static <T> T requireNonNull(T obj, String msg) {
        if (obj == null) {
            throw new NullPointerException(msg);
        }
        return obj;
    }
}
