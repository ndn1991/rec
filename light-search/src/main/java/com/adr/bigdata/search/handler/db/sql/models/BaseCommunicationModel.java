package com.adr.bigdata.search.handler.db.sql.models;

public class BaseCommunicationModel extends AbstractModel {

	private ModelFactory modelFactory;

	public ModelFactory getModelFactory() {
		return modelFactory;
	}

	public void setModelFactory(ModelFactory modelFactory) {
		this.modelFactory = modelFactory;
	}
}
