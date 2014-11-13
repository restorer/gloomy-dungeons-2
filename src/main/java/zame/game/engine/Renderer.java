package zame.game.engine;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.opengles.GL10;

// http://stackoverflow.com/questions/1848886/jni-c-library-passing-byte-ptr
// http://groups.google.com/group/android-ndk/tree/browse_frm/month/2010-01?_done=/group/android-ndk/browse_frm/month/2010-01%3F&

// Native buffers (aka ByteBuffer, ShortBuffer and FloatBuffer) suck in DalvikVM. It is terribly slow.
// So native code used to render. It's up to 4x faster than java code with native buffers.

public class Renderer implements EngineObject {
	static {
		System.loadLibrary("renderer");
	}

	protected static native void renderTriangles(float[] vertexBuf, float[] colorsBuf, int[] textureBuf, short[] indicesBuf, int indicesBufPos);
	protected static native void renderLines(float[] vertexBuf, float[] colorsBuf, int vertexCount);

	public static final float ALPHA_VALUE = 0.5f;
	protected static final int MAX_QUADS = 64 * 64 * 2;
	// (64*64*2 * (12*4 + 16*4 + 8*4 + 6*2 + 4*4 + 8*4))

	protected float[] vertexBuffer = new float[MAX_QUADS * 12];
	protected float[] colorsBuffer = new float[MAX_QUADS * 16];
	protected int[] textureBuffer = new int[MAX_QUADS * 8];
	protected short[] indicesBuffer = new short[MAX_QUADS * 6];
	protected float[] lineVertexBuffer = new float[MAX_QUADS * 4];
	protected float[] lineColorsBuffer = new float[MAX_QUADS * 8];

	protected int vertexBufferPos;
	protected int colorsBufferPos;
	protected int textureBufferPos;
	protected int indicesBufferPos;
	protected int lineVertexBufferPos;
	protected int lineColorsBufferPos;

	protected short vertexCount;
	protected short lineVertexCount;

