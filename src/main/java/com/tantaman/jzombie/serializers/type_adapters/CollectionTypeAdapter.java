package com.tantaman.jzombie.serializers.type_adapters;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.tantaman.jzombie.Collection;

public class CollectionTypeAdapter extends TypeAdapter<Collection> {
	public CollectionTypeAdapter() {
	}
	
	@Override
	public Collection read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}

		reader.beginArray();

		return null;
	}

	@Override
	public void write(JsonWriter writer, Collection arg1) throws IOException {
		// TODO Auto-generated method stub

	}

}
