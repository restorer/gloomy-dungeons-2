package zame.game.engine.controls;

import android.util.FloatMath;
import android.view.MotionEvent;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.Game;
import zame.game.engine.Labels;
import zame.game.engine.Renderer;
import zame.game.engine.State;
import zame.game.engine.TextureLoader;

public class Controls implements EngineObject {
	public static final int SCHEME_STATIC_MOVE_PAD = 0;
	public static final int SCHEME_FREE_MOVE_PAD = 1;

	public static final int FORWARD = 1;
	public static final int BACKWARD = 2;
	public static final int STRAFE_LEFT = 4;
	public static final int STRAFE_RIGHT = 8;
	public static final int FIRE = 16;
	public static final int NEXT_WEAPON = 32;
	public static final int ROTATE_LEFT = 64;
	public static final int ROTATE_RIGHT = 128;
	public static final int TOGGLE_MAP = 256;
	public static final int STRAFE_MODE = 512;
	public static final int MASK_MAX = 1024;

	public static final int POSITION_LEFT = 1;
	public static final int POSITION_RIGHT = 2;
	public static final int POSITION_TOP = 4;

	public static final int ACTION_FIRE_ONSCREEN = 1;
	public static final int ACTION_FIRE_JOYSTICK = 2;
	public static final int ACTION_FIRE_KEYS = 4;

	protected static final int POINTER_ACTION_DOWN = 1;
	protected static final int POINTER_ACTION_MOVE = 2;
	protected static final int POINTER_ACTION_UP = 3;
	protected static final int MAX_POINTER_ID = 8;

	protected static final float DIAG_SIZE = 0.075f;
	protected static final float DIAG_B_SIZE = 0.1f;
	protected static final float ARROW_F_SIZE = 0.075f;
	protected static final float ARROW_N_SIZE = ARROW_F_SIZE * 0.75f;
	protected static final float ARROW_F_WEIGHT = ARROW_F_SIZE * 0.3f;
	protected static final float LINE_WEIGHT = ARROW_F_SIZE * 0.05f;

	protected Engine engine;
	protected Renderer renderer;
	protected Config config;
	protected State state;
	protected TextureLoader textureLoader;
	protected Game game;
	protected Labels labels;
	protected OnScreenController[] activeControllers = new OnScreenController[MAX_POINTER_ID];
	protected OnScreenButton schemeMenuButton = new OnScreenButton(POSITION_LEFT | POSITION_TOP, OnScreenButton.TYPE_GAME_MENU);
	protected OnScreenPad schemePad = new OnScreenPad(POSITION_LEFT, false);
	protected OnScreenButton schemeToggleMapButton = new OnScreenButton(POSITION_RIGHT | POSITION_TOP, OnScreenButton.TYPE_TOGGLE_MAP);
	protected OnScreenFireAndRotate schemeFireAndRotate = new OnScreenFireAndRotate(POSITION_RIGHT);
	protected OnScreenUpgradeButton schemeUpgradeButton = new OnScreenUpgradeButton(POSITION_LEFT);

	protected OnScreenController[] scheme = new OnScreenController[] {
		schemeMenuButton,
		schemePad,
		schemeToggleMapButton,
		schemeUpgradeButton,
		schemeFireAndRotate // must be last
	};

	public float iconSize;

	@Override
	public void setEngine(Engine engine) {
		this.engine = engine;
		this.renderer = engine.renderer;
		this.config = engine.config;
		this.state = engine.state;
		this.textureLoader = engine.textureLoader;
		this.game = engine.game;
		this.labels = engine.labels;
	}

	public void reload() {
		schemeMenuButton.position = (config.leftHandAim ? POSITION_RIGHT : POSITION_LEFT) | POSITION_TOP;
		schemePad.position = (config.leftHandAim ? POSITION_RIGHT : POSITION_LEFT);
		schemePad.dynamic = (config.controlScheme == SCHEME_FREE_MOVE_PAD);
		schemeToggleMapButton.position = (config.leftHandAim ? POSITION_LEFT : POSITION_RIGHT) | (config.fireButtonAtTop ? 0 : POSITION_TOP);
		schemeFireAndRotate.position = (config.leftHandAim ? POSITION_LEFT : POSITION_RIGHT) | (config.fireButtonAtTop ? POSITION_TOP : 0);
		schemeUpgradeButton.position = (config.leftHandAim ? POSITION_RIGHT : POSITION_LEFT);

		for (int i = 0, len = scheme.length; i < len; i++) {
			scheme[i].setOwner(this, engine);
		}
	}

	public void surfaceSizeChanged() {
		iconSize = (float)Math.min(engine.height, engine.width) / 5.0f * config.controlsScale;

		for (int i = 0, len = scheme.length; i < len; i++) {
			OnScreenController controller = scheme[i];

			controller.surfaceSizeChanged();
			controller.pointerUp();
		}
	}

