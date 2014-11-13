package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class Mark extends DataListItem implements DataItem {
	protected static final int FIELD_ID = 1;
	protected static final int FIELD_X = 2;
	protected static final int FIELD_Y = 3;

	public int id;
	public int x;
	public int y;

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_ID, id);
		writer.write(FIELD_X, x);
		writer.write(FIELD_Y, y);
	}

	public void readFrom(DataReader reader) {
		id = reader.readInt(FIELD_ID);
		x = reader.readInt(FIELD_X);
		y = reader.readInt(FIELD_Y);
	}
}
