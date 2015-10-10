/**
 * 
 */
package com.adr.bigdata.search.handler.eventdriven.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.EventDispatcher;
import com.adr.bigdata.search.handler.eventdriven.EventHandler;

/**
 * @author minhvv2
 *
 */
public class BaseEventDispatcher implements EventDispatcher {

	private Map<String, List<EventHandler>> listeners;

	@Override
	public void addEventListener(String eventType, EventHandler listener) {
		if (this.listeners == null) {
			this.listeners = new ConcurrentHashMap<String, List<EventHandler>>();
		}
		if (!this.listeners.containsKey(eventType)) {
			this.listeners.put(eventType, new CopyOnWriteArrayList<EventHandler>());
		}
		this.listeners.get(eventType).add(listener);
	}

	@Override
	public void removeEventListener(String eventType, EventHandler listener) {
		if (this.listeners != null && this.listeners.containsKey(eventType)) {
			if (listener == null) {
				this.listeners.remove(eventType);
			} else {
				this.listeners.get(eventType).remove(listener);
			}
		}
	}

	@Override
	public void removeAllEvent() {
		this.listeners = null;
	}

	@Override
	public void dispatchEvent(Event event) {
		String eventType = event.getType();
		if (eventType != null && this.listeners != null && this.listeners.containsKey(eventType)) {
			if (event.getTarget() == null) {
				event.setTarget(this);
			}
			List<EventHandler> tmpListeners = this.listeners.get(eventType);
			if (tmpListeners.size() > 0) {
				try {
					for (EventHandler listener : tmpListeners) {
						listener.onEvent(event);
					}
				} catch (Exception e) {
					throw new RuntimeException("error while event handled", e);
				}
			}
		}
	}

	@Override
	public void dispatchEvent(String eventType, Object... data) {
		this.dispatchEvent(new GetFilterEvent(eventType, data));
	}

}
