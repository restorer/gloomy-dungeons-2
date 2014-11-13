package zame.game.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLUtils;
import java.util.HashMap;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.store.Achievements;

public class Labels implements EngineObject {
	protected static final int TEX_WIDTH = 1024;
	protected static final int TEX_HEIGHT = 1024;
	protected static final int TEX_WIDTH_LOW = 512;
	protected static final int TEX_HEIGHT_LOW = 512;

	public static final int ALIGN_BL = 0; // bottom left
	public static final int ALIGN_CC = 1; // center center
	public static final int ALIGN_CL = 2; // center left
	public static final int ALIGN_CR = 3; // center left

	public static final int LABEL_FPS = 0;
	public static final int LABEL_CANT_OPEN = 1;
	public static final int LABEL_NEED_BLUE_KEY = 2;
	public static final int LABEL_NEED_RED_KEY = 3;
	public static final int LABEL_NEED_GREEN_KEY = 4;
	public static final int LABEL_SECRET_FOUND = 5;
	public static final int LABEL_ENDL_KILLS = 6;
	public static final int LABEL_ENDL_ITEMS = 7;
	public static final int LABEL_ENDL_SECRETS = 8;
	public static final int LABEL_ENDL_TIME = 9;
	public static final int LABEL_GAMEOVER = 10;
	public static final int LABEL_GAMEOVER_LOAD_AUTOSAVE = 11;
	public static final int LABEL_ACHIEVEMENT_UNLOCKED = 12;
	public static final int LABEL_HELP_MOVE = 13;
	public static final int LABEL_HELP_MENU = 14;
	public static final int LABEL_HELP_FIRE = 15;
	public static final int LABEL_HELP_MAP = 16;
	public static final int LABEL_HELP_ROTATE = 17;
	public static final int LABEL_MESSAGE_DOOR = 18; // must == 18, or change also in convert-levels.rb
	public static final int LABEL_MESSAGE_SWITCH = 19; // must == 19, or change also in convert-levels.rb
	public static final int LABEL_MESSAGE_TURN_LEFT = 20; // must == 20, or change also in convert-levels.rb
	public static final int LABEL_MESSAGE_TURN_RIGHT = 21; //  must == 21, or change also in convert-levels.rb
	public static final int LABEL_MESSAGE_THIS_IS_WINDOW = 22; //  must == 22, or change also in convert-levels.rb
	public static final int LABEL_LAST = 23;

	public String[] map = new String[LABEL_LAST];

