package com.adr.bigdata.search.handler.db.sql.models;


public class ModelFactory {

//	private final DBIAdapter dbiAdapter;

	private ModelFactory(/*DBIAdapter dbiAdapter*/) {
//		this.dbiAdapter = dbiAdapter;
	}

	public static final ModelFactory newInstance(/*DBIAdapter dbiAdapter*/) {
		return new ModelFactory(/*dbiAdapter*/);
	}

	public <T extends AbstractModel> T getModel(Class<T> modelClass) {
		assert modelClass != null;
		try {
			T result = modelClass.newInstance();
//			result.setDbAdapter(this.dbiAdapter);
			if (result instanceof BaseCommunicationModel) {
				((BaseCommunicationModel) result).setModelFactory(this);
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}
}
