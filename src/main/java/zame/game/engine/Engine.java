package zame.game.engine;

import android.util.FloatMath;
import java.io.File;
import java.util.Random;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;
import zame.game.MainActivity;
import zame.game.MyApplication;
import zame.game.managers.SoundManager;
import zame.game.managers.Tracker;
import zame.game.store.Profile;
import zame.game.store.Store;

// According to qualcomm docs, you need to glclear after every glbindframebuffer,
// this is a problem related to tiled architecture, if you are switching framebuffers,
// data need to get copied from fastmem to normal memory to save current framebuffer
// and from slowmem to fast mem to get contents of newly binded frame, in case you are
// clearing just after glbind no data is copied from slowmem to fastmem and you are saving time,
// but you need to redesign your render pipeline often, so it will avoid reading data back
// and forth between slow and fast memory, so try to do glclear after each bind

public class Engine {
	public static final int FRAMES_PER_SECOND = 40;
	public static final int FRAMES_PER_SECOND_D10 = FRAMES_PER_SECOND / 10; // must be >= 1
	public static final int UPDATE_INTERVAL = 1000 / FRAMES_PER_SECOND;
	public static final int FPS_AVG_LEN = 2;

	public static final int VIEW_TYPE_END_LEVEL = 1;
	public static final int VIEW_TYPE_SELECT_EPISODE = 2;
	public static final int VIEW_TYPE_GAME_MENU = 3;
	public static final int VIEW_TYPE_EOD_BLOCKER = 4;
	public static final int VIEW_TYPE_UPGRADE = 5;

	protected MainActivity activity = null;
	protected long startTime;
	protected long lastTime = 0;
	protected boolean isPaused = false;
	protected int createdTexturesCount = 0;
	protected int totalTexturesCount = 0;
	protected int frames = 0;
	protected long prevRenderTime = 0;
	protected int[] fpsList = new int[FPS_AVG_LEN];
	protected int currFpsPtr = 0;
	protected boolean callResumeAfterSurfaceCreated;

	public boolean inWallpaperMode;
	public boolean renderBlackScreen = false;
	public boolean gameViewActive = true;
	public long elapsedTime = 0;
	public int screenWidth = 1;
	public int screenHeight = 1;
	public int width = 1;
	public int height = 1;
	public float xOffset = 0.0f;
	public boolean xOffsetEnabled = false;
	public boolean renderToTexture = false;
	public boolean fboSupported = false;
	public int[] framebuffers = new int[1];
	public int[] depthbuffers = new int[1];
	public boolean fboComplete = false;
	public float ratio = 1.0f;
	public Random random = new Random();
	public float heroAr; // angle in radians
	public float heroCs; // cos of angle
	public float heroSn; // sin of angle
	public boolean interracted = false;
	public boolean showFps = false;
	public String instantName = "";
	public String autosaveName = "";
	public float healthHitMonsterMult;
	public float healthHitHeroMult;

	public Profile profile;
	public Config config = new Config();
	public Game game = new Game();
	public State state = new State();
	public Labels labels = new Labels();
	public Overlay overlay = new Overlay();
	public TextureLoader textureLoader = new TextureLoader();
	public Level level = new Level();
	public LevelRenderer levelRenderer = new LevelRenderer();
	public Weapons weapons = new Weapons();
	public Renderer renderer = new Renderer();
	public Stats stats = new Stats();
	public EndLevel endLevel = new EndLevel();
	public GameOver gameOver = new GameOver();

	public HeroController heroController;
	public SoundManager soundManager;
	public Tracker tracker;

	public Engine(MainActivity activity) {
		this.activity = activity;
		inWallpaperMode = (activity == null);

		instantName = (inWallpaperMode ? "winstant" : "instant");
		autosaveName = (inWallpaperMode ? "" : "autosave");

		profile = MyApplication.self.profile;
		soundManager = SoundManager.getInstance(inWallpaperMode);
		tracker = Tracker.getInstance(inWallpaperMode);
		heroController = HeroController.newInstance(inWallpaperMode);

		config.setEngine(this);
		game.setEngine(this);
		state.setEngine(this);
		labels.setEngine(this);
		overlay.setEngine(this);
		textureLoader.setEngine(this);
		level.setEngine(this);
		levelRenderer.setEngine(this);
		weapons.setEngine(this);
		renderer.setEngine(this);
		stats.setEngine(this);
		heroController.setEngine(this);
		endLevel.setEngine(this);
		gameOver.setEngine(this);
	}

