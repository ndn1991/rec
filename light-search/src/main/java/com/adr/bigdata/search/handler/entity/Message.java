/**
 * 
 */
package com.adr.bigdata.search.handler.entity;

import java.util.Map;

/**
 * @author minhvv2
 *
 */
public interface Message {
	int getType();

	void setType(int type);

	Map getData();

	void setData(Map data);

}
