window.Hero = {
	WALK_WALL_DIST: 0.1,
	ANGLE_ADD: 2,
	ANGLE_MAX_ACC: 15,
	WALK_MAX_ACC: 12,
	WALK_ADD: 5,

	x: 0,
	y: 0,
	weapon: 0,
	hasWeapon: [],
	ammo: [],
	keysMask: 0,
	health: 100,
	armor: 0,

	angleAcc: 0,
	walkAcc: 0,
	prevMovedTime: 0,
	hasMoved: false,
	killedTime: 0,
	killedAngle: 0,
	killedHeroAngle: 0,
	nextLevelTime: 0,

	foundSecretsMask: 0,
	foundSecrets: 0,
	killedMonsters: 0,
	pickedItems: 0,

	init: function() {
		Hero.a = (270 * Engine.ANG_360 / 360) | 0;

		for (var i = 0; i < Weapons.WEAPON_LAST; i++) {
			Hero.hasWeapon[i] = false;
		}

		for (var i = 0; i < Weapons.AMMO_LAST; i++) {
			Hero.ammo[i] = 0;
		}

		Hero.weapon = Weapons.WEAPON_HAND;
		Hero.hasWeapon[Weapons.WEAPON_HAND] = true;
		Hero.reInitPistol();
	},

	reInitPistol: function() {
		Hero.hasWeapon[Weapons.WEAPON_PISTOL] = true;
		Hero.ammo[Weapons.AMMO_PISTOL] = Weapons.ENSURED_PISTOL_AMMO;
	},

	isPassable: function(elapsedTime) {
		var res = Level.isPassable(Hero.x, Hero.y, Hero.WALK_WALL_DIST, Level.PASSABLE_MASK_HERO);

		if (!res) {
			var door = Level.doorsMap[Hero.y | 0][Hero.x | 0];
			var mark = Level.marksMap[Hero.y | 0][Hero.x | 0];

			if (door) {
				if (door.sticked) {
					if (door.requiredKey == 0) {
						Overlay.showLabel(Overlay.LABEL_CANT_OPEN, elapsedTime);

						if (door.mark && door.mark.id) {
							Hero.processOneMark(100 + door.mark.id, elapsedTime);
						}
					} else if (!(Hero.keysMask & door.requiredKey)) {
						if (door.requiredKey == 4 ) {
							Overlay.showLabel(Overlay.LABEL_NEED_GREEN_KEY, elapsedTime);
						} else if (door.requiredKey == 2) {
							Overlay.showLabel(Overlay.LABEL_NEED_RED_KEY, elapsedTime);
						} else {
							Overlay.showLabel(Overlay.LABEL_NEED_BLUE_KEY, elapsedTime);
						}

						if (door.mark && door.mark.id) {
							Hero.processOneMark(100 + door.mark.id, elapsedTime);
						}
					} else {
						door.sticked = false;
					}
				}

				if (door.open()) {
					Level.passableMap[door.y][door.x] |= Level.PASSABLE_IS_DOOR_OPENED_BY_HERO;

					if (door.mark != null) {
						Hero.processOneMark(door.mark.id, elapsedTime);
					}
				}
			} else if (mark) {
				Hero.processOneMark(mark.id, elapsedTime);
			}
		}

		return res;
	},

	pickObjects: function(elapsedTime) {
		if ((Level.passableMap[Hero.y | 0][Hero.x | 0] & Level.PASSABLE_IS_OBJECT) == 0) {
			return;
		}

		var obj = Level.objectsMap[Hero.y | 0][Hero.x | 0];

		if (obj == null && obj.decor) {
			return;
		}

		switch (obj.tex) {
			case Tex.OBJ_ARMOR_GREEN:
			case Tex.OBJ_ARMOR_RED:
				if (Hero.armor >= 200) {
					return;
				}
				break;

			case Tex.OBJ_STIM:
			case Tex.OBJ_MEDI:
				if (Hero.health >= 100) {
					return;
				}
				break;

			case Tex.OBJ_CLIP:
			case Tex.OBJ_AMMO:
				if (Hero.ammo[Weapons.AMMO_PISTOL] >= Weapons.MAX_PISTOL_AMMO) {
					return;
				}
				break;

			case Tex.OBJ_SHELL:
			case Tex.OBJ_SBOX:
				if (Hero.ammo[Weapons.AMMO_SHOTGUN] >= Weapons.MAX_SHOTGUN_AMMO) {
					return;
				}
				break;

			case Tex.OBJ_BPACK:
				if (
					Hero.health >= 100
					&& Hero.ammo[Weapons.AMMO_PISTOL] >= Weapons.MAX_PISTOL_AMMO
					&& Hero.ammo[Weapons.AMMO_SHOTGUN] >= Weapons.MAX_SHOTGUN_AMMO
				) {
					return;
				}
				break;

			case Tex.OBJ_SHOTGUN:
				if (Hero.hasWeapon[Weapons.WEAPON_SHOTGUN] && Hero.ammo[Weapons.AMMO_SHOTGUN] >= Weapons.MAX_SHOTGUN_AMMO) {
					return;
				}
				break;

			case Tex.OBJ_CHAINGUN:
				if (Hero.hasWeapon[Weapons.WEAPON_CHAINGUN] && Hero.ammo[Weapons.AMMO_PISTOL] >= Weapons.MAX_PISTOL_AMMO) {
					return;
				}
				break;

			case Tex.OBJ_DBLSHOTGUN:
				if (Hero.hasWeapon[Weapons.WEAPON_DBLSHOTGUN] && Hero.ammo[Weapons.AMMO_SHOTGUN] >= Weapons.MAX_SHOTGUN_AMMO) {
					return;
				}
				break;
		}

		var bestWeapon = Weapons.getBestWeapon();

		switch (obj.tex) {
			case Tex.OBJ_ARMOR_GREEN:
				Hero.armor = Math.min(Hero.armor + 100, 200);
				break;

			case Tex.OBJ_ARMOR_RED:
				Hero.armor = Math.min(Hero.armor + 200, 200);
				break;

			case Tex.OBJ_KEY_BLUE:
				Hero.keysMask |= 1;
				break;

			case Tex.OBJ_KEY_RED:
				Hero.keysMask |= 2;
				break;

			case Tex.OBJ_KEY_GREEN:
				Hero.keysMask |= 4;
				break;

			case Tex.OBJ_STIM:
				Hero.health = Math.min(Hero.health + 10, 100);
				break;

			case Tex.OBJ_MEDI:
				Hero.health = Math.min(Hero.health + 50, 100);
				break;

			case Tex.OBJ_CLIP:
				Hero.ammo[Weapons.AMMO_PISTOL] = Math.min(Hero.ammo[Weapons.AMMO_PISTOL] + 5, Weapons.MAX_PISTOL_AMMO);

				if (bestWeapon < Weapons.WEAPON_PISTOL) {
					Weapons.selectBestWeapon(elapsedTime);
				}
				break;

			case Tex.OBJ_AMMO:
				Hero.ammo[Weapons.AMMO_PISTOL] = Math.min(Hero.ammo[Weapons.AMMO_PISTOL] + 20, Weapons.MAX_PISTOL_AMMO);

				if (bestWeapon < Weapons.WEAPON_PISTOL) {
					Weapons.selectBestWeapon(elapsedTime);
				}
				break;

			case Tex.OBJ_SHELL:
				Hero.ammo[Weapons.AMMO_SHOTGUN] = Math.min(Hero.ammo[Weapons.AMMO_SHOTGUN] + 5, Weapons.MAX_SHOTGUN_AMMO);

				if (bestWeapon < Weapons.WEAPON_SHOTGUN && Hero.hasWeapon[Weapons.WEAPON_SHOTGUN]) {
					Weapons.selectBestWeapon(elapsedTime);
				}
				break;

			case Tex.OBJ_SBOX:
				Hero.ammo[Weapons.AMMO_SHOTGUN] = Math.min(Hero.ammo[Weapons.AMMO_SHOTGUN] + 15, Weapons.MAX_SHOTGUN_AMMO);

				if (bestWeapon < Weapons.WEAPON_SHOTGUN && Hero.hasWeapon[Weapons.WEAPON_SHOTGUN]) {
					Weapons.selectBestWeapon(elapsedTime);
				}
				break;

			case Tex.OBJ_BPACK:
				Hero.health = Math.min(Hero.health + 10, 100);
				Hero.ammo[Weapons.AMMO_PISTOL] = Math.min(Hero.ammo[Weapons.AMMO_PISTOL] + 5, Weapons.MAX_PISTOL_AMMO);
				Hero.ammo[Weapons.AMMO_SHOTGUN] = Math.min(Hero.ammo[Weapons.AMMO_SHOTGUN] + 5, Weapons.MAX_SHOTGUN_AMMO);

				if (bestWeapon < Weapons.WEAPON_SHOTGUN) {
					Weapons.selectBestWeapon(elapsedTime);
				}
				break;

			case Tex.OBJ_SHOTGUN:
				Hero.hasWeapon[Weapons.WEAPON_SHOTGUN] = true;
				Hero.ammo[Weapons.AMMO_SHOTGUN] = Math.min(Hero.ammo[Weapons.AMMO_SHOTGUN] + 3, Weapons.MAX_SHOTGUN_AMMO);

				if (bestWeapon < Weapons.WEAPON_SHOTGUN) {
					Weapons.selectBestWeapon(elapsedTime);
				}
				break;

			case Tex.OBJ_CHAINGUN:
				Hero.hasWeapon[Weapons.WEAPON_CHAINGUN] = true;
				Hero.ammo[Weapons.AMMO_PISTOL] = Math.min(Hero.ammo[Weapons.AMMO_PISTOL] + 20, Weapons.MAX_PISTOL_AMMO);

				if (bestWeapon < Weapons.WEAPON_CHAINGUN) {
					Weapons.selectBestWeapon(elapsedTime);
				}
				break;

			// case Tex.OBJ_DBLSHOTGUN:
			//	Hero.hasWeapon[Weapons.WEAPON_DBLSHOTGUN] = true;
			//	Hero.ammo[Weapons.AMMO_SHOTGUN] = Math.min(Hero.ammo[Weapons.AMMO_SHOTGUN] + 6, Weapons.MAX_SHOTGUN_AMMO);
			//
			//	if (bestWeapon < Weapons.WEAPON_DBLSHOTGUN) {
			//		Weapons.selectBestWeapon();
			//	}
			//	break;
		}

		if (Level.passableMap[Hero.y | 0][Hero.x | 0] & Level.PASSABLE_IS_OBJECT_ORIG) {
			Hero.pickedItems++;
		}

		Level.objectsMap[Hero.y | 0][Hero.x | 0] = null;
		Level.passableMap[Hero.y | 0][Hero.x | 0] &= ~Level.PASSABLE_MASK_OBJECT;

		Overlay.showOverlay(Overlay.ITEM, elapsedTime);
	},

	tick: function(elapsedTime) {
		Level.clearPassable(Hero.x, Hero.y, Hero.WALK_WALL_DIST, Level.PASSABLE_IS_HERO);
		Hero.a = Math.round(Hero.a + Hero.angleAcc + Engine.ANG_360) % Engine.ANG_360;

		var prevX = Hero.x;
		var prevY = Hero.y;

		if (Hero.walkAcc != 0) {
			var currAcc = Hero.walkAcc;

			while (Math.abs(currAcc) > 1) {
				Hero.x += currAcc * Tab.cos[Hero.a] * 0.01;

				if (!Hero.isPassable(elapsedTime)) {
					Hero.x = prevX;
					currAcc += (currAcc > 0 ? -1 : 1);
					continue;
				}

				break;
			}

			currAcc = Hero.walkAcc;

			while (Math.abs(currAcc) > 1) {
				Hero.y += currAcc * Tab.sin[Hero.a] * 0.01;

				if (!Hero.isPassable(elapsedTime)) {
					Hero.y = prevY;
					currAcc += (currAcc > 0 ? -1 : 1);
					continue;
				}

				break;
			}
		}

		Hero.hasMoved = (Hero.x != prevX || Hero.y != prevY);
		Level.setPassable(Hero.x, Hero.y, Hero.WALK_WALL_DIST, Level.PASSABLE_IS_HERO);

		if (Hero.hasMoved) {
			Hero.processMarks(elapsedTime);
			Hero.pickObjects(elapsedTime);
		}

		var noChanges = true;

		if (Hero.killedTime <= 0 && Hero.nextLevelTime <= 0) {
			if (Controls.state[Controls.LEFT]) {
				Hero.angleAcc += Hero.ANGLE_ADD;
				if (Hero.angleAcc > Hero.ANGLE_MAX_ACC) { Hero.angleAcc = Hero.ANGLE_MAX_ACC; }
				noChanges = false;
			}

			if (Controls.state[Controls.RIGHT]) {
				Hero.angleAcc -= Hero.ANGLE_ADD;
				if (Hero.angleAcc < -Hero.ANGLE_MAX_ACC) { Hero.angleAcc = -Hero.ANGLE_MAX_ACC; }
				noChanges = false;
			}
		}

		if (noChanges) {
			Hero.angleAcc = (Hero.angleAcc / 2) | 0;
			if (Math.abs(Hero.angleAcc) < 2) { Hero.angleAcc = 0; }
		}

		noChanges = true;

		if (Hero.killedTime <= 0 && Hero.nextLevelTime <= 0) {
			if (Controls.state[Controls.FORWARD]) {
				Hero.walkAcc += Hero.WALK_ADD;
				if (Hero.walkAcc > Hero.WALK_MAX_ACC) { Hero.walkAcc = Hero.WALK_MAX_ACC; }
				noChanges = false;
			}

			if (Controls.state[Controls.BACKWARD]) {
				Hero.walkAcc -= Hero.WALK_ADD;
				if (Hero.walkAcc < -Hero.WALK_MAX_ACC) { Hero.walkAcc = -Hero.WALK_MAX_ACC; }
				noChanges = false;
			}
		}

		if (noChanges) {
			Hero.walkAcc = (Hero.walkAcc / 2) | 0;
			if (Math.abs(Hero.walkAcc) < 2) { Hero.walkAcc = 0; }
		}
	},

	// monster must be from visibleObjects
	checkMonsterVisibilityAndHit: function(mon, hits) {
		var shootXMain = Hero.x + Tab.cos[Hero.a] * mon.dist;
		var shootYMain = Hero.y + Tab.sin[Hero.a] * mon.dist;

		var xoff = Tab.sin[Hero.a] * 0.2;
		var yoff = - Tab.cos[Hero.a] * 0.2;

		if (
			Level.traceLine(Hero.x + xoff, Hero.y + yoff, shootXMain + xoff, shootYMain + yoff, Level.PASSABLE_MASK_SHOOT_W) ||
			Level.traceLine(Hero.x, Hero.y, shootXMain, shootYMain, Level.PASSABLE_MASK_SHOOT_W) ||
			Level.traceLine(Hero.x - xoff, Hero.y - yoff, shootXMain - xoff, shootYMain - yoff, Level.PASSABLE_MASK_SHOOT_W)
		) {
			mon.hit(Engine.getRealHits(hits, mon.dist), Weapons.WEAPONS[Hero.weapon].hitTimeout);
			return true;
		}

		return false;
	},

	processShoot: function(elapsedTime) {
		// just for case
		if (Weapons.hasNoAmmo(Hero.weapon)) {
			Weapons.selectBestWeapon(elapsedTime);
		}

		var mon = Engine.sortVisibleObjects();

		if (mon && (!Weapons.currentParams.isNear || (mon.dist <= 1.4))) {
			Hero.checkMonsterVisibilityAndHit(mon, Weapons.currentParams.hits);
		}

		if (Weapons.currentParams.ammoIdx >= 0) {
			Hero.ammo[Weapons.currentParams.ammoIdx] -= Weapons.currentParams.needAmmo;

			if (Hero.ammo[Weapons.currentParams.ammoIdx] < Weapons.currentParams.needAmmo) {
				if (Hero.ammo[Weapons.currentParams.ammoIdx] < 0) {
					Hero.ammo[Weapons.currentParams.ammoIdx] = 0;
				}

				Weapons.selectBestWeapon(elapsedTime);
			}
		}
	},

	hit: function(amt, mon, elapsedTime) {
		if (Hero.killedTime > 0 || Hero.nextLevelTime > 0) {
			return;
		}

		Overlay.showOverlay(Overlay.BLOOD, elapsedTime);

		if (Hero.armor > 0) {
			Hero.armor = Math.max(0, Hero.armor - Math.max(1, (amt * 3 / 4) | 0));
			Hero.health -= Math.max(1, (amt / 4) | 0);
		} else {
			Hero.health -= amt;
		}

		if (Hero.health <= 0) {
			Hero.health = 0;
			Hero.killedTime = elapsedTime;

			var dx = mon.x + 0.5 - Hero.x;
			var dy = mon.y + 0.5 - Hero.y;

			Hero.killedAngle = ((Math.atan2(dy, dx) * Engine.ANG_180 / Math.PI + Engine.ANG_360) | 0) % Engine.ANG_360;

			Hero.killedHeroAngle = (
				(Math.abs(Engine.ANG_360 + Hero.a - Hero.killedAngle) < Math.abs(Hero.a - Hero.killedAngle)) ?
				(Engine.ANG_360 + Hero.a) :
				Hero.a
			);
		}
	},

	processMarks: function(elapsedTime) {
		if (
			(Level.marksMap[Hero.y | 0][Hero.x | 0] != null) &&
			(Level.doorsMap[Hero.y | 0][Hero.x | 0] == null)
		) {
			Hero.processOneMark(Level.marksMap[Hero.y | 0][Hero.x | 0].id, elapsedTime);
		}
	},

	processOneMark: function(markId, elapsedTime) {
		if (Engine.executeActions(markId, elapsedTime)) {
			if (Hero.nextLevelTime == 0) {
				Overlay.showOverlay(Overlay.MARK);
			}
		}
	}
};