	public void init() {
		int difficulty = profile.products[Store.DIFFICULTY].value;

		if (difficulty == Store.DIFFICULTY_NEWBIE) {
			healthHitMonsterMult = GameParams.HEALTH_HIT_MONSTER_MULT_NEWBIE;
			healthHitHeroMult = GameParams.HEALTH_HIT_HERO_MULT_NEWBIE;
		} else if (difficulty == Store.DIFFICULTY_EASY) {
			healthHitMonsterMult = GameParams.HEALTH_HIT_MONSTER_MULT_EASY;
			healthHitHeroMult = GameParams.HEALTH_HIT_HERO_MULT_EASY;
		} else if (difficulty == Store.DIFFICULTY_HARD) {
			healthHitMonsterMult = GameParams.HEALTH_HIT_MONSTER_MULT_HARD;
			healthHitHeroMult = GameParams.HEALTH_HIT_HERO_MULT_HARD;
		} else if (difficulty == Store.DIFFICULTY_ULTIMATE) {
			healthHitMonsterMult = GameParams.HEALTH_HIT_MONSTER_MULT_ULTIMATE;
			healthHitHeroMult = GameParams.HEALTH_HIT_HERO_MULT_ULTIMATE;
		} else {
			healthHitMonsterMult = 1.0f;
			healthHitHeroMult = 1.0f;
		}

		config.reload();
		renderer.init();
		game.init();
		heroController.reload();

		interracted = false;
		gameViewActive = true;
		renderBlackScreen = false;
		callResumeAfterSurfaceCreated = true;
		startTime = System.currentTimeMillis();
	}

	public void updateAfterLevelLoadedOrCreated() {
		level.updateMaps();
		levelRenderer.updateAfterLoadOrCreate();
		heroController.updateAfterLoadOrCreate();
		weapons.updateWeapon();
	}

	public void createAutosave() {
		if (autosaveName.length() != 0) {
			state.save(autosaveName);
		}
	}

	public String getSavePathBySaveName(String name) {
		if (name == null || name.length() == 0) {
			return "";
		} else if (name.equals(instantName) || name.equals(autosaveName)) {
			return MyApplication.self.INTERNAL_ROOT + name + ".save";
		} else {
			return MyApplication.self.SAVES_ROOT + name + ".save";
		}
	}

	public boolean hasInstantSave() {
		return (new File(getSavePathBySaveName(instantName))).exists();
	}

	public void deleteInstantSave() {
		File instant = new File(getSavePathBySaveName(instantName));

		if (instant.exists()) {
			instant.delete();
		}

		// also delete wallpaper instant save

		instant = new File(MyApplication.self.INTERNAL_ROOT + "winstant.save");

		if (instant.exists()) {
			instant.delete();
		}
	}

	public void changeView(int viewType) {
		switch (viewType) {
			case VIEW_TYPE_GAME_MENU:
				if (activity != null) {
					activity.gameFragment.showGameMenu();
				}
				break;

			case VIEW_TYPE_SELECT_EPISODE:
				if (activity != null) {
					gameViewActive = false;
					renderBlackScreen = true;
					activity.showFragment(activity.selectEpisodeFragment);
				} else {
					createdTexturesCount = 0;
				}
				break;

			case VIEW_TYPE_EOD_BLOCKER:
				if (activity != null) {
					gameViewActive = false;
					renderBlackScreen = true;
					activity.showFragment(activity.eodBlockerFragment);
				}
				break;

			case VIEW_TYPE_UPGRADE:
				if (activity != null) {
					gameViewActive = false;
					renderBlackScreen = true;
					activity.storeFragment.storeCategory = Store.CATEGORY_UPGRADE;
					activity.showFragment(activity.storeFragment);
				}
				break;
		}
	}

