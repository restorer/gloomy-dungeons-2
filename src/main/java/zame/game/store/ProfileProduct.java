package zame.game.store;

import zame.game.engine.data.DataItem;
import zame.game.engine.data.DataReader;
import zame.game.engine.data.DataWriter;

import java.io.IOException;

public class ProfileProduct implements DataItem {
	protected static final int FIELD_PURCHASED = 1;
	protected static final int FIELD_VALUE = 2;

	public boolean _purchased = false; // for internal use. in other situations use profile.isPurchased
	public int value = 0;

	public void writeTo(DataWriter writer) throws IOException {
		writer.write(FIELD_PURCHASED, _purchased);
		writer.write(FIELD_VALUE, value);
	}

	public void readFrom(DataReader reader) {
		_purchased = reader.readBoolean(FIELD_PURCHASED);
		value = reader.readInt(FIELD_VALUE);
	}
}
