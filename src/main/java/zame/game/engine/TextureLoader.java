package zame.game.engine;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.os.Build;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.MyApplication;
import zame.game.R;
import zame.game.providers.CachedTexturesProvider;

public class TextureLoader implements EngineObject {
	public static final int TEXTURE_MAIN = 0;
	public static final int TEXTURE_HAND = 1;		// 1, 2, 3, 4
	public static final int TEXTURE_PIST = 5;		// 5, 6, 7, 8

	public static final int TEXTURE_SHTG = 9;		// 9, 10, 11, 12, 13
	public static final int TEXTURE_CHGN = 14;		// 14, 15, 16, 17
	public static final int TEXTURE_DBLSHTG = 18;	// 18, 19, 20, 21
	public static final int TEXTURE_DBLCHGN = 22;	// 22, 23, 24, 25
	public static final int TEXTURE_SAW = 26;		// 26, 27, 28, 29
	public static final int TEXTURE_MONSTERS = 30;	// 30, 31
	public static final int TEXTURE_RLAUNCHER = 32;	// 32, 33, 34, 35
	public static final int TEXTURE_DBLPIST = 36;	// 36, 37, 38
	public static final int TEXTURE_PDBLSHTG = 39;	// 39, 40, 41, 42
	public static final int TEXTURE_SKY = 43;
	public static final int TEXTURE_LOADING = 44;
	public static final int TEXTURE_LABELS = 45;
	public static final int TEXTURE_RENDER_TO = 46;
	public static final int TEXTURE_RENDER_TO_FBO = 47;
	public static final int TEXTURE_LAST = 48;

	public static final int RENDER_TO_SIZE = 256;
	public static final int RENDER_TO_FBO_SIZE = 512;

	public static final int ROW_COMMON = 0;
	public static final int ROW_TILES = 5;

	public static final int BASE_ICONS = ROW_COMMON * 15;
	public static final int BASE_OBJECTS = BASE_ICONS + 10;
	public static final int BASE_BULLETS = BASE_OBJECTS + 21;
	public static final int BASE_EXPLOSIONS = BASE_BULLETS + 4;
	public static final int BASE_ARROWS = BASE_EXPLOSIONS + 3;
	public static final int BASE_BACKS = (ROW_COMMON + 3) * 15;

	public static final int BASE_WALLS = ROW_TILES * 15; // BASE_WALLS must be greater than 0
	public static final int BASE_TRANSP_WALLS = BASE_WALLS + 43;
	public static final int BASE_TRANSP_PASSABLE = BASE_TRANSP_WALLS + 4;
	public static final int BASE_TRANSP_WINDOWS = BASE_TRANSP_PASSABLE + 6;
	public static final int BASE_DOORS_F = BASE_TRANSP_WINDOWS + 8;
	public static final int BASE_DOORS_S = BASE_DOORS_F + 8;
	public static final int BASE_DECOR_ITEM = BASE_DOORS_S + 8;
	public static final int BASE_DECOR_LAMP = BASE_DECOR_ITEM + 10;
	public static final int BASE_FLOOR = BASE_DECOR_LAMP + 1;
	public static final int BASE_CEIL = BASE_FLOOR + 10;

	public static final int PACKED_WALLS = 1 << 16;
	public static final int PACKED_TRANSP_WALLS = 2 << 16;
	public static final int PACKED_TRANSP_PASSABLE = 3 << 16;
	public static final int PACKED_TRANSP_WINDOWS = 4 << 16;
	public static final int PACKED_DOORS_F = 5 << 16;
	public static final int PACKED_DOORS_S = 6 << 16;
	public static final int PACKED_OBJECTS = 7 << 16;
	public static final int PACKED_DECOR_ITEM = 8 << 16;
	public static final int PACKED_DECOR_LAMP = 9 << 16;
	public static final int PACKED_FLOOR = 10 << 16;
	public static final int PACKED_CEIL = 11 << 16;
	public static final int PACKED_BULLETS = 12 << 16;
	public static final int PACKED_ARROWS = 13 << 16;

