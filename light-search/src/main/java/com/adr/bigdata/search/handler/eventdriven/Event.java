package com.adr.bigdata.search.handler.eventdriven;

public interface Event {

	public Callable getCallBack();

	public void setCallBack(Callable callback);

	public void setType(String type);

	public String getType();

	public <T extends EventDispatcher> T getTarget();

	public void setTarget(EventDispatcher target);

}
