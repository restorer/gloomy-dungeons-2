package zame.game.engine;

import android.util.FloatMath;
import javax.microedition.khronos.opengles.GL10;
import zame.game.managers.SoundManager;

public class Weapons implements EngineObject {
	public static class WeaponParams {
		public int[] cycle;
		public int ammoIdx;
		public int needAmmo;
		public int hits;
		public int hitTimeout;
		public int textureBase;
		public float xmult;
		public float xoff;
		public float hgt;
		public int soundIdx;
		public boolean isNear;
		public int noHitSoundIdx;
		public String name;
		public String description;

		public WeaponParams(
			int[] cycle,
			int ammoIdx,
			int needAmmo,
			int hits,
			int hitTimeout,
			int textureBase,
			float xmult,
			float xoff,
			float hgt,
			int soundIdx,
			boolean isNear,
			int noHitSoundIdx,
			String name
		) {
			this.cycle = cycle;
			this.ammoIdx = ammoIdx;
			this.needAmmo = needAmmo;
			this.hits = hits;
			this.hitTimeout = hitTimeout;
			this.textureBase = textureBase;
			this.xmult = xmult;
			this.xoff = xoff;
			this.hgt = hgt;
			this.soundIdx = soundIdx;
			this.isNear = isNear;
			this.noHitSoundIdx = noHitSoundIdx;
			this.name = name;

			makeDescription();
		}

		protected void makeDescription() {
			StringBuilder sb = new StringBuilder(name);

			if (isNear) {
				sb.append(" / NEAR");
			}

			int damage = 0;

			for (int i = 0; i < cycle.length; i++) {
				if (cycle[i] < 0) {
					damage += hits;
				}
			}

			sb.append(" / DMG ");
			sb.append((int)FloatMath.ceil((float)damage * 3.125f));

			sb.append(" / SPD ");
			sb.append((int)FloatMath.ceil(100.0f / (float)hitTimeout));

			if (needAmmo > 0) {
				sb.append(" / AMMO ");
				sb.append(needAmmo);
			}

			description = sb.toString();
		}
	}

	public static final int AMMO_PISTOL = 0;
	public static final int AMMO_SHOTGUN = 1;
	public static final int AMMO_ROCKET = 2;
	public static final int AMMO_LAST = 3;

	public static final int[] AMMO_OBJ_TEX_MAP = {
		TextureLoader.OBJ_CLIP,
		TextureLoader.OBJ_SHELL,
		TextureLoader.OBJ_ROCKET,
	};

	public static final int WEAPON_HAND = 0;		// required to be 0
	public static final int WEAPON_PISTOL = 1;
	public static final int WEAPON_SHOTGUN = 2;
	public static final int WEAPON_CHAINGUN = 3;
	public static final int WEAPON_DBLSHOTGUN = 4;
	public static final int WEAPON_RLAUNCHER = 5;
	public static final int WEAPON_DBLCHAINGUN = 6;
	public static final int WEAPON_CHAINSAW = 7;
	public static final int WEAPON_DBLPISTOL = 8;
	public static final int WEAPON_PDBLSHOTGUN = 9;
	public static final int WEAPON_LAST = 10;

	protected static final float WALK_OFFSET_X = 1.0f / 8.0f;
	protected static final float DEFAULT_HEIGHT = 1.5f;

