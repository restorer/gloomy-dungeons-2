package zame.game.engine.data;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.zip.CRC32;

public class DataWriter {
	protected static class SignaturedObjectOutputStream {
		protected ObjectOutputStream os;
		protected CRC32 crc32 = new CRC32();

		public SignaturedObjectOutputStream(ObjectOutputStream os) {
			this.os = os;
			crc32.update(Common.INITIAL_SIGNATURE_VALUE);
		}

		public void writeSignature() throws IOException {
			os.writeLong(crc32.getValue());
		}

		public void writeBoolean(boolean value) throws IOException {
			crc32.update(value ? 1 : 0);
			os.writeBoolean(value);
		}

		public void writeByte(int value) throws IOException {
			crc32.update(value);
			os.writeByte(value);
		}

		public void writeChar(int value) throws IOException {
			crc32.update(value);
			os.writeChar(value);
		}

		public void writeDouble(double value) throws IOException {
			long raw = Double.doubleToRawLongBits(value);
			crc32.update((int)(raw & 0xFFFFFFFF));
			crc32.update((int)(raw >> 32));
			os.writeDouble(value);
		}

		public void writeFloat(float value) throws IOException {
			crc32.update(Float.floatToRawIntBits(value));
			os.writeFloat(value);
		}

		public void writeInt(int value) throws IOException {
			crc32.update(value);
			os.writeInt(value);
		}

		public void writeLong(long value) throws IOException {
			crc32.update((int)(value & 0xFFFFFFFF));
			crc32.update((int)(value >> 32));
			os.writeLong(value);
		}

		public void writeShort(int value) throws IOException {
			crc32.update(value);
			os.writeShort(value);
		}

		public void writeUTF(String value) throws IOException {
			try {
				crc32.update(value.getBytes("UTF-8"));
			} catch (Exception ex) {
			}

			os.writeUTF(value);
		}
	}

	protected SignaturedObjectOutputStream os;

	public static void writeTo(ObjectOutputStream oos, DataItem item) throws IOException {
		writeTo(oos, item, 1);
	}

	public static void writeTo(ObjectOutputStream oos, DataItem item, int version) throws IOException {
		SignaturedObjectOutputStream os = new SignaturedObjectOutputStream(oos);
		DataWriter writer = new DataWriter(os);

		os.writeUTF(Common.SIGNATURE + "." + String.valueOf(version));
		item.writeTo(writer);
		os.writeShort(Common.MARKER_END);
		os.writeSignature();
	}

	protected DataWriter(SignaturedObjectOutputStream os) {
		this.os = os;
	}

	public void write(int fieldId, DataItem value) throws IOException {
		os.writeShort((Common.TYPE_OBJECT << Common.SHIFT_TYPE) | fieldId);
		value.writeTo(this);
		os.writeShort(Common.MARKER_END);
	}

	public void write(int fieldId, byte value) throws IOException {
		os.writeShort((Common.TYPE_BYTE << Common.SHIFT_TYPE) | fieldId);
		os.writeByte(value);
	}

	public void write(int fieldId, short value) throws IOException {
		os.writeShort((Common.TYPE_SHORT << Common.SHIFT_TYPE) | fieldId);
		os.writeShort(value);
	}

	public void write(int fieldId, int value) throws IOException {
		os.writeShort((Common.TYPE_INT << Common.SHIFT_TYPE) | fieldId);
		os.writeInt(value);
	}

	public void write(int fieldId, long value) throws IOException {
		os.writeShort((Common.TYPE_LONG << Common.SHIFT_TYPE) | fieldId);
		os.writeLong(value);
	}

	public void write(int fieldId, float value) throws IOException {
		os.writeShort((Common.TYPE_FLOAT << Common.SHIFT_TYPE) | fieldId);
		os.writeFloat(value);
	}

	public void write(int fieldId, double value) throws IOException {
		os.writeShort((Common.TYPE_DOUBLE << Common.SHIFT_TYPE) | fieldId);
		os.writeDouble(value);
	}

	public void write(int fieldId, boolean value) throws IOException {
		os.writeShort((Common.TYPE_BOOLEAN << Common.SHIFT_TYPE) | fieldId);
		os.writeBoolean(value);
	}

	public void write(int fieldId, char value) throws IOException {
		os.writeShort((Common.TYPE_CHAR << Common.SHIFT_TYPE) | fieldId);
		os.writeChar(value);
	}

