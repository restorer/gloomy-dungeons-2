package zame.game.engine;

import java.io.IOException;
import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataListItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

public class AutoWall extends DataListItem implements DataItem {
	protected static final int FIELD_FROM_X = 1;
	protected static final int FIELD_FROM_Y = 2;
	protected static final int FIELD_TO_X = 3;
	protected static final int FIELD_TO_Y = 4;
	protected static final int FIELD_VERT = 5;
	protected static final int FIELD_TYPE = 6;
	protected static final int FIELD_DOOR_UID = 7;

	public float fromX;
	public float fromY;
	public float toX;
	public float toY;
	public boolean vert;
	public int type;
	public int doorUid; // required for save/load
	public Door door;

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_FROM_X, fromX);
		writer.write(FIELD_FROM_Y, fromY);
		writer.write(FIELD_TO_X, toX);
		writer.write(FIELD_TO_Y, toY);
		writer.write(FIELD_VERT, vert);
		writer.write(FIELD_TYPE, type);
		writer.write(FIELD_DOOR_UID, doorUid);
	}

	public void readFrom(DataReader reader) {
		fromX = reader.readFloat(FIELD_FROM_X);
		fromY = reader.readFloat(FIELD_FROM_Y);
		toX = reader.readFloat(FIELD_TO_X);
		toY = reader.readFloat(FIELD_TO_Y);
		vert = reader.readBoolean(FIELD_VERT);
		type = reader.readInt(FIELD_TYPE);
		doorUid = reader.readInt(FIELD_DOOR_UID);
	}
}