	public static final WeaponParams[] WEAPONS = new WeaponParams[] {
		// WEAPON_HAND
		new WeaponParams(
			new int[] {
				0, 1, 1, 1, 2, 2, 2, -3, 3, 3, 2, 2, 2, 1, 1, 0, 0
			},
			-1, 0,
			GameParams.HEALTH_HIT_HAND, 5,
			TextureLoader.TEXTURE_HAND, 1.0f, WALK_OFFSET_X, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_HAND, true, SoundManager.SOUND_NOWAY,
			"HANDS"
		),
		// WEAPON_PISTOL
		new WeaponParams(
			new int[] {
				0,
				-1, 1, 1, 1, 1,
				2, 2, 2, 2, 2,
				3, 3, 3, 3, 3,
				0, 0, 0, 0, 0, 0
			},
			AMMO_PISTOL, 1,
			GameParams.HEALTH_HIT_PISTOL, 5,
			TextureLoader.TEXTURE_PIST, 1.0f, 0.125f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_PIST, false, 0,
			"PISTOL / ML37-A"
		),
		// WEAPON_SHOTGUN
		new WeaponParams(
			new int[] {
				0, 0, 0, 0, 0,
				1, 1, 1, 1, 1,
				-2, 2, 2, 2, 2,
				3, 3, 3, 3, 3,
				4, 4, 4, 4, 4,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
			},
			AMMO_SHOTGUN, 1,
			GameParams.HEALTH_HIT_SHOTGUN, 10,
			TextureLoader.TEXTURE_SHTG, 0.9f, 0.25f, DEFAULT_HEIGHT * 0.9f,
			SoundManager.SOUND_SHOOT_SHTG, false, 0,
			"SHOTGUN / SK25-S3"
		),
		// WEAPON_CHAINGUN
		new WeaponParams(
			new int[] {
				0,
				1, 1, 1,
				-2, 2, 2,
				-3, 3, 3,
				0, 0
			},
			AMMO_PISTOL, 1,
			GameParams.HEALTH_HIT_CHAINGUN, 5,
			TextureLoader.TEXTURE_CHGN, 0.8f, 0.125f, DEFAULT_HEIGHT * 0.8f,
			SoundManager.SOUND_SHOOT_PIST, false, 0,
			"CHAINGUN / MTH72"
		),
		// WEAPON_DBLSHOTGUN
		new WeaponParams(
			new int[] {
				0,
				-1, 1, 1, 1, 1,
				2, 2, 2, 2, 2,
				3, 3, 3, 3, 3,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
			},
			AMMO_SHOTGUN, 2,
			GameParams.HEALTH_HIT_DBLSHOTGUN, 20,
			TextureLoader.TEXTURE_DBLSHTG, 0.8f, 0.0625f, DEFAULT_HEIGHT * 0.8f,
			SoundManager.SOUND_SHOOT_DBLSHTG, false, 0,
			"DOUBLE SHOTGUN / SK34-D5"
		),
		// WEAPON_RLAUNCHER
		new WeaponParams(
			new int[] {
				0,
				1, 1, 1, 1,
				-2, 2, 2, 2, 2, 2,
				3, 3, 3, 3, 3, 3,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0,
			},
			AMMO_ROCKET, 1,
			GameParams.HEALTH_HIT_RLAUNCHER, 25,
			TextureLoader.TEXTURE_RLAUNCHER, 0.75f, 0.275f, DEFAULT_HEIGHT * 0.8f,
			SoundManager.SOUND_SHOOT_ROCKET, false, 0,
			"ROCKET LAUNCHER / RL3-S2"
		),
		// WEAPON_DBLCHAINGUN
		new WeaponParams(
			new int[] {
				0,
				1, 1, 1,
				-2, 2, 2,
				-3, 3, 3,
				0, 0
			},
			AMMO_PISTOL, 2,
			GameParams.HEALTH_HIT_DBLCHAINGUN, 8,
			TextureLoader.TEXTURE_DBLCHGN, 1.0f + WALK_OFFSET_X, 0.0f, DEFAULT_HEIGHT * (1.0f + WALK_OFFSET_X),
			SoundManager.SOUND_SHOOT_PIST, false, 0,
			"DOUBLE CHAINGUN / MTH72-D4"
		),
		// WEAPON_CHAINSAW
		new WeaponParams(
			new int[] {
				0, 0, 0, 0, 0,
				1, 1, 1, 1, 1,
				-2,    2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				-1002, 2, 2, 2, -1002, 3, 3, 3, -1003, 3,
				1, 1, 1, 1, 1,
				0, 0, 0, 0, 0
			},
			-1, 0,
			GameParams.HEALTH_HIT_CHAINSAW, 4,
			TextureLoader.TEXTURE_SAW, 0.8f, 0.05f, DEFAULT_HEIGHT * 0.8f,
			SoundManager.SOUND_SHOOT_SAW, true, 0,
			"CHAINSAW / CHLP-285"
		),
		// WEAPON_DBLPISTOL
		new WeaponParams(
			new int[] {
				0,
				-1, 1, 1, 1,
				0, 0, 0, 0,
				-2, 2, 2, 2,
				0, 0, 0
			},
			AMMO_PISTOL, 1,
			GameParams.HEALTH_HIT_DBLPISTOL, 5,
			TextureLoader.TEXTURE_DBLPIST, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_PIST, false, 0,
			"DOUBLE PISTOL / ML39-D"
		),
		// WEAPON_PDBLSHOTGUN
		new WeaponParams(
			new int[] {
				0,
				-1, 1, 1, 1, 1,
				2, 2, 2, 2, 2,
				3, 3, 3, 3, 3,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
			},
			AMMO_SHOTGUN, 2,
			GameParams.HEALTH_HIT_DBLSHOTGUN, 20,
			TextureLoader.TEXTURE_PDBLSHTG, 0.8f, 0.275f, DEFAULT_HEIGHT * 0.8f,
			SoundManager.SOUND_SHOOT_DBLSHTG, false, 0,
			"DOUBLE SHOTGUN / PR17-L4I"
		),
	};

