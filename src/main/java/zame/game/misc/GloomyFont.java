package zame.game.misc;

import android.graphics.Typeface;
import org.holoeverywhere.FontLoader;
import zame.game.Common;
import zame.game.R;

public class GloomyFont extends FontLoader.Font {
	public GloomyFont() {
		setFontFamily("gloomy");
	}

	public GloomyFont(FontLoader.Font font) {
		super(font);
		setFontFamily("gloomy");
	}

	@Override
	public GloomyFont clone() {
		return new GloomyFont(this);
	}

	@Override
	public Typeface getTypeface(String fontFamily, int fontStyle) {
		if ("gloomy".equals(getContext().getString(R.string.typeface_interface))
			|| (fontFamily != null && fontFamily.equals(getFontFamily()))
		) {
			return super.getTypeface(fontFamily, fontStyle);
		} else {
			// FontLoader.Font.setContext() - моя кастомная функа
			FontLoader.ROBOTO.setContext(getContext());
			return FontLoader.ROBOTO.getTypeface(fontFamily, fontStyle);
		}
	}

	@Override
	public Typeface loadTypeface() {
		return Common.loadTypeface();
	}
}
