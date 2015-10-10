/**
 * 
 */
package com.adr.bigdata.search.handler.eventdriven;

/**
 * @author minhvv2
 *
 */
public interface EventDispatcher {
	public void addEventListener(String eventType, EventHandler listener);

	public void removeEventListener(String eventType, EventHandler listener);

	public void removeAllEvent();

	public void dispatchEvent(Event event);

	public void dispatchEvent(String eventType, Object... data);
}