	public static final int PACKED_TEXMAP_MONSTERS = 1 << 16;

	public static final int COUNT_MONSTER = 0x10;	// block = [up, rt, dn, lt], monster = block[walk_a, walk_b, hit], die[3], shoot

	public static final int ICON_JOY = BASE_ICONS + 0;
	public static final int ICON_MENU = BASE_ICONS + 1;
	public static final int ICON_SHOOT = BASE_ICONS + 2;
	public static final int ICON_MAP = BASE_ICONS + 3;
	public static final int ICON_HEALTH = BASE_ICONS + 4;
	public static final int ICON_ARMOR = BASE_ICONS + 5;
	public static final int ICON_AMMO = BASE_ICONS + 6;
	public static final int ICON_BLUE_KEY = BASE_ICONS + 7;
	public static final int ICON_RED_KEY = BASE_ICONS + 8;
	public static final int ICON_GREEN_KEY = BASE_ICONS + 9;

	public static final int OBJ_ARMOR_GREEN = BASE_OBJECTS-1 + 1;
	public static final int OBJ_ARMOR_RED = BASE_OBJECTS-1 + 2;
	public static final int OBJ_KEY_BLUE = BASE_OBJECTS-1 + 3;
	public static final int OBJ_KEY_RED = BASE_OBJECTS-1 + 4;
	public static final int OBJ_STIM = BASE_OBJECTS-1 + 5;
	public static final int OBJ_MEDI = BASE_OBJECTS-1 + 6;
	public static final int OBJ_CLIP = BASE_OBJECTS-1 + 7;
	public static final int OBJ_AMMO = BASE_OBJECTS-1 + 8;
	public static final int OBJ_SHELL = BASE_OBJECTS-1 + 9;
	public static final int OBJ_SBOX = BASE_OBJECTS-1 + 10;
	public static final int OBJ_BPACK = BASE_OBJECTS-1 + 11;
	public static final int OBJ_SHOTGUN = BASE_OBJECTS-1 + 12;
	public static final int OBJ_KEY_GREEN = BASE_OBJECTS-1 + 13;
	public static final int OBJ_CHAINGUN = BASE_OBJECTS-1 + 14;
	public static final int OBJ_DBLSHOTGUN = BASE_OBJECTS-1 + 15;
	public static final int OBJ_CHAINSAW = BASE_OBJECTS-1 + 16;
	public static final int OBJ_DBLCHAINGUN = BASE_OBJECTS-1 + 17;
	public static final int OBJ_OPENMAP = BASE_OBJECTS-1 + 18;
	public static final int OBJ_ROCKET = BASE_OBJECTS-1 + 19;
	public static final int OBJ_RBOX = BASE_OBJECTS-1 + 20;
	public static final int OBJ_RLAUNCHER = BASE_OBJECTS-1 + 21;

	public static final int ARROW_UP = BASE_ARROWS-1 + 1;
	public static final int ARROW_RT = BASE_ARROWS-1 + 2;
	public static final int ARROW_DN = BASE_ARROWS-1 + 3;
	public static final int ARROW_LT = BASE_ARROWS-1 + 4;

	public static final int[] WALL_LIGHTS = {
		BASE_WALLS-1 + 7,
		BASE_WALLS-1 + 9,
		BASE_WALLS-1 + 10,
		BASE_WALLS-1 + 12,
		BASE_WALLS-1 + 20,
		BASE_WALLS-1 + 21,
		BASE_WALLS-1 + 22,
		BASE_WALLS-1 + 23,
		BASE_WALLS-1 + 24,
		BASE_WALLS-1 + 25,
		BASE_WALLS-1 + 26,
		BASE_WALLS-1 + 27,
		BASE_WALLS-1 + 28,
		BASE_WALLS-1 + 29,
		BASE_WALLS-1 + 30,
		BASE_WALLS-1 + 31,
		BASE_WALLS-1 + 32,
		BASE_WALLS-1 + 33,
		BASE_WALLS-1 + 34,
		BASE_WALLS-1 + 35,
	};

