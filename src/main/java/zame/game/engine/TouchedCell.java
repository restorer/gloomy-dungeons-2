package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class TouchedCell extends DataListItem implements DataItem {
	protected static final int FIELD_X = 1;
	protected static final int FIELD_Y = 2;

	public int x;
	public int y;

	public void initFrom(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void copyFrom(TouchedCell tc) {
		x = tc.x;
		y = tc.y;
	}

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_X, x);
		writer.write(FIELD_Y, y);
	}

	public void readFrom(DataReader reader) {
		x = reader.readInt(FIELD_X);
		y = reader.readInt(FIELD_Y);
	}
}