	public void write(int fieldId, String value) throws IOException {
		os.writeShort((Common.TYPE_STRING << Common.SHIFT_TYPE) | fieldId);
		os.writeUTF(value);
	}

	public void write(int fieldId, DataItem[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_OBJECT << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				list[i].writeTo(this);
				os.writeShort(Common.MARKER_END);
			}
		}
	}

	public void write(int fieldId, DataVector<?> list) throws IOException {
		os.writeShort((Common.TYPE_OBJECT << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
		int length = list.count;
		os.writeInt(length);

		for (int i = 0; i < length; i++) {
			((DataItem)list.data[i]).writeTo(this);
			os.writeShort(Common.MARKER_END);
		}
	}

	public void write(int fieldId, DataList<?> list) throws IOException {
		os.writeShort((Common.TYPE_OBJECT << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
		int length = list.count();
		os.writeInt(length);

		for (DataListItem item = list.first(); item != null; item = item.next) {
			((DataItem)item).writeTo(this);
			os.writeShort(Common.MARKER_END);
		}
	}

	public void writeList(int fieldId, List<?> list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_OBJECT << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.size();
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				((DataItem)list.get(i)).writeTo(this);
				os.writeShort(Common.MARKER_END);
			}
		}
	}

	public void writeList2d(int fieldId, List<?> list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_OBJECT << Common.SHIFT_TYPE) | (Common.ARRAY_2DV << Common.SHIFT_ARRAY) | fieldId);
			int length = list.size();
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				List<?> line = (List<?>)list.get(i);
				int lineLength = line.size();
				os.writeInt(lineLength);

				for (int j = 0; j < lineLength; j++) {
					((DataItem)line.get(j)).writeTo(this);
					os.writeShort(Common.MARKER_END);
				}
			}
		}
	}

	public void write(int fieldId, byte[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_BYTE << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeByte(list[i]);
			}
		}
	}

	public void write(int fieldId, short[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_SHORT << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeShort(list[i]);
			}
		}
	}

	public void write(int fieldId, int[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_INT << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeInt(list[i]);
			}
		}
	}

	public void write(int fieldId, long[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_LONG << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeLong(list[i]);
			}
		}
	}

	public void write(int fieldId, float[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_FLOAT << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeFloat(list[i]);
			}
		}
	}

	public void write(int fieldId, double[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_DOUBLE << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeDouble(list[i]);
			}
		}
	}

	public void write(int fieldId, boolean[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_BOOLEAN << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeBoolean(list[i]);
			}
		}
	}

	public void write(int fieldId, char[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_CHAR << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeChar(list[i]);
			}
		}
	}

	public void write(int fieldId, String[] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_STRING << Common.SHIFT_TYPE) | (Common.ARRAY_1D << Common.SHIFT_ARRAY) | fieldId);
			int length = list.length;
			os.writeInt(length);

			for (int i = 0; i < length; i++) {
				os.writeUTF(list[i]);
			}
		}
	}

	public void write(int fieldId, byte[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_BYTE << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeByte(list[i][j]);
				}
			}
		}
	}

	public void write(int fieldId, short[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_SHORT << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeShort(list[i][j]);
				}
			}
		}
	}

	public void write(int fieldId, int[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_INT << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeInt(list[i][j]);
				}
			}
		}
	}

	public void write(int fieldId, long[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_LONG << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeLong(list[i][j]);
				}
			}
		}
	}

	public void write(int fieldId, float[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_FLOAT << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeFloat(list[i][j]);
				}
			}
		}
	}

	public void write(int fieldId, double[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_DOUBLE << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeDouble(list[i][j]);
				}
			}
		}
	}

	public void write(int fieldId, boolean[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_BOOLEAN << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeBoolean(list[i][j]);
				}
			}
		}
	}

	public void write(int fieldId, char[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_CHAR << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeChar(list[i][j]);
				}
			}
		}
	}

	public void write(int fieldId, String[][] list) throws IOException {
		if (list == null) {
			os.writeShort((Common.TYPE_NULL << Common.SHIFT_TYPE) | fieldId);
		} else {
			os.writeShort((Common.TYPE_STRING << Common.SHIFT_TYPE) | (Common.ARRAY_2D << Common.SHIFT_ARRAY) | fieldId);
			int height = list.length;
			int width = list[0].length;
			os.writeInt(height);
			os.writeInt(width);

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					os.writeUTF(list[i][j]);
				}
			}
		}
	}
}