	protected void processOnePointer(int pointerId, float x, float y, int pointerAction) {
		if (pointerId < 0 || pointerId >= MAX_POINTER_ID) {
			return;
		}

		if (config.rotateScreen) {
			x = (float)engine.width - x;
			y = (float)engine.height - y;
		}

		OnScreenController controller = activeControllers[pointerId];

		switch (pointerAction) {
			case POINTER_ACTION_DOWN:
				if (controller != null) {
					controller.pointerUp();
					activeControllers[pointerId] = null;
					Common.log("Secondary POINTER_ACTION_DOWN for the same pointerId");
				}

				for (int i = 0, len = scheme.length; i < len; i++) {
					OnScreenController checkedController = scheme[i];

					if ((checkedController.pointerId < 0)
						&& ((checkedController.renderModeMask & game.renderMode) != 0)
						&& checkedController.pointerDown(x, y)
					) {
						checkedController.pointerId = pointerId;
						checkedController.pointerMove(x, y);
						activeControllers[pointerId] = checkedController;
						break;
					}
				}
				break;

			case POINTER_ACTION_MOVE:
				if (controller != null) {
					if (!controller.pointerMove(x, y)) {
						controller.pointerUp();
						activeControllers[pointerId] = null;
					}
				}
				break;

			default:
				if (controller != null) {
					controller.pointerUp();
					activeControllers[pointerId] = null;
				}
				break;
		}
	}

