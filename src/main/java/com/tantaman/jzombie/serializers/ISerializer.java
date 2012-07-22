package com.tantaman.jzombie.serializers;

import java.lang.reflect.Type;

public interface ISerializer<SerializedType, UnserializedType> {
	public UnserializedType deserialize(SerializedType data, Class<UnserializedType> type);
	public UnserializedType deserialize(SerializedType data, Type nonErasedType);
	public SerializedType serialize(UnserializedType object);
}
