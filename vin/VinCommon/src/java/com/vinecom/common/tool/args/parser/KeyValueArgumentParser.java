package com.vinecom.common.tool.args.parser;

import java.util.HashMap;

/**
 * Created by ndn on 7/23/2015.
 */
public class KeyValueArgumentParser extends HashMap<String, String> {

	private static final long serialVersionUID = 8173811153457904145L;

	public KeyValueArgumentParser() {

	}

	public KeyValueArgumentParser(String[] args) {
		super();
		parse(args);
	}

	public String get(String key) {
		return super.get(key);
	}

	public boolean isKeyExist(String key) {
		return super.containsKey(key);
	}

	public boolean hasValue(String key) {
		return isKeyExist(key) && !get(key).isEmpty();
	}

	public void parse(String[] args) {
		String key = null;
		for (String arg : args) {
			if (arg.startsWith("-") || arg.startsWith("--")) {
				key = arg;
				super.put(key, "");
			} else {
				if (key == null) {
					throw new IllegalArgumentException("Wrong argument: " + arg);
				}
				super.put(key, arg);
				key = null;
			}
		}
	}
}
