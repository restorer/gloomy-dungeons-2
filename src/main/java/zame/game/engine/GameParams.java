package zame.game.engine;

public class GameParams {
	public static final int HEALTH_MAX = 100;
	public static final int HEALTH_ADDITIONAL = 50;
	public static final int HEALTH_ADD_STIM = 10;
	public static final int HEALTH_ADD_MEDI = 50;
	public static final float HEALTH_HIT_MONSTER_MULT_NEWBIE = 1.5f;
	public static final float HEALTH_HIT_MONSTER_MULT_EASY = 1.25f;
	public static final float HEALTH_HIT_MONSTER_MULT_HARD = 0.75f;
	public static final float HEALTH_HIT_MONSTER_MULT_ULTIMATE = 0.5f;
	public static final float HEALTH_HIT_HERO_MULT_NEWBIE = 0.5f;
	public static final float HEALTH_HIT_HERO_MULT_EASY = 0.75f;
	public static final float HEALTH_HIT_HERO_MULT_HARD = 1.25f;
	public static final float HEALTH_HIT_HERO_MULT_ULTIMATE = 1.5f;
	public static final int HEALTH_HIT_HAND = 1;
	public static final int HEALTH_HIT_PISTOL = 2;
	public static final int HEALTH_HIT_SHOTGUN = 8;
	public static final int HEALTH_HIT_CHAINGUN = 2;
	public static final int HEALTH_HIT_DBLSHOTGUN = 16;
	public static final int HEALTH_HIT_DBLCHAINGUN = 4;
	public static final int HEALTH_HIT_CHAINSAW = 1;
	public static final int HEALTH_HIT_RLAUNCHER = 40;
	public static final int HEALTH_HIT_DBLPISTOL = 4;
	public static final int HEALTH_HIT_PDBLSHOTGUN = 24;
	public static final int HEALTH_HIT_BARREL = HEALTH_HIT_RLAUNCHER;
	public static final int HEALTH_BARREL = 32 * 4;

	public static final int ARMOR_MAX = 200;
	public static final int ARMOR_ADDITIONAL = 100;
	public static final int ARMOR_ADD_GREEN = 100;
	public static final int ARMOR_ADD_RED = 200;

	public static final int AMMO_MAX_PISTOL = 150;
	public static final int AMMO_MAX_SHOTGUN = 75;
	public static final int AMMO_MAX_ROCKET = 50;
	public static final int AMMO_ADDITIONAL_PISTOL = 75;
	public static final int AMMO_ADDITIONAL_SHOTGUN = 35;
	public static final int AMMO_ADDITIONAL_ROCKET = 25;
	public static final int AMMO_ENSURED_PISTOL = 50;
	public static final int AMMO_ENSURED_SHOTGUN = 25;
	public static final int AMMO_ENSURED_ROCKET = 5;
	public static final int AMMO_ADD_CLIP = 5;
	public static final int AMMO_ADD_AMMO = 20;
	public static final int AMMO_ADD_SHELL = 5;
	public static final int AMMO_ADD_SBOX = 15;
	public static final int AMMO_ADD_ROCKET = 1;
	public static final int AMMO_ADD_RBOX = 5;
	public static final int AMMO_ADD_SHOTGUN = 3;
	public static final int AMMO_ADD_CHAINGUN = 20;
	public static final int AMMO_ADD_DBLSHOTGUN = 6;
	public static final int AMMO_ADD_DBLCHAINGUN = 30;
	public static final int AMMO_ADD_RLAUNCHER = 5;

	public static final int EXP_OPEN_DOOR = 1 * 5;
	public static final int EXP_PICK_OBJECT = 5 * 5;
	public static final int EXP_KILL_MONSTER = 25 * 5;
	public static final int EXP_SECRET_FOUND = 50 * 5;
	public static final int EXP_END_LEVEL = 100 * 5;
}