	protected Engine engine;
	protected State state;
	protected Renderer renderer;
	protected TextureLoader textureLoader;

	public WeaponParams currentParams;
	public int[] currentCycle;
	public int shootCycle;
	public int changeWeaponDir;
	public int changeWeaponNext;
	public long changeWeaponTime;

	public void setEngine(Engine engine) {
		this.engine = engine;
		this.state = engine.state;
		this.renderer = engine.renderer;
		this.textureLoader = engine.textureLoader;
	}

	public void init() {
		shootCycle = 0;
		changeWeaponDir = 0;
		updateWeapon();
	}

	public void updateWeapon() {
		currentParams = WEAPONS[state.heroWeapon];
		currentCycle = currentParams.cycle;
		shootCycle = 0;
	}

	public void switchWeapon(int weaponIdx) {
		changeWeaponNext = weaponIdx;
		changeWeaponTime = engine.elapsedTime;
		changeWeaponDir = -1;
	}

	public boolean hasNoAmmo(int weaponIdx) {
		return ((WEAPONS[weaponIdx].ammoIdx >= 0) && (state.heroAmmo[WEAPONS[weaponIdx].ammoIdx] < WEAPONS[weaponIdx].needAmmo));
	}

	public boolean canSwitch(int weaponIdx) {
		return (state.heroHasWeapon[weaponIdx] && !hasNoAmmo(weaponIdx));
	}

	public void nextWeapon() {
		int resWeapon = (state.heroWeapon + 1) % WEAPON_LAST;

		while ((resWeapon != 0) && (!state.heroHasWeapon[resWeapon] || hasNoAmmo(resWeapon))) {
			resWeapon = (resWeapon + 1) % WEAPON_LAST;
		}

		switchWeapon(resWeapon);
	}

	public int getBestWeapon() {
		int resWeapon = WEAPON_LAST - 1;

		while (resWeapon > 0 && (
			!state.heroHasWeapon[resWeapon]
			|| hasNoAmmo(resWeapon)
			|| WEAPONS[resWeapon].isNear
			/* || WEAPONS[resWeapon].ammoIdx == AMMO_ROCKET */
		)) {
			resWeapon--;
		}

		if (resWeapon == 0) {
			resWeapon = WEAPON_LAST - 1;

			while (resWeapon > 0 && (
				!state.heroHasWeapon[resWeapon]
				|| hasNoAmmo(resWeapon)
				|| !(WEAPONS[resWeapon].isNear /* || WEAPONS[resWeapon].ammoIdx == AMMO_ROCKET */)
			)) {
				resWeapon--;
			}
		}

		return resWeapon;
	}

