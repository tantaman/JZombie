package com.tantaman.jzombie;

import com.google.gson.annotations.Expose;

public class ModelUpdateMessage<ModelType> {
	@Expose
	public String verb;
	@Expose
	public ModelType model;
	
	private ModelUpdateMessage() {}
}
