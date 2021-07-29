package de.iltisauge.transport.utils;

import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Because Java.
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CastUtil {

	public static <T> T cast(Object obj) {
		return (T) obj;
	}

	public static <T> List<T> castList(List<?> list) {
		return (List<T>) list;
	}

	public static <T> List<T> castList(Class<T> target, List<?> list) {
		return (List<T>) list;
	}

	public static <T> Set<T> castSet(Set<?> set) {
		return (Set<T>) set;
	}

	public static <T> Iterable<T> castIterable(Iterable<?> iterable) {
		return (Iterable<T>) iterable;
	}

	public static <T> List<T>[] castArrayGenericList(List<?>[] array) {
		return (List<T>[]) array;
	}

	public static <T> Set<T>[] castArrayGenericSet(Set<?>[] array) {
		return (Set<T>[]) array;
	}

	public static <T extends Enum<T>> T getEnumValue(Class<T> e, Enum<?> name) {
		return (T) Enum.valueOf((Class<T>) e, name.toString());
	}
}
