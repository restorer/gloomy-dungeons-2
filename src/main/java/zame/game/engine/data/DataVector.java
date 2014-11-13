package zame.game.engine.data;

import java.lang.reflect.Array;

public class DataVector<T> {
	public T[] data;
	public int count;

	@SuppressWarnings({"unchecked"})
	public DataVector(Class<T> theClass, int capacity) {
		data = (T[])Array.newInstance(theClass, capacity);
		count = 0;

		for (int i = 0; i < capacity; i++) {
			try {
				data[i] = theClass.newInstance();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
