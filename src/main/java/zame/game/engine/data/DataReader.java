package zame.game.engine.data;

import android.util.SparseArray;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class DataReader {
	protected static class SignaturedObjectInputStream {
		protected ObjectInputStream is;
		protected CRC32 crc32 = new CRC32();

		public SignaturedObjectInputStream(ObjectInputStream is) {
			this.is = is;
			crc32.update(Common.INITIAL_SIGNATURE_VALUE);
		}

		public boolean readAndCheckSignature() {
			try {
				long sig = is.readLong();
				return (sig == crc32.getValue());
			} catch (Exception ex) {
				return false;
			}
		}

		public boolean readBoolean() throws IOException {
			boolean value = is.readBoolean();
			crc32.update(value ? 1 : 0);
			return value;
		}

		public byte readByte() throws IOException {
			byte value = is.readByte();
			crc32.update(value);
			return value;
		}

		public char readChar() throws IOException {
			char value = is.readChar();
			crc32.update(value);
			return value;
		}

		public double readDouble() throws IOException {
			double value = is.readDouble();
			long raw = Double.doubleToRawLongBits(value);
			crc32.update((int)(raw & 0xFFFFFFFF));
			crc32.update((int)(raw >> 32));
			return value;
		}

		public float readFloat() throws IOException {
			float value = is.readFloat();
			crc32.update(Float.floatToRawIntBits(value));
			return value;
		}

		public int readInt() throws IOException {
			int value = is.readInt();
			crc32.update(value);
			return value;
		}

		public long readLong() throws IOException {
			long value = is.readLong();
			crc32.update((int)(value & 0xFFFFFFFF));
			crc32.update((int)(value >> 32));
			return value;
		}

		public short readShort() throws IOException {
			short value = is.readShort();
			crc32.update(value);
			return value;
		}

		public String readUTF() throws IOException {
			String value = is.readUTF();

			try {
				crc32.update(value.getBytes("UTF-8"));
			} catch (Exception ex) {
			}

			return value;
		}
	}

	protected static class ArrayList1d<T> extends ArrayList<T> {
		private static final long serialVersionUID = 0L;

		public ArrayList1d(int capacity) {
			super(capacity);
		}
	}

	protected static class ArrayList2d<T> extends ArrayList<T> {
		private static final long serialVersionUID = 0L;

		public ArrayList2d(int capacity) {
			super(capacity);
		}
	}

	protected static class ArrayList2dv<T> extends ArrayList<T> {
		private static final long serialVersionUID = 0L;

		public ArrayList2dv(int capacity) {
			super(capacity);
		}
	}

	public static int readFrom(ObjectInputStream ois, DataItem item) throws IOException, UnknownSignatureException {
		return readFrom(ois, item, 1);
	}

	public static int readFrom(ObjectInputStream ois, DataItem item, int maxSupportedVersion) throws IOException, UnknownSignatureException {
		SignaturedObjectInputStream is = new SignaturedObjectInputStream(ois);

		String signatureAndVersion = is.readUTF();
		int version = 0;

		if (signatureAndVersion == null) {
			throw new UnknownSignatureException(UnknownSignatureException.INVALID_SIGNATURE);
		}

		if (!signatureAndVersion.startsWith(Common.SIGNATURE + ".")) {
			throw new UnknownSignatureException(UnknownSignatureException.UNKNOWN_SIGNATURE);
		}

		try {
			version = Integer.parseInt(signatureAndVersion.substring(Common.SIGNATURE.length() + 1));
		} catch (NumberFormatException ex) {
			throw new UnknownSignatureException(UnknownSignatureException.INVALID_VERSION, ex);
		}

		if (version > maxSupportedVersion) {
			throw new UnknownSignatureException(UnknownSignatureException.UNSUPPORTED_VERSION);
		}

		DataReader reader = readInnerObject(is);
		item.readFrom(reader);

		reader = null;
		System.gc();

		if (!is.readAndCheckSignature()) {
			throw new UnknownSignatureException(UnknownSignatureException.INVALID_CHECKSUM);
		}

		return version;
	}

	@SuppressWarnings({"unchecked"})
	protected static DataReader readInnerObject(SignaturedObjectInputStream is) throws IOException {
		DataReader reader = new DataReader();

		for (;;) {
			short id = is.readShort();

			if (id == Common.MARKER_END) {
				break;
			}

			int arrayType = ((int)id & Common.MASK_ARRAY) >> Common.SHIFT_ARRAY;
			int dataType = ((int)id & Common.MASK_TYPE) >> Common.SHIFT_TYPE;
			int fieldId = (int)id & Common.MASK_FIELD_ID;

			if (arrayType == Common.ARRAY_1D) {
				int length = is.readInt();
				ArrayList1d<Object> list = new ArrayList1d<Object>(length);
				reader.values.put(fieldId, list);

				for (int i = 0; i < length; i++) {
					list.add(readInnerValue(dataType, is));
				}
			} else if (arrayType == Common.ARRAY_2D) {
				int height = is.readInt();
				int width = is.readInt();
				ArrayList2d<Object> list = new ArrayList2d<Object>(height);
				reader.values.put(fieldId, list);

				for (int i = 0; i < height; i++) {
					ArrayList<Object> line = new ArrayList<Object>(width);
					list.add(line);

					for (int j = 0; j < width; j++) {
						line.add(readInnerValue(dataType, is));
					}
				}
			} else if (arrayType == Common.ARRAY_2DV) {
				int length = is.readInt();
				ArrayList2dv<Object> list = new ArrayList2dv<Object>(length);
				reader.values.put(fieldId, list);

				for (int i = 0; i < length; i++) {
					int lineLength = is.readInt();
					ArrayList<Object> line = new ArrayList<Object>(lineLength);
					list.add(line);

					for (int j = 0; j < lineLength; j++) {
						line.add(readInnerValue(dataType, is));
					}
				}
			} else {
				reader.values.put(fieldId, readInnerValue(dataType, is));
			}
		}

		return reader;
	}

	protected static Object readInnerValue(int dataType, SignaturedObjectInputStream is) throws IOException {
		switch (dataType) {
			case Common.TYPE_OBJECT:
				return readInnerObject(is);

			case Common.TYPE_BYTE:
				return Byte.valueOf(is.readByte());

			case Common.TYPE_SHORT:
				return Short.valueOf(is.readShort());

			case Common.TYPE_INT:
				return Integer.valueOf(is.readInt());

			case Common.TYPE_LONG:
				return Long.valueOf(is.readLong());

			case Common.TYPE_FLOAT:
				return Float.valueOf(is.readFloat());

			case Common.TYPE_DOUBLE:
				return Double.valueOf(is.readDouble());

			case Common.TYPE_BOOLEAN:
				return Boolean.valueOf(is.readBoolean());

			case Common.TYPE_CHAR:
				return Character.valueOf(is.readChar());

			case Common.TYPE_STRING:
				return is.readUTF();

			case Common.TYPE_NULL:
				return null;
		}

		return null;
	}

	protected static final DataReader emptyDataReader = new DataReader();
	protected SparseArray<Object> values = new SparseArray<Object>();

	protected DataReader() {
	}

	public boolean has(int fieldId) {
		return (values.get(Integer.valueOf(fieldId)) != null);
	}

	public void readItem(int fieldId, DataItem item) {
		Object val = values.get(fieldId);
		item.readFrom(val instanceof DataReader ? (DataReader)val : emptyDataReader);
	}

	public void readDataVector(int fieldId, DataVector<?> dataVector) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = Math.min(dataVector.data.length, list.size());
			dataVector.count = length;

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);
				((DataItem)dataVector.data[i]).readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
			}
		} else {
			dataVector.count = 0;
		}
	}

	public void readDataList(int fieldId, DataList<?> dataList) {
		Object val = values.get(fieldId);
		dataList.clear();

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();

			for (int i = 0; i < length; i++) {
				DataItem item = (DataItem)dataList.take();

				if (item == null) {
					break;
				}

				Object v = list.get(i);
				item.readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
			}
		}
	}

	@SuppressWarnings({"unchecked"})
	public void readList(int fieldId, List<DataItem> resultList, Class<?> theClass) {
		Object val = values.get(fieldId);
		resultList.clear();

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				try {
					DataItem inst = (DataItem)theClass.newInstance();
					inst.readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
					resultList.add(inst);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	@SuppressWarnings({"unchecked"})
	public <T extends DataItem> void readList2d(int fieldId, List<ArrayList<T>> resultList, Class<?> theClass) {
		Object val = values.get(fieldId);
		resultList.clear();

		if (val instanceof ArrayList2dv) {
			ArrayList2dv<?> list = (ArrayList2dv<?>)val;
			int length = list.size();

			for (int i = 0; i < length; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);
				int lineLength = line.size();
				ArrayList<T> resultLine = new ArrayList<T>(lineLength);
				resultList.add(resultLine);

				for (int j = 0; j < lineLength; j++) {
					Object v = line.get(j);

					try {
						DataItem inst = (DataItem)theClass.newInstance();
						inst.readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
						resultLine.add((T)inst);
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		}
	}

	public void readObjectArray(int fieldId, DataItem[] resList) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = Math.min(resList.length, list.size());

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);
				resList[i].readFrom(v instanceof DataReader ? (DataReader)v : emptyDataReader);
			}
		}
	}

	public byte readByte(int fieldId, byte def) {
		Object val = values.get(fieldId);
		return (val instanceof Number ? ((Number)val).byteValue() : def);
	}

	public byte readByte(int fieldId) {
		return readByte(fieldId, (byte)0);
	}

	public short readShort(int fieldId, short def) {
		Object val = values.get(fieldId);
		return (val instanceof Number ? ((Number)val).shortValue() : def);
	}

	public short readShort(int fieldId) {
		return readShort(fieldId, (short)0);
	}

	public int readInt(int fieldId, int def) {
		Object val = values.get(fieldId);
		return (val instanceof Number ? ((Number)val).intValue() : def);
	}

	public int readInt(int fieldId) {
		return readInt(fieldId, 0);
	}

	public long readLong(int fieldId, long def) {
		Object val = values.get(fieldId);
		return (val instanceof Number ? ((Number)val).longValue() : def);
	}

	public long readLong(int fieldId) {
		return readLong(fieldId, 0);
	}

	public float readFloat(int fieldId, float def) {
		Object val = values.get(fieldId);
		return (val instanceof Number ? ((Number)val).floatValue() : def);
	}

	public float readFloat(int fieldId) {
		return readFloat(fieldId, 0.0f);
	}

	public boolean readBoolean(int fieldId, boolean def) {
		Object val = values.get(fieldId);
		return (val instanceof Boolean ? ((Boolean)val).booleanValue() : def);
	}

	public boolean readBoolean(int fieldId) {
		return readBoolean(fieldId, false);
	}

	public double readDouble(int fieldId, double def) {
		Object val = values.get(fieldId);
		return (val instanceof Number ? ((Number)val).doubleValue() : def);
	}

	public double readDouble(int fieldId) {
		return readDouble(fieldId, 0.0);
	}

	public char readChar(int fieldId, char def) {
		Object val = values.get(fieldId);
		return (val instanceof Character ? ((Character)val).charValue() : def);
	}

	public char readChar(int fieldId) {
		return readChar(fieldId, ' ');
	}

	public String readString(int fieldId, String def) {
		Object val = values.get(fieldId);
		return (val instanceof String ? (String)val : def);
	}

	public String readString(int fieldId) {
		return readString(fieldId, "");
	}

	public byte[] readByteArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			byte[] result = new byte[length];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).byteValue();
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public byte[] readByteArray(int fieldId, int minLength) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			byte[] result = new byte[Math.max(length, minLength)];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).byteValue();
				}
			}

			return result;
		} else {
			return new byte[minLength];
		}
	}

	public short[] readShortArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			short[] result = new short[length];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).shortValue();
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public short[] readShortArray(int fieldId, int minLength) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			short[] result = new short[Math.max(length, minLength)];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).shortValue();
				}
			}

			return result;
		} else {
			return new short[minLength];
		}
	}

	public int[] readIntArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			int[] result = new int[length];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).intValue();
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public int[] readIntArray(int fieldId, int minLength) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			int[] result = new int[Math.max(length, minLength)];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).intValue();
				}
			}

			return result;
		} else {
			return new int[minLength];
		}
	}

	public long[] readLongArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			long[] result = new long[length];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).longValue();
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public long[] readLongArray(int fieldId, int minLength) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			long[] result = new long[Math.max(length, minLength)];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).longValue();
				}
			}

			return result;
		} else {
			return new long[minLength];
		}
	}

	public float[] readFloatArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			float[] result = new float[length];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).floatValue();
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public float[] readFloatArray(int fieldId, int minLength) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			float[] result = new float[Math.max(length, minLength)];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).floatValue();
				}
			}

			return result;
		} else {
			return new float[minLength];
		}
	}

	public boolean[] readBooleanArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			boolean[] result = new boolean[length];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Boolean) {
					result[i] = ((Boolean)v).booleanValue();
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public boolean[] readBooleanArray(int fieldId, int minLength) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			boolean[] result = new boolean[Math.max(length, minLength)];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Boolean) {
					result[i] = ((Boolean)v).booleanValue();
				}
			}

			return result;
		} else {
			return new boolean[minLength];
		}
	}

	public double[] readDoubleArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			double[] result = new double[length];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).doubleValue();
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public double[] readDoubleArray(int fieldId, int minLength) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			double[] result = new double[Math.max(length, minLength)];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Number) {
					result[i] = ((Number)v).doubleValue();
				}
			}

			return result;
		} else {
			return new double[minLength];
		}
	}

	public char[] readCharArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			char[] result = new char[length];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Character) {
					result[i] = ((Character)v).charValue();
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public char[] readCharArray(int fieldId, int minLength) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)val;
			int length = list.size();
			char[] result = new char[Math.max(length, minLength)];

			for (int i = 0; i < length; i++) {
				Object v = list.get(i);

				if (v instanceof Character) {
					result[i] = ((Character)v).charValue();
				}
			}

			return result;
		} else {
			return new char[minLength];
		}
	}

	public String[] readStringArray(int fieldId) {
		if (values.get(fieldId) instanceof ArrayList1d) {
			ArrayList1d<?> list = (ArrayList1d<?>)values.get(fieldId);
			int length = list.size();
			String[] result = new String[length];

			for (int i = 0; i < length; i++) {
				if (list.get(i) instanceof String) {
					result[i] = (String)list.get(i);
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public byte[][] readByte2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			byte[][] result = new byte[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).byteValue();
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public byte[][] readByte2dArray(int fieldId, int minHeight, int minWidth) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			byte[][] result = new byte[Math.max(height, minHeight)][Math.max(width, minWidth)];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).byteValue();
					}
				}
			}

			return result;
		} else {
			return new byte[minHeight][minWidth];
		}
	}

	public short[][] readShort2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			short[][] result = new short[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).shortValue();
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public short[][] readShort2dArray(int fieldId, int minHeight, int minWidth) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			short[][] result = new short[Math.max(height, minHeight)][Math.max(width, minWidth)];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).shortValue();
					}
				}
			}

			return result;
		} else {
			return new short[minHeight][minWidth];
		}
	}

	public int[][] readInt2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			int[][] result = new int[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).intValue();
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public int[][] readInt2dArray(int fieldId, int minHeight, int minWidth) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			int[][] result = new int[Math.max(height, minHeight)][Math.max(width, minWidth)];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).intValue();
					}
				}
			}

			return result;
		} else {
			return new int[minHeight][minWidth];
		}
	}

	public long[][] readLong2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			long[][] result = new long[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).longValue();
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public long[][] readLong2dArray(int fieldId, int minHeight, int minWidth) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			long[][] result = new long[Math.max(height, minHeight)][Math.max(width, minWidth)];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).longValue();
					}
				}
			}

			return result;
		} else {
			return new long[minHeight][minWidth];
		}
	}

	public float[][] readFloat2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			float[][] result = new float[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).floatValue();
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public float[][] readFloat2dArray(int fieldId, int minHeight, int minWidth) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			float[][] result = new float[Math.max(height, minHeight)][Math.max(width, minWidth)];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).floatValue();
					}
				}
			}

			return result;
		} else {
			return new float[minHeight][minWidth];
		}
	}

	public boolean[][] readBoolean2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			boolean[][] result = new boolean[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Boolean) {
						result[i][j] = ((Boolean)v).booleanValue();
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public boolean[][] readBoolean2dArray(int fieldId, int minHeight, int minWidth) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			boolean[][] result = new boolean[Math.max(height, minHeight)][Math.max(width, minWidth)];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Boolean) {
						result[i][j] = ((Boolean)v).booleanValue();
					}
				}
			}

			return result;
		} else {
			return new boolean[minHeight][minWidth];
		}
	}

	public double[][] readDouble2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			double[][] result = new double[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).doubleValue();
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public double[][] readDouble2dArray(int fieldId, int minHeight, int minWidth) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			double[][] result = new double[Math.max(height, minHeight)][Math.max(width, minWidth)];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Number) {
						result[i][j] = ((Number)v).doubleValue();
					}
				}
			}

			return result;
		} else {
			return new double[minHeight][minWidth];
		}
	}

	public char[][] readChar2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			char[][] result = new char[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Character) {
						result[i][j] = ((Character)v).charValue();
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}

	public char[][] readChar2dArray(int fieldId, int minHeight, int minWidth) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			char[][] result = new char[Math.max(height, minHeight)][Math.max(width, minWidth)];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof Character) {
						result[i][j] = ((Character)v).charValue();
					}
				}
			}

			return result;
		} else {
			return new char[minHeight][minWidth];
		}
	}

	public String[][] readString2dArray(int fieldId) {
		Object val = values.get(fieldId);

		if (val instanceof ArrayList2d) {
			ArrayList2d<?> list = (ArrayList2d<?>)val;
			int height = list.size();
			int width = ((ArrayList<?>)list.get(0)).size();
			String[][] result = new String[height][width];

			for (int i = 0; i < height; i++) {
				ArrayList<?> line = (ArrayList<?>)list.get(i);

				for (int j = 0; j < width; j++) {
					Object v = line.get(j);

					if (v instanceof String) {
						result[i][j] = (String)v;
					}
				}
			}

			return result;
		} else {
			return null;
		}
	}
}
