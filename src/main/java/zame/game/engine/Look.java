package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class Look extends DataListItem implements DataItem {
	protected static final int FIELD_MARK_ID = 1;
	protected static final int FIELD_X = 2;
	protected static final int FIELD_Y = 3;

	public int markId;
	public int x;
	public int y;

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_MARK_ID, markId);
		writer.write(FIELD_X, x);
		writer.write(FIELD_Y, y);
	}

	public void readFrom(DataReader reader) {
		markId = reader.readInt(FIELD_MARK_ID);
		x = reader.readInt(FIELD_X);
		y = reader.readInt(FIELD_Y);
	}
}
