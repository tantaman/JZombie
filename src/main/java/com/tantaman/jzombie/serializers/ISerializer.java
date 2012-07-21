package com.tantaman.jzombie.serializers;

public interface ISerializer<SerializedType, UnserializedType> {
	public UnserializedType deserialize(SerializedType data, Class<UnserializedType> type);
	public SerializedType serialize(UnserializedType object);
}
