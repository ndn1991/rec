/**
 * 
 */
package com.adr.bigdata.search.handler.suggestion;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
@Deprecated
public class ModelSyncronizor implements Loggable {
	private final String path;
	private final List<String> hosts;
	private final String userName;

	public ModelSyncronizor(String path, String userName) {
		this.path = path;
		String[] solrHosts = System.getProperty("solrcloud.hosts", "10.220.75.133,10.220.75.134,10.220.75.135").split(
				",");
		hosts = new ArrayList<String>();
		this.userName = userName;

		for (String host : solrHosts) {
			hosts.add(host);
		}

	}

	public void watch() {
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path dir = Paths.get(path);
			dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			getLogger().info("starting watch change for model file....{}.... at hosts:...{}", path, hosts.toString());

			while (true) {
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException ex) {
					return;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path fileName = ev.context();

					if (kind == ENTRY_MODIFY || kind == ENTRY_CREATE) {
						sendFile(fileName);
					}
				}

				boolean valid = key.reset();
				if (!valid) {
					break;
				}
			}

		} catch (IOException ex) {
			getLogger().error("error registering file watcher....{}", ex.getMessage());
		}
	}

	private void sendFile(Path file) {
		assert hosts != null && !hosts.isEmpty();
		for (String host : hosts) {
			sendFile2Host(file, userName, host);
		}
	}

	private void sendFile2Host(Path file, String usr, String host) {
		String command = "scp " + file.toAbsolutePath().toString() + usr + "@" + host + ":" + path;
		executeCommand(command);
	}

	private String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output.toString();

	}

}