	public static final int[] DITEM_LIGHTS = {
		BASE_DECOR_ITEM-1 + 1,
		BASE_DECOR_ITEM-1 + 2,
		BASE_DECOR_ITEM-1 + 8,
		BASE_DECOR_ITEM-1 + 10,
	};

	public static final int[] CEIL_LIGHTS = {
		BASE_CEIL-1 + 2,
		BASE_CEIL-1 + 4,
		BASE_CEIL-1 + 6,
	};

	public static class TextureToLoad {
		public static final int TYPE_RESOURCE = 0;
		public static final int TYPE_MAIN = 1;
		public static final int TYPE_MONSTERS_1 = 2;
		public static final int TYPE_MONSTERS_2 = 3;

		public int tex;
		public int pixelsResId;
		public int alphaResId;
		public int type;

		public TextureToLoad(int tex, int pixelsResId, int alphaResId) {
			this.tex = tex;
			this.pixelsResId = pixelsResId;
			this.alphaResId = alphaResId;
			this.type = TYPE_RESOURCE;
		}

		public TextureToLoad(int tex, int pixelsResId, int alphaResId, int type) {
			this.tex = tex;
			this.pixelsResId = pixelsResId;
			this.alphaResId = alphaResId;
			this.type = type;
		}
	}

	public static final TextureToLoad[] TEXTURES_TO_LOAD = new TextureToLoad[] {
		new TextureToLoad(TEXTURE_MONSTERS + 0, 0, 0, TextureToLoad.TYPE_MONSTERS_1),
		new TextureToLoad(TEXTURE_MONSTERS + 1, 0, 0, TextureToLoad.TYPE_MONSTERS_2),
		new TextureToLoad(TEXTURE_MAIN, 0, 0, TextureToLoad.TYPE_MAIN),

		new TextureToLoad(TEXTURE_HAND + 0, R.drawable.hit_hand_1_p, R.drawable.hit_hand_1_a),
		new TextureToLoad(TEXTURE_HAND + 1, R.drawable.hit_hand_2_p, R.drawable.hit_hand_2_a),
		new TextureToLoad(TEXTURE_HAND + 2, R.drawable.hit_hand_3_p, R.drawable.hit_hand_3_a),
		new TextureToLoad(TEXTURE_HAND + 3, R.drawable.hit_hand_4_p, R.drawable.hit_hand_4_a),

		new TextureToLoad(TEXTURE_PIST + 0, R.drawable.hit_pist_1_p, R.drawable.hit_pist_1_a),
		new TextureToLoad(TEXTURE_PIST + 1, R.drawable.hit_pist_2_p, R.drawable.hit_pist_2_a),
		new TextureToLoad(TEXTURE_PIST + 2, R.drawable.hit_pist_3_p, R.drawable.hit_pist_3_a),
		new TextureToLoad(TEXTURE_PIST + 3, R.drawable.hit_pist_4_p, R.drawable.hit_pist_4_a),

		new TextureToLoad(TEXTURE_SHTG + 0, R.drawable.hit_shtg_1_p, R.drawable.hit_shtg_1_a),
		new TextureToLoad(TEXTURE_SHTG + 1, R.drawable.hit_shtg_2_p, R.drawable.hit_shtg_2_a),
		new TextureToLoad(TEXTURE_SHTG + 2, R.drawable.hit_shtg_3_p, R.drawable.hit_shtg_3_a),
		new TextureToLoad(TEXTURE_SHTG + 3, R.drawable.hit_shtg_4_p, R.drawable.hit_shtg_4_a),
		new TextureToLoad(TEXTURE_SHTG + 4, R.drawable.hit_shtg_5_p, R.drawable.hit_shtg_5_a),

		new TextureToLoad(TEXTURE_CHGN + 0, R.drawable.hit_chgn_1_p, R.drawable.hit_chgn_1_a),
		new TextureToLoad(TEXTURE_CHGN + 1, R.drawable.hit_chgn_2_p, R.drawable.hit_chgn_2_a),
		new TextureToLoad(TEXTURE_CHGN + 2, R.drawable.hit_chgn_3_p, R.drawable.hit_chgn_3_a),
		new TextureToLoad(TEXTURE_CHGN + 3, R.drawable.hit_chgn_4_p, R.drawable.hit_chgn_4_a),

		new TextureToLoad(TEXTURE_DBLSHTG + 0, R.drawable.hit_dblshtg_1_p, R.drawable.hit_dblshtg_1_a),
		new TextureToLoad(TEXTURE_DBLSHTG + 1, R.drawable.hit_dblshtg_2_p, R.drawable.hit_dblshtg_2_a),
		new TextureToLoad(TEXTURE_DBLSHTG + 2, R.drawable.hit_dblshtg_3_p, R.drawable.hit_dblshtg_3_a),
		new TextureToLoad(TEXTURE_DBLSHTG + 3, R.drawable.hit_dblshtg_4_p, R.drawable.hit_dblshtg_4_a),

		new TextureToLoad(TEXTURE_DBLCHGN + 0, R.drawable.hit_dblchgn_1_p, R.drawable.hit_dblchgn_1_a),
		new TextureToLoad(TEXTURE_DBLCHGN + 1, R.drawable.hit_dblchgn_2_p, R.drawable.hit_dblchgn_2_a),
		new TextureToLoad(TEXTURE_DBLCHGN + 2, R.drawable.hit_dblchgn_3_p, R.drawable.hit_dblchgn_3_a),
		new TextureToLoad(TEXTURE_DBLCHGN + 3, R.drawable.hit_dblchgn_4_p, R.drawable.hit_dblchgn_4_a),

		new TextureToLoad(TEXTURE_SAW + 0, R.drawable.hit_saw_1_p, R.drawable.hit_saw_1_a),
		new TextureToLoad(TEXTURE_SAW + 1, R.drawable.hit_saw_2_p, R.drawable.hit_saw_2_a),
		new TextureToLoad(TEXTURE_SAW + 2, R.drawable.hit_saw_3_p, R.drawable.hit_saw_3_a),
		new TextureToLoad(TEXTURE_SAW + 3, R.drawable.hit_saw_4_p, R.drawable.hit_saw_4_a),

		new TextureToLoad(TEXTURE_RLAUNCHER + 0, R.drawable.hit_rocket_1_p, R.drawable.hit_rocket_1_a),
		new TextureToLoad(TEXTURE_RLAUNCHER + 1, R.drawable.hit_rocket_2_p, R.drawable.hit_rocket_2_a),
		new TextureToLoad(TEXTURE_RLAUNCHER + 2, R.drawable.hit_rocket_3_p, R.drawable.hit_rocket_3_a),
		new TextureToLoad(TEXTURE_RLAUNCHER + 3, R.drawable.hit_rocket_4_p, R.drawable.hit_rocket_4_a),

		new TextureToLoad(TEXTURE_DBLPIST + 0, R.drawable.hit_dblpist_1_p, R.drawable.hit_dblpist_1_a),
		new TextureToLoad(TEXTURE_DBLPIST + 1, R.drawable.hit_dblpist_2_p, R.drawable.hit_dblpist_2_a),
		new TextureToLoad(TEXTURE_DBLPIST + 2, R.drawable.hit_dblpist_3_p, R.drawable.hit_dblpist_3_a),

		new TextureToLoad(TEXTURE_PDBLSHTG + 0, R.drawable.hit_pdblshtg_1_p, R.drawable.hit_pdblshtg_1_a),
		new TextureToLoad(TEXTURE_PDBLSHTG + 1, R.drawable.hit_pdblshtg_2_p, R.drawable.hit_pdblshtg_2_a),
		new TextureToLoad(TEXTURE_PDBLSHTG + 2, R.drawable.hit_pdblshtg_3_p, R.drawable.hit_pdblshtg_3_a),
		new TextureToLoad(TEXTURE_PDBLSHTG + 3, R.drawable.hit_pdblshtg_4_p, R.drawable.hit_pdblshtg_4_a),

		new TextureToLoad(TEXTURE_SKY, R.drawable.sky_1, 0),
	};

