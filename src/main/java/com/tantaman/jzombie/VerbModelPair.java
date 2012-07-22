package com.tantaman.jzombie;

import com.google.gson.annotations.Expose;

public class VerbModelPair<ModelType> {
	@Expose
	public String verb;
	@Expose
	public ModelType model;
	
	public VerbModelPair() {}
	
	@Override
	public String toString() {
		return verb + " " + model + " " + model.getClass();
	}
}
