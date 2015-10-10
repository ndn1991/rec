package com.adr.bigdata.search.handler.db.sql.models;

import com.nhb.common.Loggable;

/*
 * data model: get data from hazelcast, return null if there's error connecting hazelcast
 */
public class AbstractModel implements Loggable {

	//	private DBIAdapter dbAdapter;
	//
	//	protected DBIAdapter getDbAdapter() {
	//		return dbAdapter;
	//	}
	//
	//	void setDbAdapter(DBIAdapter dbAdapter) {
	//		this.dbAdapter = dbAdapter;
	//	}
	//
	//	protected <T extends AbstractDAO> T openDAO(Class<T> daoClass) {
	//		assert daoClass != null;
	//		getLogger().error("debug ", new Exception());
	//		return this.dbAdapter.openDAO(daoClass);
	//	}
}