	protected Engine engine;
	protected Config config;
	protected Renderer renderer;
	protected TextureLoader textureLoader;
	protected Paint paint;
	protected HashMap<Character, Rect> charMap = new HashMap<Character, Rect>();
	protected Rect[] numberMap = new Rect[11];
	protected int lastTexX;
	protected int lastTexY;
	protected int textAscent;
	protected int textHeight;
	protected int spaceWidth;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.config = engine.config;
		this.renderer = engine.renderer;
		this.textureLoader = engine.textureLoader;
	}

	public void init() {
		Typeface typeface = Common.loadTypeface();

		if (typeface == null) {
			typeface = Typeface.DEFAULT;
		}

		paint = new Paint();
		paint.setTypeface(typeface);
		paint.setAntiAlias(true);
		paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
		paint.setTextSize(64);
	}

	public void createLabels(GL10 gl) {
		Bitmap bitmap = Common.createBitmap(TEX_WIDTH, TEX_HEIGHT, TEX_WIDTH_LOW, TEX_HEIGHT_LOW, "Can't alloc bitmap for labels");
		Canvas canvas = new Canvas(bitmap);
		canvas.setDensity(Bitmap.DENSITY_NONE);

		MyApplication context = MyApplication.self;
		charMap.clear();
		lastTexX = 0;
		lastTexY = 0;

		textAscent = (int)Math.ceil(-paint.ascent()); // Paint.ascent is negative, so negate it
		textHeight = textAscent + (int)Math.ceil(paint.descent());
		spaceWidth = (int)Math.ceil(paint.measureText(" "));

		appendChars(canvas, "0123456789-");

		map[LABEL_FPS] = appendChars(canvas, context.getString(R.string.lbl_fps));
		map[LABEL_CANT_OPEN] = appendChars(canvas, context.getString(R.string.lbl_cant_open_door));
		map[LABEL_NEED_BLUE_KEY] = appendChars(canvas, context.getString(R.string.lbl_need_blue_key));
		map[LABEL_NEED_RED_KEY] = appendChars(canvas, context.getString(R.string.lbl_need_red_key));
		map[LABEL_NEED_GREEN_KEY] = appendChars(canvas, context.getString(R.string.lbl_need_green_key));
		map[LABEL_SECRET_FOUND] = appendChars(canvas, context.getString(R.string.lbl_secret_found));
		map[LABEL_ENDL_KILLS] = appendChars(canvas, context.getString(R.string.lbl_endl_kills));
		map[LABEL_ENDL_ITEMS] = appendChars(canvas, context.getString(R.string.lbl_endl_items));
		map[LABEL_ENDL_SECRETS] = appendChars(canvas, context.getString(R.string.lbl_endl_secrets));
		map[LABEL_ENDL_TIME] = appendChars(canvas, context.getString(R.string.lbl_endl_time));
		map[LABEL_GAMEOVER] = appendChars(canvas, context.getString(R.string.lbl_gameover));
		map[LABEL_GAMEOVER_LOAD_AUTOSAVE] = appendChars(canvas, context.getString(R.string.lbl_gameover_load_autosave));
		map[LABEL_ACHIEVEMENT_UNLOCKED] = appendChars(canvas, context.getString(R.string.ac_unlocked));
		map[LABEL_HELP_MOVE] = appendChars(canvas, context.getString(R.string.lblh_move));
		map[LABEL_HELP_MENU] = appendChars(canvas, context.getString(R.string.lblh_menu));
		map[LABEL_HELP_FIRE] = appendChars(canvas, context.getString(R.string.lblh_fire));
		map[LABEL_HELP_MAP] = appendChars(canvas, context.getString(R.string.lblh_map));
		map[LABEL_HELP_ROTATE] = appendChars(canvas, context.getString(R.string.lblh_rotate));
		map[LABEL_MESSAGE_DOOR] = appendChars(canvas, context.getString(R.string.lblm_door));
		map[LABEL_MESSAGE_SWITCH] = appendChars(canvas, context.getString(R.string.lblm_switch));
		map[LABEL_MESSAGE_TURN_LEFT] = appendChars(canvas, context.getString(R.string.lblm_turn_left));
		map[LABEL_MESSAGE_TURN_RIGHT] = appendChars(canvas, context.getString(R.string.lblm_turn_right));
		map[LABEL_MESSAGE_THIS_IS_WINDOW] = appendChars(canvas, context.getString(R.string.lblm_this_is_window));

		for (int i = 0, len = Achievements.LIST.length; i < len; i++) {
			appendChars(canvas, Achievements.cleanupTitle(context.getString(Achievements.LIST[i].titleResourceId)));
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureLoader.textures[TextureLoader.TEXTURE_LABELS]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		canvas = null;
		bitmap.recycle();
		bitmap = null;
		System.gc();
	}

	protected String appendChars(Canvas canvas, String str) {
		for (int i = 0, len = str.length(); i < len; i++) {
			char ch = str.charAt(i);

			if (ch != ' ' && !charMap.containsKey(ch)) {
				String chStr = String.valueOf(ch);
				int textWidth = (int)Math.ceil(paint.measureText(chStr));

				if ((lastTexX + textWidth + 1) >= TEX_WIDTH) {
					if ((lastTexY + textHeight + 1) >= TEX_HEIGHT) {
						Common.log("Labels.appendChars: no free texture space");
						continue;
					}

					lastTexX = 0;
					lastTexY += (textHeight + 2);
				}

				Rect rect = new Rect(lastTexX + 1, lastTexY + 1, lastTexX + textWidth + 1, lastTexY + textHeight + 1);
				canvas.drawText(chStr, (float)(lastTexX + 1), (float)(lastTexY + textAscent + 1), paint);
				lastTexX += textWidth + 2;

				charMap.put(ch, rect);

				if (ch == '-') {
					numberMap[10] = rect;
				} else if (ch >= '0' && ch <= '9') {
					numberMap[Character.digit(ch, 10)] = rect;
				}
			}
		}

		return str;
	}

	protected float drawCharacter(Rect rect, float xpos, float ypos, float scale) {
		if (rect == null) {
			return (float)spaceWidth * scale;
		}

		renderer.setQuadTexCoords(
			(rect.left << 16) / TEX_WIDTH,
			(rect.bottom << 16) / TEX_HEIGHT,
			(rect.right << 16) / TEX_WIDTH,
			(rect.top << 16) / TEX_HEIGHT
		);

		renderer.setQuadOrthoCoords(
			xpos,
			ypos,
			xpos + (float)(rect.width() - 1) * scale,
			ypos + (float)(textHeight - 1) * scale
		);

		renderer.drawQuad();
		return (float)rect.width() * scale;
	}

	public void beginDrawing(GL10 gl) {
		beginDrawing(gl, false);
	}

	public void beginDrawing(GL10 gl, boolean customGlConfig) {
		if (!customGlConfig) {
			renderer.initOrtho(gl, true, true, -engine.ratio, engine.ratio, -1.0f, 1.0f, 0.0f, 1.0f);
			gl.glShadeModel(GL10.GL_FLAT);
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDisable(GL10.GL_DEPTH_TEST);
		}

		renderer.init();
	}

	public void draw(
		GL10 gl,
		float sx, float sy, float ex, float ey,
		String str, float desiredHeight, int align
	) {
		float scale = desiredHeight / (float)textHeight;
		float width = (float)getWidth(str);

		if ((width * scale) > (ex - sx)) {
			scale = (ex - sx) / width;
		}

		if (align == ALIGN_CC || align == ALIGN_CL || align == ALIGN_CR) {
			sy += (ey - sy - (float)textHeight * scale) * 0.5f;
		}

		if (align == ALIGN_CC) {
			sx += (ex - sx - width * scale) * 0.5f;
		} else if (align == ALIGN_CR) {
			sx = ex - width * scale;
		}

		for (int i = 0, len = str.length(); i < len; i++) {
			char ch = str.charAt(i);
			sx += drawCharacter((ch == ' ' ? null : charMap.get(ch)), sx, sy, scale);
		}
	}

	public void draw(
		GL10 gl,
		float sx, float sy, float ex, float ey,
		int value, float desiredHeight, int align
	) {
		float scale = desiredHeight / (float)textHeight;
		float width = (float)getWidth(value);

		if ((width * scale) > (ex - sx)) {
			scale = (ex - sx) / width;
		}

		if (align == ALIGN_CC || align == ALIGN_CL || align == ALIGN_CR) {
			sy += (ey - sy - (float)textHeight * scale) * 0.5f;
		}

		if (align == ALIGN_CC) {
			sx += (ex - sx - width * scale) * 0.5f;
		} else if (align == ALIGN_CR) {
			sx = ex - width * scale;
		}

		if (value == 0) {
			drawCharacter(numberMap[0], sx, sy, scale);
			return;
		}

		if (value < 0) {
			sx += drawCharacter(numberMap[10], sx, sy, scale);
			value = -value;
		}

		int divider = 1;
		int tmpValue = value / 10;

		while (tmpValue > 0) {
			divider *= 10;
			tmpValue /= 10;
		}

		while (divider > 0) {
			sx += drawCharacter(numberMap[(value / divider) % 10], sx, sy, scale);
			divider /= 10;
		}
	}

	public void endDrawing(GL10 gl) {
		endDrawing(gl, false);
	}

	public void endDrawing(GL10 gl, boolean customGlConfig) {
		renderer.bindTextureBlur(gl, textureLoader.textures[TextureLoader.TEXTURE_LABELS]);
		renderer.flush(gl);

		if (!customGlConfig) {
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glPopMatrix();

			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glPopMatrix();
		}
	}

	public float getScaledWidth(String str, float desiredHeight) {
		float scale = desiredHeight / (float)textHeight;
		float width = (float)getWidth(str);

		return width * scale;
	}

	protected int getWidth(String str) {
		int result = 0;

		for (int i = 0, len = str.length(); i < len; i++) {
			char ch = str.charAt(i);

			if (ch == ' ') {
				result += spaceWidth;
			} else {
				Rect rect = charMap.get(ch);
				result += (rect == null ? spaceWidth : rect.width());
			}
		}

		return result;
	}

	public float getScaledWidth(int value, float desiredHeight) {
		float scale = desiredHeight / (float)textHeight;
		float width = (float)getWidth(value);

		return width * scale;
	}

	protected int getWidth(int value) {
		if (value == 0) {
			return numberMap[0].width();
		}

		int result = 0;

		if (value < 0) {
			result += numberMap[10].width();
			value = -value;
		}

		while (value > 0) {
			result += numberMap[value % 10].width();
			value /= 10;
		}

		return result;
	}
}
