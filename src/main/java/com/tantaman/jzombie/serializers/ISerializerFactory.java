package com.tantaman.jzombie.serializers;

import com.tantaman.jzombie.ModelCollectionCommon;

public interface ISerializerFactory {
	public <T, SerializedType, UnserializedType> ISerializer<SerializedType, UnserializedType> createModelSerializer(ModelCollectionCommon<T> existingModelInstance);
	public <T, SerializedType, UnserializedType> ISerializer<SerializedType, UnserializedType> createCollectionSerializer(ModelCollectionCommon<T> existingCollectionlInstance);
	public <T, SerializedType, UnserializedType> ISerializer<SerializedType, UnserializedType> createModelUpdateMessageSerializer(ModelCollectionCommon<T> existingModelInstance);
	public <T, SerializedType, UnserializedType> ISerializer<SerializedType, UnserializedType> createCollectionUpdateMessageSerializer(ModelCollectionCommon<T> existingCollectionInstance);
}
