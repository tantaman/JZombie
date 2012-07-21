package com.tantaman.jzombie.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GSonSerializer<T> implements ISerializer<String, T> {
	private final Gson builder;
	
	public GSonSerializer(Gson gson) {
		builder = gson;
	}
	
	public GSonSerializer() {
		builder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	}
	
	@Override
	public T deserialize(String data, Class<T> type) {
		return (T)builder.fromJson(data, type);
	}
	
	@Override
	public String serialize(T object) {
		return builder.toJson(object);
	}
}
