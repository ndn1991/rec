package com.vinecom.common.tool.args.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ndn on 7/23/2015.
 */
public class KeyValueArgumentParser {
    private Map<String, String> map = null;

    public KeyValueArgumentParser() {

    }

    public KeyValueArgumentParser(String[] args) {
        parse(args);
    }

    public String get(String key) {
        return this.map.get(key);
    }

    public boolean isKeyExist(String key) {
        return this.map.containsKey(key);
    }

    public  boolean hasValue(String key) {
        return isKeyExist(key) && !get(key).isEmpty();
    }

    public void parse(String[] args) {
        this.map = new HashMap<>();

        String key = null;
        for (String arg : args) {
            if (arg.startsWith("-") || arg.startsWith("--")) {
                key = arg;
                this.map.put(key, "");
            } else {
                if (key == null) {
                    throw new IllegalArgumentException("Wrong argument: " + arg);
                }
                map.put(key, arg);
                key = null;
            }
        }
    }
}
