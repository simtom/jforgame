package com.kingston.jforgame.net.socket.codec.reflect.serializer;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.mina.core.buffer.IoBuffer;

import com.kingston.jforgame.net.socket.utils.ByteBuffUtil;

public class CollectionSerializer extends Serializer {

	@Override
	public Object decode(IoBuffer in, Class<?> type, Class<?> wrapper) {
		int size = ByteBuffUtil.readShort(in);
		int modifier = type.getModifiers();
		Collection<Object> result = null;

		if (Modifier.isAbstract(modifier) || Modifier.isInterface(modifier)) {
			if (List.class.isAssignableFrom(type)) {
				result = new ArrayList<>();
			} else if (Set.class.isAssignableFrom(type)) {
				result = new HashSet<>();
			}
		} else {
			try {
				result = (Collection)type.newInstance();
			}catch(Exception e) {
				e.printStackTrace();
				result = new ArrayList<>();
			}
		}

		for (int i=0; i<size; i++) {
			Serializer fieldCodec = Serializer.getSerializer(wrapper);
			Object eleValue = fieldCodec.decode(in, wrapper, null);
			result.add(eleValue);
		}

		return result;
	}

	@Override
	public void encode(IoBuffer out, Object value, Class<?> wrapper) {
		if (value == null) {
			ByteBuffUtil.writeShort(out, (short)0);
			return;
		}
		Collection<Object> collection = (Collection)value;
		int size = collection.size();
		ByteBuffUtil.writeShort(out, (short)size);

		for (Object elem:collection) {
			Class<?> clazz = elem.getClass();
			Serializer fieldCodec = Serializer.getSerializer(clazz);
			fieldCodec.encode(out, elem, wrapper);
		}
	}

}
