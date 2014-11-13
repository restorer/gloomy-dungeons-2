package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class Timeout extends DataListItem implements DataItem {
	protected static final int FIELD_MARK_ID = 1;
	protected static final int FIELD_DELAY = 2;

	public int markId;
	public int delay;

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_MARK_ID, markId);
		writer.write(FIELD_DELAY, delay);
	}

	public void readFrom(DataReader reader) {
		markId = reader.readInt(FIELD_MARK_ID);
		delay = reader.readInt(FIELD_DELAY);
	}
}