	public void heroAngleUpdated() {
		state.heroA = (360.0f + (state.heroA % 360.0f)) % 360.0f;

		heroAr = state.heroA * GameMath.G2RAD_F;
		heroCs = FloatMath.cos(heroAr);
		heroSn = FloatMath.sin(heroAr);
	}

	public int getRealHits(int maxHits, float dist) {
		float div = Math.max(1.0f, dist * 0.35f);
		int minHits = Math.max(1, (int)((float)maxHits / div));

		return (random.nextInt(maxHits - minHits + 1) + minHits);
	}

	// modified Level_CheckLine from wolf3d for iphone by Carmack
	public boolean traceLine(float x1, float y1, float x2, float y2, int mask) {
		int cx1 = (int)x1;
		int cy1 = (int)y1;
		int cx2 = (int)x2;
		int cy2 = (int)y2;
		int maxX = state.levelWidth - 1;
		int maxY = state.levelHeight - 1;

		// level has one-cell border
		if (cx1 <= 0 || cx1 >= maxX || cy1 <= 0 || cy1 >= maxY ||
			cx2 <= 0 || cx2 >= maxX || cy2 <= 0 || cy2 >= maxY
		) {
			return false;
		}

		int[][] localPassableMap = state.passableMap;

		if (cx1 != cx2) {
			int stepX;
			float partial;

			if (cx2 > cx1) {
				partial = 1.0f - (x1 - (float)((int)x1));
				stepX = 1;
			} else {
				partial = x1 - (float)((int)x1);
				stepX = -1;
			}

			float dx = ((x2 >= x1) ? (x2 - x1) : (x1 - x2));
			float stepY = (y2 - y1) / dx;
			float y = y1 + (stepY * partial);

			cx1 += stepX;
			cx2 += stepX;

			do {
				if ((localPassableMap[(int)y][cx1] & mask) != 0) {
					return false;
				}

				y += stepY;
				cx1 += stepX;
			} while (cx1 != cx2);
		}

		if (cy1 != cy2) {
			int stepY;
			float partial;

			if (cy2 > cy1) {
				partial = 1.0f - (y1 - (float)((int)y1));
				stepY = 1;
			} else {
				partial = y1 - (float)((int)y1);
				stepY = -1;
			}

			float dy = ((y2 >= y1) ? (y2 - y1) : (y1 - y2));
			float stepX = (x2 - x1) / dy;
			float x = x1 + (stepX * partial);

			cy1 += stepY;
			cy2 += stepY;

			do {
				if ((localPassableMap[cy1][(int)x] & mask) != 0) {
					return false;
				}

				x += stepX;
				cy1 += stepY;
			} while (cy1 != cy2);
		}

		return true;
	}

	public void onSurfaceCreated(GL10 gl) {
		fboSupported = ((" " + gl.glGetString(GL10.GL_EXTENSIONS) + " ").indexOf(" GL_OES_framebuffer_object ") >= 0);
		gl.glClearColor(0.0f, 0f, 0f, 1.0f);

		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glCullFace(GL10.GL_BACK);
		gl.glDisable(GL10.GL_DITHER);
		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
		gl.glDepthFunc(GL10.GL_LESS); // GL10.GL_LEQUAL

		createdTexturesCount = 0;
		totalTexturesCount = TextureLoader.TEXTURES_TO_LOAD.length + 1;
		textureLoader.onSurfaceCreated(gl);

		if (callResumeAfterSurfaceCreated) {
			callResumeAfterSurfaceCreated = false;
			onResume();
		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.screenWidth = (width < 1 ? 1 : width); // just for case
		this.screenHeight = (height < 1 ? 1 : height); // just for case
		renderToTexture = (inWallpaperMode && width < height);
		fboComplete = false;

		if (renderToTexture && fboSupported) {
			GL11ExtensionPack gl11ep = (GL11ExtensionPack)gl;
			gl11ep.glGenFramebuffersOES(1, framebuffers, 0);
			gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, framebuffers[0]);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl11ep.glGenRenderbuffersOES(1, depthbuffers, 0);
			gl11ep.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, depthbuffers[0]);

			gl11ep.glRenderbufferStorageOES(
				GL11ExtensionPack.GL_RENDERBUFFER_OES,
				GL11ExtensionPack.GL_DEPTH_COMPONENT16,
				TextureLoader.RENDER_TO_FBO_SIZE,
				TextureLoader.RENDER_TO_FBO_SIZE
			);

			gl11ep.glFramebufferRenderbufferOES(
				GL11ExtensionPack.GL_FRAMEBUFFER_OES,
				GL11ExtensionPack.GL_DEPTH_ATTACHMENT_OES,
				GL11ExtensionPack.GL_RENDERBUFFER_OES,
				depthbuffers[0]
			);

			gl11ep.glFramebufferTexture2DOES(
				GL11ExtensionPack.GL_FRAMEBUFFER_OES,
				GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES,
				GL10.GL_TEXTURE_2D,
				textureLoader.textures[TextureLoader.TEXTURE_RENDER_TO_FBO],
				0
			);

			int status = gl11ep.glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES);