	public void selectBestWeapon() {
		int bestWeapon = getBestWeapon();

		if (bestWeapon != state.heroWeapon) {
			switchWeapon(bestWeapon);
		}
	}

	public void render(GL10 gl, long walkTime) {
		renderer.r1 = 1.0f; renderer.g1 = 1.0f; renderer.b1 = 1.0f; renderer.a1 = 1.0f;
		renderer.r2 = 1.0f; renderer.g2 = 1.0f; renderer.b2 = 1.0f; renderer.a2 = 1.0f;
		renderer.r3 = 1.0f; renderer.g3 = 1.0f; renderer.b3 = 1.0f; renderer.a3 = 1.0f;
		renderer.r4 = 1.0f; renderer.g4 = 1.0f; renderer.b4 = 1.0f; renderer.a4 = 1.0f;

		renderer.z1 = 0.0f;
		renderer.z2 = 0.0f;
		renderer.z3 = 0.0f;
		renderer.z4 = 0.0f;

		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

		renderer.initOrtho(gl, true, false, -1.0f, 1.0f, 0.0f, 2.0f, 0f, 1.0f);
		gl.glDisable(GL10.GL_ALPHA_TEST);

		renderer.init();

		float yoff = 0;

		if (changeWeaponDir == -1) {
			yoff = (float)(engine.elapsedTime - changeWeaponTime) / 150.0f;

			if (yoff >= currentParams.hgt + 0.1f) {
				state.heroWeapon = changeWeaponNext;
				updateWeapon();

				changeWeaponDir = 1;
				changeWeaponTime = engine.elapsedTime;
			}
		} else if (changeWeaponDir == 1) {
			yoff = currentParams.hgt + 0.1f - (float)(engine.elapsedTime - changeWeaponTime) / 150.0f;

			if (yoff <= 0.0f) {
				yoff = 0.0f;
				changeWeaponDir = 0;
			}
		}

		float xoff = FloatMath.sin((float)walkTime / 150.0f) * WALK_OFFSET_X;
		float xlt = -currentParams.xmult + currentParams.xoff + xoff;
		float xrt = currentParams.xmult + currentParams.xoff + xoff;

		yoff += Math.abs(FloatMath.sin((float)walkTime / 150.0f + GameMath.PI_F / 2.0f)) * 0.1f + 0.05f;
		float hgt = currentParams.hgt - yoff;

		renderer.x1 = xlt; renderer.y1 = -yoff;
		renderer.x2 = xlt; renderer.y2 = hgt;
		renderer.x3 = xrt; renderer.y3 = hgt;
		renderer.x4 = xrt; renderer.y4 = -yoff;

		renderer.u1 = 0; renderer.v1 = 1 << 16;
		renderer.u2 = 0; renderer.v2 = 0;
		renderer.u3 = 1 << 16; renderer.v3 = 0;
		renderer.u4 = 1 << 16; renderer.v4 = 1 << 16;

		renderer.drawQuad();

		// just for case
		if (shootCycle > currentCycle.length) {
			shootCycle = 0;
		}

		int weaponTexture = currentCycle[shootCycle];

		if (weaponTexture < -1000) {
			weaponTexture = -1000 - weaponTexture;
		} else if (weaponTexture < 0) {
			weaponTexture = -weaponTexture;
		}

		renderer.bindTextureCtl(gl, textureLoader.textures[currentParams.textureBase + weaponTexture]);
		renderer.flush(gl);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();

		gl.glDisable(GL10.GL_ALPHA_TEST);
	}
}
