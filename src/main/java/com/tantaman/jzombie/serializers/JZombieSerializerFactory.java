package com.tantaman.jzombie.serializers;

import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.tantaman.jzombie.ModelCollectionCommon;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JZombieSerializerFactory implements ISerializerFactory {
	public static final JZombieSerializerFactory instance = new JZombieSerializerFactory();
	
	@Override
	public <T, SerializedType, UnserializedType> ISerializer<SerializedType, UnserializedType> createModelSerializer(
			ModelCollectionCommon<T> existingModelInstance) {
		GsonBuilder builder = new GsonBuilder();
		final T instance = (T)existingModelInstance;
		
		builder.registerTypeAdapter(this.getClass(), new InstanceCreator<T>() {
			@Override
			public T createInstance(Type arg0) {
				return instance;
			}
		}).excludeFieldsWithoutExposeAnnotation();
		
		return new GSonSerializer(builder.create());
	}

	@Override
	public <T, SerializedType, UnserializedType> ISerializer<SerializedType, UnserializedType> createCollectionSerializer(
			ModelCollectionCommon<T> existingCollectionlInstance) {
		return createModelSerializer(existingCollectionlInstance);
	}

	@Override
	public <T, SerializedType, UnserializedType> ISerializer<SerializedType, UnserializedType> createModelUpdateMessageSerializer(
			ModelCollectionCommon<T> existingModelInstance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T, SerializedType, UnserializedType> ISerializer<SerializedType, UnserializedType> createCollectionUpdateMessageSerializer(
			ModelCollectionCommon<T> existingCollectionInstance) {
		// TODO Auto-generated method stub
		return null;
	}

}