	public void touchEvent(MotionEvent event) {
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		int points = event.getPointerCount();
		int i, aidx;

		switch (actionCode) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				aidx = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

				if (aidx < points) {
					processOnePointer(event.getPointerId(aidx), event.getX(aidx), event.getY(aidx), POINTER_ACTION_DOWN);
				}
				break;

			case MotionEvent.ACTION_MOVE:
				for (i = 0; i < points; i++) {
					processOnePointer(event.getPointerId(i), event.getX(i), event.getY(i), POINTER_ACTION_MOVE);
				}
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
				aidx = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

				if (aidx < points) {
					processOnePointer(event.getPointerId(aidx), event.getX(aidx), event.getY(aidx), POINTER_ACTION_UP);
				}
				break;
		}
	}

	public void drawIcon(
		float pointerX,
		float pointerY,
		int texNum,
		boolean pressed,
		boolean highlighted,
		long elapsedTime
	) {
		float screenX = pointerX / (float)engine.width * engine.ratio;
		float screenY = 1.0f - pointerY / (float)engine.height;

		float sx = screenX - 0.125f * config.controlsScale;
		float sy = screenY - 0.125f * config.controlsScale;
		float ex = sx + 0.25f * config.controlsScale;
		float ey = sy + 0.25f * config.controlsScale;

		renderer.x1 = sx; renderer.y1 = sy;
		renderer.x2 = sx; renderer.y2 = ey;
		renderer.x3 = ex; renderer.y3 = ey;
		renderer.x4 = ex; renderer.y4 = sy;

		if (pressed) {
			renderer.a1 = 1.0f;
		} else if (highlighted) {
			renderer.a1 = FloatMath.sin((float)elapsedTime * 0.01f) * 0.25f + 0.75f;
		} else {
			renderer.a1 = config.controlsAlpha;
		}

		renderer.a2 = renderer.a1;
		renderer.a3 = renderer.a1;
		renderer.a4 = renderer.a1;

		renderer.drawQuad(texNum);
	}

	public void drawBack(
		float pointerX,
		float pointerY,
		int texNum,
		boolean pressed,
		boolean highlighted,
		long elapsedTime
	) {
		float screenX = pointerX / (float)engine.width * engine.ratio;
		float screenY = 1.0f - pointerY / (float)engine.height;

		float sx = screenX - 0.25f * config.controlsScale;
		float sy = screenY - 0.25f * config.controlsScale;
		float ex = sx + 0.5f * config.controlsScale;
		float ey = sy + 0.5f * config.controlsScale;

		renderer.x1 = sx; renderer.y1 = sy;
		renderer.x2 = sx; renderer.y2 = ey;
		renderer.x3 = ex; renderer.y3 = ey;
		renderer.x4 = ex; renderer.y4 = sy;

		if (pressed) {
			renderer.a1 = 1.0f;
		} else if (highlighted) {
			renderer.a1 = FloatMath.sin((float)elapsedTime * 0.01f) * 0.25f + 0.75f;
		} else {
			renderer.a1 = config.controlsAlpha;
		}

		renderer.a2 = renderer.a1;
		renderer.a3 = renderer.a1;
		renderer.a4 = renderer.a1;

		renderer.drawQuad2x(texNum);
	}

	public void drawBtn(
		float pointerX,
		float pointerY,
		int texNum,
		boolean pressed,
		boolean highlighted,
		long elapsedTime
	) {
		float screenX = pointerX / (float)engine.width * engine.ratio;
		float screenY = 1.0f - pointerY / (float)engine.height;

		float sx = screenX - 0.5f * config.controlsScale;
		float sy = screenY - 0.125f * config.controlsScale;
		float ex = sx + 1.0f * config.controlsScale;
		float ey = sy + 0.25f * config.controlsScale;

		renderer.x1 = sx; renderer.y1 = sy;
		renderer.x2 = sx; renderer.y2 = ey;
		renderer.x3 = ex; renderer.y3 = ey;
		renderer.x4 = ex; renderer.y4 = sy;

		if (pressed) {
			renderer.a1 = 1.0f;
		} else if (highlighted) {
			renderer.a1 = FloatMath.sin((float)elapsedTime * 0.01f) * 0.25f + 0.75f;
		} else {
			renderer.a1 = config.controlsAlpha;
		}

		renderer.a2 = renderer.a1;
		renderer.a3 = renderer.a1;
		renderer.a4 = renderer.a1;

		renderer.drawQuad4x1x(texNum);
	}

	protected void drawArrow(GL10 gl, float sx, float sy, float ex, float ey, float hor, boolean drawLastSegment) {
		float dx = ex - sx;
		float dy = ey - sy;
		float dist = FloatMath.sqrt(dx * dx + dy * dy);
		float mx = dx / dist;
		float my = dy / dist;
		float nx = -my;
		float ny = mx;

		renderer.x1 = sx + mx * ARROW_N_SIZE + nx * LINE_WEIGHT; renderer.y1 = sy + my * ARROW_N_SIZE + ny * LINE_WEIGHT;
		renderer.x2 = sx + mx * ARROW_N_SIZE; renderer.y2 = sy + my * ARROW_N_SIZE;
		renderer.x3 = sx; renderer.y3 = sy;
		renderer.x4 = sx + mx * ARROW_F_SIZE + nx * ARROW_F_WEIGHT; renderer.y4 = sy + my * ARROW_F_SIZE + ny * ARROW_F_WEIGHT;
		renderer.drawQuad();

		renderer.x1 = sx; renderer.y1 = sy;
		renderer.x2 = sx + mx * ARROW_N_SIZE; renderer.y2 = sy + my * ARROW_N_SIZE;
		renderer.x3 = sx + mx * ARROW_N_SIZE - nx * LINE_WEIGHT; renderer.y3 = sy + my * ARROW_N_SIZE - ny * LINE_WEIGHT;
		renderer.x4 = sx + mx * ARROW_F_SIZE - nx * ARROW_F_WEIGHT; renderer.y4 = sy + my * ARROW_F_SIZE - ny * ARROW_F_WEIGHT;
		renderer.drawQuad();

		renderer.x1 = ex + nx * LINE_WEIGHT; renderer.y1 = ey + ny * LINE_WEIGHT;
		renderer.x2 = ex - nx * LINE_WEIGHT; renderer.y2 = ey - ny * LINE_WEIGHT;
		renderer.x3 = sx + mx * ARROW_N_SIZE - nx * LINE_WEIGHT; renderer.y3 = sy + my * ARROW_N_SIZE - ny * LINE_WEIGHT;
		renderer.x4 = sx + mx * ARROW_N_SIZE + nx * LINE_WEIGHT; renderer.y4 = sy + my * ARROW_N_SIZE + ny * LINE_WEIGHT;
		renderer.drawQuad();

		if (drawLastSegment) {
			renderer.x1 = ex + hor; renderer.y1 = ey + ny * LINE_WEIGHT;
			renderer.x2 = ex + hor; renderer.y2 = ey - ny * LINE_WEIGHT;
			renderer.x3 = ex - nx * LINE_WEIGHT; renderer.y3 = ey - ny * LINE_WEIGHT;
			renderer.x4 = ex + nx * LINE_WEIGHT; renderer.y4 = ey + ny * LINE_WEIGHT;
			renderer.drawQuad();
		}
	}

	protected void subRenderControls(GL10 gl, long elapsedTime) {
		renderer.init();

		for (int i = 0, len = scheme.length; i < len; i++) {
			OnScreenController controller = scheme[i];

			if ((controller.renderModeMask & game.renderMode) != 0) {
				controller.render(gl, elapsedTime);
			}
		}

		renderer.bindTextureCtl(gl, textureLoader.textures[TextureLoader.TEXTURE_MAIN]);
		renderer.flush(gl);
	}

	protected void subRenderHelp(GL10 gl, boolean helpOverlayFullMode) {
		for (int i = 0, len = scheme.length; i < len; i++) {
			OnScreenController controller = scheme[i];

			if ((controller.renderModeMask & game.renderMode) != 0 && controller.helpLabelId >= 0) {
				if (!helpOverlayFullMode && !(controller instanceof OnScreenFireAndRotate)) {
					continue;
				}

				String helpText = labels.map[controller.helpLabelId];
				float diagSize = (controller instanceof OnScreenPad ? DIAG_B_SIZE : DIAG_SIZE);
				float horSize = labels.getScaledWidth(helpText, 0.04f);
				boolean posLeft = ((controller.position & Controls.POSITION_RIGHT) == 0);
				boolean posBottom = ((controller.position & Controls.POSITION_TOP) == 0);

				float sx = controller.getDisplayX() / (float)engine.width * engine.ratio;
				float sy = 1.0f - controller.getDisplayY() / (float)engine.height;
				float ex = sx + (posLeft ? diagSize : -diagSize) * engine.ratio;
				float ey = sy + (posBottom ? diagSize : -diagSize);
				float hor = (posLeft ? horSize : -horSize);

				renderer.init();
				renderer.setQuadA(0.5f);
				drawArrow(gl, sx, sy, ex, ey, hor, true);
				renderer.flush(gl, false);

				labels.beginDrawing(gl, true);
				renderer.setQuadA(1.0f);

				if (posLeft) {
					if (posBottom) {
						labels.draw(gl, ex, ey + 0.01f, ex + engine.ratio * 0.4f, ey + 0.06f, helpText, 0.04f, Labels.ALIGN_CL);
					} else {
						labels.draw(gl, ex, ey - 0.05f, ex + engine.ratio * 0.4f, ey, helpText, 0.04f, Labels.ALIGN_CL);
					}
				} else {
					if (posBottom) {
						labels.draw(gl, ex - engine.ratio * 0.4f, ey + 0.01f, ex, ey + 0.06f, helpText, 0.04f, Labels.ALIGN_CR);
					} else {
						labels.draw(gl, ex - engine.ratio * 0.4f, ey - 0.05f, ex, ey, helpText, 0.04f, Labels.ALIGN_CR);
					}
				}

				labels.endDrawing(gl, true);

				if (controller instanceof OnScreenFireAndRotate) {
					sx = ((posLeft ? 0.25f : 0.75f) - 0.2f) * engine.ratio;
					ex = ((posLeft ? 0.25f : 0.75f) + 0.2f) * engine.ratio;

					renderer.init();
					renderer.setQuadA(0.5f);
					drawArrow(gl, ex, 0.55f, sx, 0.55f, 0f, false);
					drawArrow(gl, sx, 0.45f, ex, 0.45f, 0.0f, false);
					renderer.flush(gl, false);

					labels.beginDrawing(gl, true);
					renderer.setQuadA(1.0f);
					labels.draw(gl, sx, 0.45f, ex, 0.55f, labels.map[Labels.LABEL_HELP_ROTATE], 0.04f, Labels.ALIGN_CC);
					labels.endDrawing(gl, true);
				}
			}
		}
	}

	protected void subRenderBlackOverlay(GL10 gl, long elapsedTime, long firstTouchTime) {
		float op = 0.5f - (float)(elapsedTime - firstTouchTime) / 500.0f;

		if (op > 0.0f) {
			renderer.init();
			renderer.setQuadRGBA(0.0f, 0.0f, 0.0f, op);
			renderer.setQuadOrthoCoords(0.0f, 0.0f, engine.ratio, 1.0f);
			renderer.drawQuad();
			renderer.flush(gl, false);
		}
	}

	public void render(GL10 gl, long elapsedTime, boolean renderHelpOverlay, boolean helpOverlayFullMode, long firstTouchTime) {
		renderer.z1 = 0.0f;
		renderer.z2 = 0.0f;
		renderer.z3 = 0.0f;
		renderer.z4 = 0.0f;

		renderer.initOrtho(gl, true, false, 0f, engine.ratio, 0.0f, 1.0f, 0f, 1.0f);

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		if (renderHelpOverlay) {
			subRenderBlackOverlay(gl, elapsedTime, firstTouchTime);
		}

		renderer.setQuadRGB(1.0f, 1.0f, 1.0f);
		subRenderControls(gl, elapsedTime);

		if (renderHelpOverlay) {
			subRenderHelp(gl, helpOverlayFullMode);
		}

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
	}

	public void updateHero() {
		for (int i = 0, len = scheme.length; i < len; i++) {
			OnScreenController controller = scheme[i];

			if ((controller.renderModeMask & game.renderMode) != 0) {
				controller.updateHero();
			}
		}
	}
}
