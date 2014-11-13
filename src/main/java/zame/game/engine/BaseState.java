package zame.game.engine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import zame.game.Common;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public abstract class BaseState implements DataItem {
	public static final int LOAD_RESULT_SUCCESS = 0;
	public static final int LOAD_RESULT_NOT_FOUND = 1;
	public static final int LOAD_RESULT_ERROR = 2;

	public abstract void writeTo(DataWriter writer) throws IOException;
	public abstract void readFrom(DataReader reader);

	protected int getVersion() {
		return 4;
	}

	protected void versionUpgrade(int version) {
	}

	public boolean save(String path) {
		String tmpPath = path + ".tmp";

		try {
			FileOutputStream fo = new FileOutputStream(tmpPath, false);
			ObjectOutputStream os = new ObjectOutputStream(fo);

			DataWriter.writeTo(os, this, getVersion());

			os.flush();
			fo.close();

			Common.safeRename(tmpPath, path);
			return true;
		} catch (Exception ex) {
			Common.log(ex.toString());
			return false;
		}
	}

	public int load(String path) {
		try {
			FileInputStream fi = new FileInputStream(path);
			ObjectInputStream is = new ObjectInputStream(fi);

			versionUpgrade(DataReader.readFrom(is, this, getVersion()));
			return LOAD_RESULT_SUCCESS;
		} catch (FileNotFoundException ex) {
			return LOAD_RESULT_NOT_FOUND;
		} catch (Exception ex) {
			Common.log(ex.toString());
			return LOAD_RESULT_ERROR;
		}
	}
}
