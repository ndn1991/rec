/**
 * 
 */
package com.adr.bigdata.search.handler.eventdriven;


/**
 * @author minhvv2
 *
 */
public interface EventHandler {
	public void onEvent(Event event) throws Exception;
}
