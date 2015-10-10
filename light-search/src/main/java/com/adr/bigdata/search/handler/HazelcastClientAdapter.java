/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler;

import java.io.IOException;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.nhb.common.utils.FileSystemUtils;

/**
 *
 * @author Tong Hoang Anh
 */
public class HazelcastClientAdapter {

	private static HazelcastClientAdapter instance;	

	public static HazelcastClientAdapter getInstance() {
		if (instance == null) {
			instance = new HazelcastClientAdapter();
			addShutdownHook();
		}
		return instance;
	}

	public static <K, V> IMap<K, V> getMap(String name) {
		return getInstance().getHazelcast().getMap(name);
	}

	public static <E> IList<E> getList(String name) {
		return getInstance().getHazelcast().getList(name);
	}

	public static IAtomicLong getLong(String name) {
		return getInstance().getHazelcast().getAtomicLong(name);
	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {

			{
				this.setPriority(MAX_PRIORITY);
			}

			@Override
			public void run() {
				if (instance != null) {
					// instance.shutdown();
				}
			}
		});
	}

	private HazelcastInstance hazelcast;

	private HazelcastClientAdapter() {
		ClientConfig config;
		try {
			config = new XmlClientConfigBuilder(FileSystemUtils.createAbsolutePathFrom("conf", "hazelcast-client.xml"))
					.build();
			// Config config = new XmlConfigBuilder(FileSystemUtils.createAbsolutePathFrom("conf", "hazelcast.xml")).build();
			config.setClassLoader(getClass().getClassLoader());
			this.hazelcast = HazelcastClient.newHazelcastClient(config);
		} catch (IOException e) {
			throw new RuntimeException("error while trying to create hazelcast client", e);
		}
	}

	public void shutdown() {
		if (this.hazelcast != null) {
			try {
				this.hazelcast.shutdown();
			} catch (Exception ex) {
				System.err.println("error while trying to shutdown hazelcast client...");
				ex.printStackTrace();
			}
		}
	}

	public HazelcastInstance getHazelcast() {
		return hazelcast;
	}
}