	protected Engine engine;
	protected Config config;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.config = engine.config;
	}

	public void init() {
		vertexCount = 0;
		lineVertexCount = 0;

		vertexBufferPos = 0;
		colorsBufferPos = 0;
		textureBufferPos = 0;
		indicesBufferPos = 0;
		lineVertexBufferPos = 0;
		lineColorsBufferPos = 0;
	}

	public void flush(GL10 gl) {
		flush(gl, true);
	}

	public void flush(GL10 gl, boolean useTextures) {
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		if (indicesBufferPos != 0) {
			if (useTextures) {
				gl.glEnable(GL10.GL_TEXTURE_2D);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			} else {
				gl.glDisable(GL10.GL_TEXTURE_2D);
				gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			}

			renderTriangles(vertexBuffer, colorsBuffer, (useTextures ? textureBuffer : null), indicesBuffer, indicesBufferPos);
		}

		if (lineVertexCount != 0) {
			gl.glDisable(GL10.GL_TEXTURE_2D);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			renderLines(lineVertexBuffer, lineColorsBuffer, lineVertexCount);
		}
	}

	public void bindTexture(GL10 gl, int tex) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, config.levelTextureFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, config.levelTextureFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
	}

	public void bindTextureRep(GL10 gl, int tex) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, config.levelTextureFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, config.levelTextureFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
	}

	public void bindTextureCtl(GL10 gl, int tex) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, config.weaponsTextureFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, config.weaponsTextureFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
	}

	public void bindTextureCtlRep(GL10 gl, int tex) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, config.weaponsTextureFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, config.weaponsTextureFilter);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
	}

	public void bindTextureBlur(GL10 gl, int tex) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
	}

	public void initOrtho(
		GL10 gl,
		boolean pushProjection,
		boolean pushModel,
		float left,
		float right,
		float bottom,
		float top,
		float near,
		float far
	) {
		gl.glMatrixMode(GL10.GL_PROJECTION);

		if (pushProjection) {
			gl.glPushMatrix();
		}

		gl.glLoadIdentity();

		if (!engine.inWallpaperMode && config.rotateScreen) {
			gl.glOrthof(right, left, top, bottom, near, far);
		} else {
			gl.glOrthof(left, right, bottom, top, near, far);
		}

		gl.glMatrixMode(GL10.GL_MODELVIEW);

		if (pushModel) {
			gl.glPushMatrix();
		}

		gl.glLoadIdentity();
	}

	public void initFrustum(GL10 gl, float left, float right, float bottom, float top, float near, float far) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		if (!engine.inWallpaperMode && config.rotateScreen) {
			gl.glFrustumf(right, left, top, bottom, near, far);
		} else {
			gl.glFrustumf(left, right, bottom, top, near, far);
		}
	}

	public void frustrumModelIdentity(GL10 gl) {
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	public float x1, y1, z1;
	public int u1; public int v1;
	public float r1; public float g1; public float b1; public float a1;

	public float x2; public float y2; public float z2;
	public int u2; public int v2;
	public float r2; public float g2; public float b2; public float a2;

	public float x3; public float y3; public float z3;
	public int u3; public int v3;
	public float r3; public float g3; public float b3; public float a3;

	public float x4; public float y4; public float z4;
	public int u4; public int v4;
	public float r4; public float g4; public float b4; public float a4;

	// In-game:
	//
	//	1 | 2
	// ---+--->
	//	4 | 3
	//	  v
	//
	// Ortho:
	//
	//  2 | 3
	// ---+--->
	//  1 | 4
	//    v
	//
	public void drawQuad() {
		int vertexBufferPosL = vertexBufferPos;
		int colorsBufferPosL = colorsBufferPos;
		int textureBufferPosL = textureBufferPos;
		int indicesBufferPosL = indicesBufferPos;
		short vertexCountL = vertexCount;

		float[] vertexBufferL = vertexBuffer;
		float[] colorsBufferL = colorsBuffer;
		int[] textureBufferL = textureBuffer;
		short[] indicesBufferL = indicesBuffer;

		vertexBufferL[vertexBufferPosL] = x1; vertexBufferL[vertexBufferPosL + 1] = y1; vertexBufferL[vertexBufferPosL + 2] = z1;
		vertexBufferL[vertexBufferPosL + 3] = x2; vertexBufferL[vertexBufferPosL + 4] = y2; vertexBufferL[vertexBufferPosL + 5] = z2;
		vertexBufferL[vertexBufferPosL + 6] = x3; vertexBufferL[vertexBufferPosL + 7] = y3; vertexBufferL[vertexBufferPosL + 8] = z3;
		vertexBufferL[vertexBufferPosL + 9] = x4; vertexBufferL[vertexBufferPosL + 10] = y4; vertexBufferL[vertexBufferPosL + 11] = z4;

		colorsBufferL[colorsBufferPosL] = r1; colorsBufferL[colorsBufferPosL + 1] = g1;
		colorsBufferL[colorsBufferPosL + 2] = b1; colorsBufferL[colorsBufferPosL + 3] = a1;
		colorsBufferL[colorsBufferPosL + 4] = r2; colorsBufferL[colorsBufferPosL + 5] = g2;
		colorsBufferL[colorsBufferPosL + 6] = b2; colorsBufferL[colorsBufferPosL + 7] = a2;
		colorsBufferL[colorsBufferPosL + 8] = r3; colorsBufferL[colorsBufferPosL + 9] = g3;
		colorsBufferL[colorsBufferPosL + 10] = b3; colorsBufferL[colorsBufferPosL + 11] = a3;
		colorsBufferL[colorsBufferPosL + 12] = r4; colorsBufferL[colorsBufferPosL + 13] = g4;
		colorsBufferL[colorsBufferPosL + 14] = b4; colorsBufferL[colorsBufferPosL + 15] = a4;

		textureBufferL[textureBufferPosL] = u1; textureBufferL[textureBufferPosL + 1] = v1;
		textureBufferL[textureBufferPosL + 2] = u2; textureBufferL[textureBufferPosL + 3] = v2;
		textureBufferL[textureBufferPosL + 4] = u3; textureBufferL[textureBufferPosL + 5] = v3;
		textureBufferL[textureBufferPosL + 6] = u4; textureBufferL[textureBufferPosL + 7] = v4;

		indicesBufferL[indicesBufferPosL] = vertexCountL;
		indicesBufferL[indicesBufferPosL + 1] = (short)(vertexCountL + 2);
		indicesBufferL[indicesBufferPosL + 2] = (short)(vertexCountL + 1);
		indicesBufferL[indicesBufferPosL + 3] = vertexCountL;
		indicesBufferL[indicesBufferPosL + 4] = (short)(vertexCountL + 3);
		indicesBufferL[indicesBufferPosL + 5] = (short)(vertexCountL + 2);

		vertexBufferPos = vertexBufferPosL + 12;
		colorsBufferPos = colorsBufferPosL + 16;
		textureBufferPos = textureBufferPos + 8;
		indicesBufferPos = indicesBufferPosL + 6;
		vertexCount = (short)(vertexCountL + 4);
	}

	private static final int TEX_CELL_X = (66 << 16) / 1024;
	private static final int TEX_CELL_Y = (66 << 16) / 1024;
	private static final int TEX_1PX_X = (1 << 16) / 1024;
	private static final int TEX_1PX_Y = (1 << 16) / 1024;
	private static final int TEX_SIZE_X = (64 << 16) / 1024;
	private static final int TEX_SIZE_Y = (64 << 16) / 1024;
	private static final int TEX_SIZE_X_D2 = (32 << 16) / 1024;
	private static final int TEX_SIZE_Y_D2 = (32 << 16) / 1024;
	private static final int TEX_SIZE_X_D2_1PX = TEX_SIZE_X_D2 + TEX_1PX_X;
	private static final int TEX_SIZE_Y_D2_1PX = TEX_SIZE_Y_D2 + TEX_1PX_Y;
	private static final int TEX_SIZE_X_X2 = (128 << 16) / 1024;
	private static final int TEX_SIZE_Y_X2 = (128 << 16) / 1024;
	private static final int TEX_SIZE_X_X4 = (256 << 16) / 1024;
	private static final int TEX_31PX_X = (31 << 16) / 1024;
	private static final int TEX_SIZE_4PX_X = (4 << 16) / 1024;

	public void drawQuad(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y;
		drawQuad();
	}

	public void drawQuad4PX(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_31PX_X) + TEX_SIZE_4PX_X;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y;
		drawQuad();
	}

	public void drawQuad1(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_D2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_D2;
		drawQuad();
	}

	public void drawQuad2(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_SIZE_X_D2_1PX) + TEX_SIZE_X_D2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_D2;
		drawQuad();
	}

	public void drawQuad3(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_D2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_SIZE_Y_D2_1PX) + TEX_SIZE_Y_D2;
		drawQuad();
	}

	public void drawQuad4(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_SIZE_X_D2_1PX) + TEX_SIZE_X_D2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_SIZE_Y_D2_1PX) + TEX_SIZE_Y_D2;
		drawQuad();
	}

	public void drawQuad2x(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_X2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_X2;
		drawQuad();
	}

	public void drawQuad4x1x(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_X4;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y;
		drawQuad();
	}

	public void drawQuadFlipLR(int texNum) {
		u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y;
		drawQuad();
	}

	public void drawQuadFlipLR1(int texNum) {
		u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_D2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_D2;
		drawQuad();
	}

	public void drawQuadFlipLR2(int texNum) {
		u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_SIZE_X_D2_1PX) + TEX_SIZE_X_D2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_D2;
		drawQuad();
	}

	public void drawQuadFlipLR3(int texNum) {
		u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_D2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_SIZE_Y_D2_1PX) + TEX_SIZE_Y_D2;
		drawQuad();
	}

	public void drawQuadFlipLR4(int texNum) {
		u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_SIZE_X_D2_1PX) + TEX_SIZE_X_D2;
		v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_SIZE_Y_D2_1PX) + TEX_SIZE_Y_D2;
		drawQuad();
	}

	private static final int TEX_CELL_MON = (128 << 16) / 1024;
	private static final int TEX_1PX_MON = (1 << 16) / 1024;
	private static final int TEX_SIZE_MON = (126 << 16) / 1024;

	public void drawQuadMon(int texNum) {
		u3 = u4 = (u1 = u2 = (texNum % 8) * TEX_CELL_MON + TEX_1PX_MON) + TEX_SIZE_MON;
		v1 = v4 = (v2 = v3 = (texNum / 8) * TEX_CELL_MON + TEX_1PX_MON) + TEX_SIZE_MON;
		drawQuad();
	}

	public void drawLine() {
		int lineVertexBufferPosL = lineVertexBufferPos;
		int lineColorsBufferPosL = lineColorsBufferPos;
		float[] lineVertexBufferL = lineVertexBuffer;
		float[] lineColorsBufferL = lineColorsBuffer;

		lineVertexBufferL[lineVertexBufferPosL] = x1; lineVertexBufferL[lineVertexBufferPosL + 1] = y1;
		lineVertexBufferL[lineVertexBufferPosL + 2] = x2; lineVertexBufferL[lineVertexBufferPosL + 3] = y2;

		lineColorsBufferL[lineColorsBufferPosL] = r1; lineColorsBufferL[lineColorsBufferPosL + 1] = g1;
		lineColorsBufferL[lineColorsBufferPosL + 2] = b1; lineColorsBufferL[lineColorsBufferPosL + 3] = a1;
		lineColorsBufferL[lineColorsBufferPosL + 4] = r2; lineColorsBufferL[lineColorsBufferPosL + 5] = g2;
		lineColorsBufferL[lineColorsBufferPosL + 6] = b2; lineColorsBufferL[lineColorsBufferPosL + 7] = a2;

		lineVertexBufferPos = lineVertexBufferPosL + 4;
		lineColorsBufferPos = lineColorsBufferPosL + 8;
		lineVertexCount += 2;
	}

	public void drawLine(float lx1, float ly1, float lx2, float ly2) {
		int lineVertexBufferPosL = lineVertexBufferPos;
		int lineColorsBufferPosL = lineColorsBufferPos;
		float[] lineVertexBufferL = lineVertexBuffer;
		float[] lineColorsBufferL = lineColorsBuffer;

		lineVertexBufferL[lineVertexBufferPosL] = lx1; lineVertexBufferL[lineVertexBufferPosL + 1] = ly1;
		lineVertexBufferL[lineVertexBufferPosL + 2] = lx2; lineVertexBufferL[lineVertexBufferPosL + 3] = ly2;

		lineColorsBufferL[lineColorsBufferPosL] = r1; lineColorsBufferL[lineColorsBufferPosL + 1] = g1;
		lineColorsBufferL[lineColorsBufferPosL + 2] = b1; lineColorsBufferL[lineColorsBufferPosL + 3] = a1;
		lineColorsBufferL[lineColorsBufferPosL + 4] = r2; lineColorsBufferL[lineColorsBufferPosL + 5] = g2;
		lineColorsBufferL[lineColorsBufferPosL + 6] = b2; lineColorsBufferL[lineColorsBufferPosL + 7] = a2;

		lineVertexBufferPos = lineVertexBufferPosL + 4;
		lineColorsBufferPos = lineColorsBufferPosL + 8;
		lineVertexCount += 2;
	}

	public void setQuadRGB(float r, float g, float b) {
		r1 = r; g1 = g; b1 = b;
		r2 = r; g2 = g; b2 = b;
		r3 = r; g3 = g; b3 = b;
		r4 = r; g4 = g; b4 = b;
	}

	public void setQuadA(float a) {
		a1 = a;
		a2 = a;
		a3 = a;
		a4 = a;
	}

	public void setQuadRGBA(float r, float g, float b, float a) {
		r1 = r; g1 = g; b1 = b; a1 = a;
		r2 = r; g2 = g; b2 = b; a2 = a;
		r3 = r; g3 = g; b3 = b; a3 = a;
		r4 = r; g4 = g; b4 = b; a4 = a;
	}

	public void setLineRGB(float r, float g, float b) {
		r1 = r; g1 = g; b1 = b;
		r2 = r; g2 = g; b2 = b;
	}

	public void setLineA(float a) {
		a1 = a;
		a2 = a;
	}

	public void setLineRGBA(float r, float g, float b, float a) {
		r1 = r; g1 = g; b1 = b; a1 = a;
		r2 = r; g2 = g; b2 = b; a2 = a;
	}

	public void setQuadOrthoCoords(float lx1, float ly1, float lx2, float ly2) {
		x1 = lx1; y1 = ly1; z1 = 0.0f;
		x2 = lx1; y2 = ly2; z2 = 0.0f;
		x3 = lx2; y3 = ly2; z3 = 0.0f;
		x4 = lx2; y4 = ly1; z4 = 0.0f;
	}

	public void setQuadTexCoords(int sx, int sy, int ex, int ey) {
		u1 = sx; v1 = sy;
		u2 = sx; v2 = ey;
		u3 = ex; v3 = ey;
		u4 = ex; v4 = sy;
	}
}
