package com.tantaman.jzombie;

import com.google.gson.annotations.Expose;

@SuppressWarnings("rawtypes")
public class ModelUpdateMessage {
	@Expose
	public String id;
	@Expose
	public String clientId;
	@Expose 
	public ModelCollectionCommon data;
	
	private ModelUpdateMessage() {}
	
	@Override
	public String toString() {
		return id + " " + clientId + " " + data;
	}
}