			if (status == GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {
				fboComplete = true;
			}

			gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		}

		if (renderToTexture) {
			this.width = (fboComplete ? TextureLoader.RENDER_TO_FBO_SIZE : TextureLoader.RENDER_TO_SIZE);
			this.height = (fboComplete ? TextureLoader.RENDER_TO_FBO_SIZE : TextureLoader.RENDER_TO_SIZE);
		} else {
			this.width = screenWidth;
			this.height = screenHeight;
		}

		gl.glViewport(0, 0, this.width, this.height);
		ratio = (float)this.width / (float)this.height;

		levelRenderer.surfaceSizeChanged(gl);
		heroController.surfaceSizeChanged();
		stats.surfaceSizeChanged();
	}

	public void onPause() {
		if (!isPaused) {
			isPaused = true;
			state.tempElapsedTime = elapsedTime;
			state.tempLastTime = lastTime;
			state.save(instantName);
		}
	}

	public synchronized void onResume() {
		if (callResumeAfterSurfaceCreated) {
			// wait for created surface
			return;
		}

		if (isPaused) {
			isPaused = false;
			elapsedTime = state.tempElapsedTime;
			lastTime = state.tempLastTime;
			startTime = System.currentTimeMillis() - elapsedTime;
		}

		game.resume();
	}

	public boolean texturesLoaded() {
		return (createdTexturesCount >= totalTexturesCount);
	}

	public void onDrawFrame(GL10 gl) {
		if (isPaused) {
			render(gl);
			return;
		}

		heroController.onDrawFrame();
		elapsedTime = System.currentTimeMillis() - startTime;

		if (lastTime > elapsedTime) {
			lastTime = elapsedTime;
		}

		if (elapsedTime - lastTime > UPDATE_INTERVAL) {
			long count = (elapsedTime - lastTime) / UPDATE_INTERVAL;

			if (count > 10) {
				count = 10;
				lastTime = elapsedTime;
			} else {
				lastTime += UPDATE_INTERVAL * count;
			}

			if (texturesLoaded()) {
				while (count > 0 && !renderBlackScreen) {
					game.update();
					count--;
				}
			}
		}

		render(gl);
	}

	protected void drawPreloader(GL10 gl) {
		renderer.initOrtho(gl, true, false, -ratio, ratio, 1.0f, -1.0f, 0.0f, 1.0f);
		renderer.init();

		gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_CULL_FACE);

		renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
		renderer.setQuadOrthoCoords(-0.5f, -0.5f, 0.5f, 0.5f);
		renderer.setQuadTexCoords(0, 0, 1 << 16, 1 << 16);
		renderer.drawQuad();

		renderer.bindTextureBlur(gl, textureLoader.textures[TextureLoader.TEXTURE_LOADING]);
		renderer.flush(gl);

		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	protected void renderDimLayer(GL10 gl) {
		renderer.initOrtho(gl, true, false, 0.0f, 1.0f, 0.0f, 1.0f, 0f, 1.0f);
		renderer.init();

		renderer.setQuadRGBA(0.0f, 0.0f, 0.0f, config.wpDim);
		renderer.setQuadOrthoCoords(0.0f, 0.0f, 1.0f, 1.0f);
		renderer.drawQuad();

		gl.glShadeModel(GL10.GL_FLAT);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		renderer.flush(gl, false);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	protected void render(GL10 gl) {
		GL11ExtensionPack gl11ep = null;

		if (renderBlackScreen) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			return;
		}

		if (renderToTexture && fboComplete) {
			gl11ep = (GL11ExtensionPack)gl;
			gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, framebuffers[0]);
		}

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		if (createdTexturesCount < totalTexturesCount) {
			drawPreloader(gl);

			if (createdTexturesCount == 0) {
				labels.createLabels(gl);
				createdTexturesCount++;
			} else if (MyApplication.self.cachedTexturesReady) {
				textureLoader.loadTexture(gl, createdTexturesCount - 1);
				createdTexturesCount++;
			}

			if (createdTexturesCount >= totalTexturesCount) {
				System.gc();
			}
		} else {
			game.render(gl);
		}

		if (inWallpaperMode || isPaused) {
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
			}
		}

		if (inWallpaperMode) {
			renderDimLayer(gl);
		}

		// http://stackoverflow.com/questions/10729352/framebuffer-fbo-render-to-texture-is-very-slow-using-opengl-es-2-0-on-android
		// http://www.java2s.com/Code/Android/File/DemonstratetheFrameBufferObjectOpenGLESextension.htm
		// http://www.gamedev.net/topic/590324-fbo-set-up-on-android/

		if (renderToTexture) {
			if (gl11ep != null) {
				gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
				gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
				renderer.bindTextureBlur(gl, textureLoader.textures[TextureLoader.TEXTURE_RENDER_TO_FBO]);
			} else {
				renderer.bindTextureBlur(gl, textureLoader.textures[TextureLoader.TEXTURE_RENDER_TO]);
				gl.glCopyTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, 0, 0, width, height, 0);
			}

			gl.glViewport(0, 0, screenWidth, screenHeight);
			gl.glShadeModel(GL10.GL_FLAT);
			gl.glDisable(GL10.GL_DEPTH_TEST);
			gl.glDisable(GL10.GL_BLEND);

			float widthD2 = (float)screenWidth * 0.5f;
			float heightD2 = (float)screenHeight * 0.5f;

			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			gl.glOrthof(- widthD2, widthD2, - heightD2, heightD2, 0.0f, 1.0f);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();

			if (xOffsetEnabled) {
				gl.glTranslatef((float)(screenWidth - screenHeight) * (xOffset - 0.5f), 0.0f, 0.0f);
			}

			renderer.init();
			renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
			renderer.setQuadTexCoords(0, 0, 1 << 16, 1 << 16);
			renderer.setQuadOrthoCoords(- heightD2, - heightD2, heightD2, heightD2);
			renderer.drawQuad();
			renderer.flush(gl);

			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glPopMatrix();
			gl.glViewport(0, 0, height, width);
		}
	}

	protected int getAvgFps() {
		frames++;

		long time = System.currentTimeMillis();
		long diff = time - prevRenderTime;

		if (diff > 1000) {
			int seconds = (int)(diff / 1000L);
			prevRenderTime += (long)seconds * 1000L;

			fpsList[currFpsPtr] = frames / seconds;
			currFpsPtr = (currFpsPtr + 1) % FPS_AVG_LEN;

			frames = 0;
		}

		int sum = 0;

		for (int v : fpsList) {
			sum += v;
		}

		return (sum / FPS_AVG_LEN);
	}

	public void drawFps(GL10 gl) {
		int fps = getAvgFps();
		renderer.setQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);

		labels.beginDrawing(gl);
		labels.draw(gl, -ratio + 0.01f, -1.0f + 0.01f, ratio, 1.0f, String.format(labels.map[Labels.LABEL_FPS], fps), 0.125f, Labels.ALIGN_BL);
		labels.endDrawing(gl);
	}
}
