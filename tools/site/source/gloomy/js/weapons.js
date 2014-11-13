window.Weapons = {
	ENSURED_PISTOL_AMMO: 50,
	ENSURED_SHOTGUN_AMMO: 25,
	MAX_PISTOL_AMMO: 150,
	MAX_SHOTGUN_AMMO: 75,

	AMMO_PISTOL: 0,
	AMMO_SHOTGUN: 1,
	AMMO_LAST: 2,

	WEAPON_HAND: 0, // required to be 0
	WEAPON_PISTOL: 1,
	WEAPON_SHOTGUN: 2,
	WEAPON_CHAINGUN: 3,
	WEAPON_LAST: 4,

	currentParams: null,
	shootCycle: 0,
	changeWeaponDir: 0,
	changeWeaponNext: 0,
	changeWeaponTime: 0,

	init: function() {
		Weapons.WEAPONS = [
			// WEAPON_HAND
			{
				cycle: [ 0, 1, 1, 1, 2, 2, 2, -3, 3, 3, 2, 2, 2, 1, 1, 0, 0 ],
				ammoIdx: -1,
				needAmmo: 0,
				hits: 1,
				hitTimeout: 5,
				tmapBase: Tex.HAND,
				xmult: 1,
				xoff: 0,
				hgt: 1.5,
				isNear: true
			},
			// WEAPON_PISTOL
			{
				cycle: [ 0, 1, 1, 1, 1, 2, 2, 2, 2, -3, 3, 3, 3, 4, 4, 4, 4, 0, 0, 0, 0, 0 ],
				ammoIdx: Weapons.AMMO_PISTOL,
				needAmmo: 1,
				hits: 2,
				hitTimeout: 5,
				tmapBase: Tex.PIST,
				xmult: 1,
				xoff: 0.03,
				hgt: 1.5,
				isNear: false
			},
			// WEAPON_SHOTGUN
			{
				cycle: [
					0, 0, 0, 0, 0,
					1, 1, 1, 1, 1,
					-2, 2, 2, 2, 2,
					3, 3, 3, 3, 3,
					3, 3, 3, 3, 3,
					4, 4, 4, 4, 4,
					4, 4, 4, 4, 4,
					0, 0, 0, 0, 0
				],
				ammoIdx: Weapons.AMMO_SHOTGUN,
				needAmmo: 1,
				hits: 8,
				hitTimeout: 10,
				tmapBase: Tex.SHTG,
				xmult: 0.9,
				xoff: 0.1,
				hgt: 1.35,
				isNear: false
			},
			// WEAPON_CHAINGUN
			{
				cycle: [
					0,
					1, 1, 1,
					-2, 2, 2,
					-3, 3, 3,
					0, 0
				],
				ammoIdx: Weapons.AMMO_PISTOL,
				needAmmo: 1,
				hits: 2,
				hitTimeout: 5,
				tmapBase: Tex.CHGN,
				xmult: 0.8,
				xoff: 0.115,
				hgt: 1.2,
				isNear: false
			}
		];

		Weapons.updateWeapon();
	},

	updateWeapon: function() {
		Weapons.currentParams = Weapons.WEAPONS[Hero.weapon];
		Weapons.currentCycle = Weapons.currentParams.cycle;
		Weapons.shootCycle = 0;
	},

	hasNoAmmo: function(type) {
		return ((Weapons.WEAPONS[type].ammoIdx >= 0) && (Hero.ammo[Weapons.WEAPONS[type].ammoIdx] < Weapons.WEAPONS[type].needAmmo));
	},

	switchWeapon: function(type, elapsedTime) {
		if (Hero.hasWeapon[type] && !Weapons.hasNoAmmo(type)) {
			Weapons.changeWeaponNext = type;
			Weapons.changeWeaponTime = elapsedTime;
			Weapons.changeWeaponDir = -1;
		}
	},

	getBestWeapon: function() {
		var resWeapon = Weapons.WEAPON_LAST - 1;

		while ((resWeapon > 0) && (!Hero.hasWeapon[resWeapon] || Weapons.hasNoAmmo(resWeapon) || Weapons.WEAPONS[resWeapon].isNear)) {
			resWeapon--;
		}

		if (resWeapon == 0) {
			resWeapon = Weapons.WEAPON_LAST - 1;

			while ((resWeapon > 0) && (!Hero.hasWeapon[resWeapon] || Weapons.hasNoAmmo(resWeapon) || !Weapons.WEAPONS[resWeapon].isNear)) {
				resWeapon--;
			}
		}

		return resWeapon;
	},

	selectBestWeapon: function(elapsedTime) {
		var resWeapon = Weapons.getBestWeapon();

		if (resWeapon != Hero.weapon) {
			Weapons.switchWeapon(resWeapon, elapsedTime);
		}
	},

	tick: function(elapsedTime) {
		if ((Weapons.currentCycle[Weapons.shootCycle] < 0) && (Weapons.changeWeaponDir == 0)) {
			Hero.processShoot(elapsedTime);
		}

		if (Weapons.shootCycle > 0) {
			Weapons.shootCycle = (Weapons.shootCycle + 1) % Weapons.currentCycle.length;
		}
	},

	update: function(elapsedTime) {
		if (Hero.killedTime > 0 || Hero.nextLevelTime > 0) {
			return;
		}

		if (Controls.state[Controls.ACTION] && Weapons.shootCycle == 0 && Weapons.changeWeaponDir == 0) {
			Weapons.shootCycle++;
		}

		var switchToWeapon = -1;
		var keyUp = -1;

		if (Controls.state[Controls.SW_HAND]) {
			switchToWeapon = Weapons.WEAPON_HAND;
			keyUp = Controls.SW_HAND;
		} else if (Controls.state[Controls.SW_PIST]) {
			switchToWeapon = Weapons.WEAPON_PISTOL;
			keyUp = Controls.SW_PIST;
		} else if (Controls.state[Controls.SW_SHTG]) {
			switchToWeapon = Weapons.WEAPON_SHOTGUN;
			keyUp = Controls.SW_SHTG;
		} else if (Controls.state[Controls.SW_CHGN]) {
			switchToWeapon = Weapons.WEAPON_CHAINGUN;
			keyUp = Controls.SW_CHGN;
		}

		if (switchToWeapon >= 0 && Weapons.shootCycle == 0 && Weapons.changeWeaponDir == 0) {
			Weapons.switchWeapon(switchToWeapon, elapsedTime);
			Controls.keyUp(keyUp);
		}
	},

	render: function(elapsedTime, walkTime) {
		var yoff = 0;

		if (Weapons.changeWeaponDir < 0) {
			yoff = (elapsedTime - Weapons.changeWeaponTime) / 150.0;

			if (yoff >= Weapons.currentParams.hgt + 0.1) {
				Hero.weapon = Weapons.changeWeaponNext;
				Weapons.updateWeapon();

				Weapons.changeWeaponDir = 1;
				Weapons.changeWeaponTime = elapsedTime;
			}
		} else if (Weapons.changeWeaponDir > 0) {
			yoff = Weapons.currentParams.hgt + 0.1 - (elapsedTime - Weapons.changeWeaponTime) / 150.0;

			if (yoff <= 0) {
				yoff = 0;
				Weapons.changeWeaponDir = 0;
			}
		}

		yoff += Math.abs(Math.sin(walkTime / 150.0 + Math.PI / 2.0)) * 0.1 + 0.05;

		var xoff = Math.sin(walkTime / 150.0) * (Engine.VIEWPORT_WIDTH / 16.0) + (Engine.VIEWPORT_WIDTH / 32.0);
		var xpos = (Engine.VIEWPORT_WIDTH * Weapons.currentParams.xoff + xoff) | 0;
		var wdt = (Engine.VIEWPORT_WIDTH * Weapons.currentParams.xmult) | 0;

		var hgt = (Weapons.currentParams.hgt * Engine.VIEWPORT_HEIGHT * 0.5) | 0;
		var ypos = ((Engine.VIEWPORT_HEIGHT - hgt) + Engine.VIEWPORT_HEIGHT * 0.5 * yoff) | 0;

		// just for case
		if (Weapons.shootCycle > Weapons.currentCycle.length) {
		    Weapons.shootCycle = 0;
		}

		var weaponTexture = Weapons.currentCycle[Weapons.shootCycle];

		if (weaponTexture < -1000) {
			weaponTexture = -1000 - weaponTexture;
		} else if (weaponTexture < 0) {
			weaponTexture = -weaponTexture;
		}

		var tmap = Tex.MAPS[Weapons.currentParams.tmapBase + weaponTexture];

		App.offscreenContext.drawImage(
			tmap.img,
			0, 0, tmap.wdt, tmap.hgt,
			xpos, ypos, wdt, hgt
		);
	}
};