	protected Engine engine;
	protected State state;
	protected Resources resources;
	protected AssetManager assetManager;
	protected boolean texturesInitialized = false;
	protected BitmapFactory.Options bitmapOptions;
	protected LevelConfig levelConf;

	public int[] textures = new int[TEXTURE_LAST];

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
		this.resources = MyApplication.self.getResources();
		this.assetManager = MyApplication.self.getAssets();
	}

	protected void loadAndBindTexture(GL10 gl, int tex, int set) {
		Bitmap img = BitmapFactory.decodeFile(CachedTexturesProvider.getCachePath(tex, set));

		if (img == null) {
			final String errorMessage = "Can't load cached bitmap";
			Common.showToast(errorMessage);
			throw new RuntimeException(errorMessage);
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[tex]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);

		img.recycle();
		img = null;
	}

	public void onSurfaceCreated(GL10 gl) {
		if (texturesInitialized) {
			gl.glDeleteTextures(TEXTURE_LAST, textures, 0);
		}

		texturesInitialized = true;
		gl.glGenTextures(TEXTURE_LAST, textures, 0);

		// так как все битмапы загружаются в память GPU, и после этого освобождаются,
		// то inPurgeable не несёт особой пользы, а вред может и принести - на некоторых девайсах
		// вне зависимости от значения inInputShareable система может всё равно сделать копию исходных данных
		bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inDither = false;
		bitmapOptions.inScaled = false;
		bitmapOptions.inPurgeable = false;
		bitmapOptions.inInputShareable = false;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// пробуем уменьшить расход памяти, ибо всё равно потом все битмапы пургаются и за-null-иваются
			bitmapOptions.inMutable = true;
		}

		Bitmap img = CachedTexturesProvider.decodeResource(resources, R.drawable.tex_loading, bitmapOptions);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[TEXTURE_LOADING]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
		img.recycle();
		img = null;

		img = Common.createBitmap(RENDER_TO_SIZE, RENDER_TO_SIZE, "Can't alloc bitmap for render buffer");
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[TEXTURE_RENDER_TO]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, img, 0);
		img.recycle();
		img = null;

		if (engine.fboSupported) {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[TEXTURE_RENDER_TO_FBO]);

			gl.glTexImage2D(
				GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA,
				RENDER_TO_FBO_SIZE, RENDER_TO_FBO_SIZE,
				0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, null
			);
		}

		System.gc();
	}

	public boolean loadTexture(GL10 gl, int createdTexturesCount) {
		if (createdTexturesCount >= TEXTURES_TO_LOAD.length) {
			return false;
		}

		if (createdTexturesCount == 0) {
			levelConf = LevelConfig.read(assetManager, state.levelName);
		}

		TextureToLoad texToLoad = TEXTURES_TO_LOAD[createdTexturesCount];

		if (texToLoad.type == TextureToLoad.TYPE_MAIN) {
			loadAndBindTexture(
				gl,
				texToLoad.tex,
				CachedTexturesProvider.normalizeSetNum(CachedTexturesProvider.mainTexMap, levelConf.graphicsSet)
			);
		} else {
			loadAndBindTexture(gl, texToLoad.tex, 0);
		}

		return true;
	}

	public static int packTexmap(int texmap) {
		if (texmap == TEXTURE_MONSTERS || texmap == TEXTURE_MONSTERS + 1) {
			return (texmap - TEXTURE_MONSTERS) | PACKED_TEXMAP_MONSTERS;
		} else {
			return texmap;
		}
	}

	public static int unpackTexmap(int texmap) {
		if (texmap <= 0) {
			return texmap;
		}

		int texmapBase = texmap & 0xFFFF;

		switch (texmap & 0xF0000) {
			case PACKED_TEXMAP_MONSTERS: return texmapBase + TEXTURE_MONSTERS;
			default: return texmapBase;
		}
	}

	public static int packTexId(int texId) {
		if (texId >= BASE_CEIL) {
			return (texId - BASE_CEIL) | PACKED_CEIL;
		} else if (texId >= BASE_FLOOR) {
			return (texId - BASE_FLOOR) | PACKED_FLOOR;
		} else if (texId >= BASE_DECOR_LAMP) {
			return (texId - BASE_DECOR_LAMP) | PACKED_DECOR_LAMP;
		} else if (texId >= BASE_DECOR_ITEM) {
			return (texId - BASE_DECOR_ITEM) | PACKED_DECOR_ITEM;
		} else if (texId >= BASE_DOORS_S) {
			return (texId - BASE_DOORS_S) | PACKED_DOORS_S;
		} else if (texId >= BASE_DOORS_F) {
			return (texId - BASE_DOORS_F) | PACKED_DOORS_F;
		} else if (texId >= BASE_TRANSP_WINDOWS) {
			return (texId - BASE_TRANSP_WINDOWS) | PACKED_TRANSP_WINDOWS;
		} else if (texId >= BASE_TRANSP_PASSABLE) {
			return (texId - BASE_TRANSP_PASSABLE) | PACKED_TRANSP_PASSABLE;
		} else if (texId >= BASE_TRANSP_WALLS) {
			return (texId - BASE_TRANSP_WALLS) | PACKED_TRANSP_WALLS;
		} else if (texId >= BASE_WALLS) {
			return (texId - BASE_WALLS) | PACKED_WALLS;
		} else if (texId >= BASE_ARROWS) {
			return (texId - BASE_ARROWS) | PACKED_ARROWS;
		} else if (texId >= BASE_EXPLOSIONS) {
			return texId; // do not pack explosions
		} else if (texId >= BASE_BULLETS) {
			return (texId - BASE_BULLETS) | PACKED_BULLETS;
		} else if (texId >= BASE_OBJECTS) {
			return (texId - BASE_OBJECTS) | PACKED_OBJECTS;
		} else {
			return texId;
		}
	}

	public static int unpackTexId(int texId) {
		if (texId <= 0) {
			return texId;
		}

		int texBase = texId & 0xFFFF;

		switch (texId & 0xF0000) {
			case PACKED_WALLS: return texBase + BASE_WALLS;
			case PACKED_TRANSP_WALLS: return texBase + BASE_TRANSP_WALLS;
			case PACKED_TRANSP_PASSABLE: return texBase + BASE_TRANSP_PASSABLE;
			case PACKED_TRANSP_WINDOWS: return texBase + BASE_TRANSP_WINDOWS;
			case PACKED_DOORS_F: return texBase + BASE_DOORS_F;
			case PACKED_DOORS_S: return texBase + BASE_DOORS_S;
			case PACKED_OBJECTS: return texBase + BASE_OBJECTS;
			case PACKED_DECOR_ITEM: return texBase + BASE_DECOR_ITEM;
			case PACKED_DECOR_LAMP: return texBase + BASE_DECOR_LAMP;
			case PACKED_FLOOR: return texBase + BASE_FLOOR;
			case PACKED_CEIL: return texBase + BASE_CEIL;
			case PACKED_BULLETS: return texBase + BASE_BULLETS;
			case PACKED_ARROWS: return texBase + BASE_ARROWS;
			default: return texBase;
		}
	}
}
