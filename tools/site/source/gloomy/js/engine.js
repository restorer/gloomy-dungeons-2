window.Engine = {
	VIEWPORT_WIDTH: (500 / 2) | 0,
	VIEWPORT_HEIGHT: (350) | 0,

	ANG_360: 1500,
	INFINITY: 0.0000000000001,

	drawCols: [],
	drawObjects: [],
	touchedMap: [],
	visibleObjects: [],

	init: function() {
		Engine.ANG_30 = (Engine.ANG_360 / 72) | 0;
		Engine.ANG_30 = (Engine.ANG_360 / 12) | 0;
		Engine.ANG_45 = (Engine.ANG_360 / 8) | 0;
		Engine.ANG_60 = (Engine.ANG_360 / 6) | 0;
		Engine.ANG_90 = (Engine.ANG_360 / 4) | 0;
		Engine.ANG_180 = (Engine.ANG_360 / 2) | 0;
		Engine.ANG_405 = Engine.ANG_360 + Engine.ANG_45;
		Engine.ANG_720 = Engine.ANG_360 * 2;

		Engine.MAX_DIST = Level.WIDTH * Level.HEIGHT;
		Engine.RENDER_HEIGHT = Engine.VIEWPORT_HEIGHT * 1.2;
		Engine.HGT2WDT_MULT = Engine.VIEWPORT_WIDTH / Engine.RENDER_HEIGHT;

		for (var i = 0; i < Engine.VIEWPORT_WIDTH; i++) {
			Engine.drawCols.push({
				hgt: 0,
				dist: Engine.MAX_DIST
			});
		}
	},

	renderColumn: function(col, x, y, a, corr) {
		var sn = Tab.sin[a];
		var cs = Tab.cos[a];
		var twdt = Tex.MAPS[1].wdt;
		var door = Level.doorsMap[y | 0][x | 0];

		var ver = { act: false };
		var hor = { act: false };

		if (cs != 0) {
			var dy = Tab.sinDivAbsCos[a];
			var ox = (cs < 0 ? (Math.floor(x) - x - Engine.INFINITY) : (1.0 + Math.floor(x) - x + Engine.INFINITY));

			ver = {
				act: true,
				dx: (cs > 0 ? 1 : -1),
				dy: dy,
				x: x + ox,
				y: y + Math.abs(ox) * dy,
				dd: Math.abs(1 / cs),
				dist: Math.abs(ox / cs)
			};
		}

		if (sn != 0) {
			var dx = Tab.cosDivAbsSin[a];
			var oy = (sn < 0 ? (Math.floor(y) - y - Engine.INFINITY) : (1.0 + Math.floor(y) - y + Engine.INFINITY));

			hor = {
				act: true,
				dx: dx,
				dy: (sn > 0 ? 1 : -1),
				x: x + Math.abs(oy) * dx,
				y: y + oy,
				dd: Math.abs(1 / sn),
				dist: Math.abs(oy / sn)
			};
		}

		var cur, off, ry, rx, tex, frac, mult, obj, idx;
		var zpos = 0.5;

		for (;;) {
			if (ver.act && (!hor.act || ver.dist <= hor.dist)) {
				cur = ver;
				off = ver.y;
			} else if (hor.act && (!ver.act || hor.dist <= ver.dist)) {
				cur = hor;
				off = hor.x;
			} else {
				col.hgt = 0;
				col.dist = Engine.MAX_DIST;
				return;
			}

			rx = cur.x | 0;
			ry = cur.y | 0;

			if (rx < 0 || ry < 0 || rx >= Level.WIDTH || ry >= Level.HEIGHT) {
				cur.act = false;
			} else {
				tex = Level.wallsMap[ry][rx];

				if (tex > 0) {
					tex = (door ? door.stex : tex) + (cur == ver ? 80 : 0);
					break;
				} else if (tex < 0) {
					Engine.touchedMap[ry * Level.WIDTH + rx] = true;
					door = Level.doorsMap[ry][rx];

					if (cur == ver) {
						frac = cur.y % 1;

						if (door.ver) {
							mult = zpos;
							off = frac + cur.dy * zpos;
						} else {
							mult = cur.dy * (zpos - frac);
							off = (cur.dx > 0 ? mult : 1.0 - mult);
						}
					} else {
						frac = cur.x % 1;

						if (door.ver) {
							mult = cur.dx * (zpos - frac);
							off = (cur.dy > 0 ? mult : 1.0 - mult);
						} else {
							mult = zpos;
							off = frac + cur.dx * zpos;
						}
					}

					if (off >= 0 && off < 1) {
						off -= door.pos;

						if (off >= 0 && off < 1) {
							tex = door.ftex;
							cur.dist += cur.dd * mult;
							break;
						}
					}
				} else {
					door = null;
					idx = ry * Level.WIDTH + rx;

					if (!Engine.touchedMap[idx]) {
						Engine.touchedMap[idx] = true;

						if (obj = Level.objectsMap[ry][rx]) {
							Engine.drawObjects.push(obj);
						}
					}
				}

				cur.x += cur.dx;
				cur.y += cur.dy;
				cur.dist += cur.dd;
			}
		}

		col.tex = tex;
		col.off = ((off * twdt) | 0) % twdt;
		col.hgt = (Engine.RENDER_HEIGHT / (cur.dist * corr)) | 0;
		col.dist = cur.dist;
	},

	prepareObject: function(obj) {
		var tx = obj.x + 0.5 - Hero.x;
		var ty = obj.y + 0.5 - Hero.y;

		var ang = Engine.ANG_180 - ((
			(Math.atan2(ty, tx) * Engine.ANG_180 / Math.PI - Hero.a + Engine.ANG_720) | 0
		) % Engine.ANG_360 + Engine.ANG_180) % Engine.ANG_360;

		obj.sx = ((ang / Engine.ANG_30 * Engine.VIEWPORT_WIDTH + Engine.VIEWPORT_WIDTH) / 2) | 0;
		obj.dist = Math.sqrt(tx * tx + ty * ty);
		obj.hgt = (Engine.RENDER_HEIGHT / (obj.dist * Tab.corr[ Math.max(0, Math.min(Engine.VIEWPORT_WIDTH - 1, obj.sx)) ])) | 0;
		obj.wdt = (obj.hgt * Engine.HGT2WDT_MULT) | 0;
	},

	sortByDistAsc: function(a, b) {
		return a.dist- b.dist;
	},

	sortByDistDesc: function(a, b) {
		return b.dist * 10 + b.renderWeight - a.dist * 10 - a.renderWeight;
	},

	prepare: function(elapsedTime) {
		Engine.drawObjects = [];
		Engine.touchedMap = [];

		for (var i = 0; i < Engine.VIEWPORT_WIDTH; i++) {
			Engine.renderColumn(
				Engine.drawCols[i],
				Hero.x, Hero.y,
				(Tab.colAng[i] + Hero.a) % Engine.ANG_360,
				Tab.corr[i]
			);
		}

		for (var i = 0; i < Engine.drawObjects.length; i++) {
			Engine.prepareObject(Engine.drawObjects[i]);
		}

		var mid = Engine.VIEWPORT_WIDTH / 2;

		for (var i = 0; i < Level.monsters.length; i++) {
			var mon = Level.monsters[i];

			if (Engine.touchedMap[mon.cellY * Level.WIDTH + mon.cellX]) {
				Engine.prepareObject(mon);

				if (mon.health > 0) {
					if ((mon.hitTimeout <= 0) && (mon.attackTimeout > 0)) {
						mon.tex = 15;
					} else if (mon.isAimedOnHero) {
						mon.tex = 2;
					} else {
						mon.tex = ((Hero.a + Engine.ANG_405 - mon.dir * Engine.ANG_90) % Engine.ANG_360 / Engine.ANG_90) | 0;
					}

					if (mon.hitTimeout > 0) {
						mon.tex += 8;
					} else if (!mon.isInAttackState && ((elapsedTime % 800) > 400)) {
						mon.tex += 4;
					}

					if ((mon.sx - mon.wdt / 2) <= mid && (mon.sx + mon.wdt / 2) >= mid) {
						Engine.visibleObjects.push(mon);
					}
				} else {
					if (mon.dieTime == 0) {
						mon.dieTime = elapsedTime;
					}

					mon.tex = 12 + (mon.dieTime < 0 ? 2 : Math.min(2, ((elapsedTime - mon.dieTime) / 150) | 0));
				}

				Engine.drawObjects.push(mon);
			}
		}

		Engine.drawObjects.sort(Engine.sortByDistDesc);
	},

	render: function(elapsedTime, yoff) {
		Engine.visibleObjects = [];
		Engine.prepare(elapsedTime);

		var offscreenContext = App.offscreenContext;
		var tmaps = Tex.MAPS;
		var tmap = tmaps[0];

		offscreenContext.drawImage(
			tmap.img,
			0, 0, tmap.wdt, tmap.hgt,
			0, 0, Engine.VIEWPORT_WIDTH, Engine.VIEWPORT_HEIGHT
		);

		offscreenContext.drawImage(
			tmap.img,
			0, 0, tmap.wdt, tmap.hgt,
			0, yoff, Engine.VIEWPORT_WIDTH, Engine.VIEWPORT_HEIGHT
		);

		var tmap = tmaps[1];

		for (var i = 0; i < Engine.VIEWPORT_WIDTH; i++) {
			var col = Engine.drawCols[i];

			if (col.hgt > 0) {
				offscreenContext.drawImage(
					tmap.img,
					(col.tex % tmap.cols) * tmap.wdt + col.off, ((col.tex / tmap.cols) | 0) * tmap.hgt, 1, tmap.hgt,
					i, yoff + ((Engine.VIEWPORT_HEIGHT - col.hgt) / 2) | 0, 1, col.hgt
				);
			}
		}

		for (var i = 0; i < Engine.drawObjects.length; i++) {
			var obj = Engine.drawObjects[i];

			if (obj.hgt > 0 && obj.wdt > 0) {
				var tmap = tmaps[obj.tmap];
				var sx = (obj.sx - obj.wdt / 2) | 0;
				var tx = (obj.tex % tmap.cols) * tmap.wdt;
				var tdx = tmap.wdt / obj.wdt;
				var cnt = obj.wdt;
				var ty = ((obj.tex / tmap.cols) | 0) * tmap.hgt;
				var sy = ((Engine.VIEWPORT_HEIGHT - obj.hgt) / 2) | 0;

				if (sx < 0) {
					cnt += sx;
					tx -= tdx * sx;
					sx = 0;
				}

				while (cnt > 0 && sx < Engine.VIEWPORT_WIDTH) {
					if (Engine.drawCols[sx].dist > obj.dist) {
						offscreenContext.drawImage(
							tmap.img,
							tx | 0, ty, 1, tmap.hgt,
							sx, yoff + sy, 1, obj.hgt
						);
					}

					tx += tdx;
					sx++;
					cnt--;
				}
			}
		}
	},

	getRealHits: function(maxHits, dist) {
		var div = Math.max(1.0, dist * 0.35);
		var minHits = Math.max(1, ((maxHits / div) | 0));

		return ((Math.random() * (maxHits - minHits + 1) + minHits) | 0);
	},

	sortVisibleObjects: function() {
		if (!Engine.visibleObjects.length) {
			return null;
		}

		Engine.visibleObjects.sort(Engine.sortByDistAsc);
		return Engine.visibleObjects[0];
	},

	executeActions: function(id, elapsedTime) {
		var actions = Level.actions[id];

		if (!actions || !actions.length) {
			return false;
		}

		for (var k = 0; k < actions.length; k++) {
			var act = actions[k];
			var marks = Level.marksHash[act.mark] || [];

			switch (act.type) {
				case Level.ACTION_CLOSE:
				case Level.ACTION_OPEN:
				case Level.ACTION_REQ_KEY: {
					for (var i = 0; i < marks.length; i++) {
						var mark = marks[i];
						var door = Level.doorsMap[mark.y][mark.x];

						if (door) {
							door.stick(act.type == Level.ACTION_OPEN);

							if (act.type == Level.ACTION_REQ_KEY) {
								door.requiredKey = act.param;
							}
						}
					}
				}
				break;

				case Level.ACTION_UNMARK: {
					for (var i = 0; i < marks.length; i++) {
						Level.marksMap[marks[i].y][marks[i].x] = null;
					}

					for (var i = 0; i < Level.doors.length; i++) {
						var door = Level.doors[i];

						if (door.mark && door.mark.id == act.mark) {
							door.mark = null;
						}
					}

					Level.marksHash[act.mark] = [];
					var newMarks = [];

					for (var i = 0; i < Level.marks.length; i++) {
						if (Level.marks[i].id != act.mark) {
							newMarks.push(Level.marks[i]);
						}
					}

					Level.marks = newMarks;
				}
				break;

				case Level.ACTION_WALL: {
					for (var l = 0; l < marks.length; l++) {
						var mark = marks[l];

						if ((Level.passableMap[mark.y][mark.x] & Level.PASSABLE_IS_MONSTER) != 0) {
							var newMonsters = [];

							for (var i = 0; i < Level.monsters.length; i++) {
								var mon = Level.monsters[i];

								if (mon.cellX != mark.x || mon.cellY != mark.y) {
									newMonsters.push(mon);
								}
							}

							Level.monsters = newMonsters;
						}

						if (act.param > 0) {
							Level.wallsMap[mark.y][mark.x] = Tex.BASE_WALLS + act.param - 1;
							Level.objectsMap[mark.y][mark.x] = null;
							Level.passableMap[mark.y][mark.x] = Level.PASSABLE_IS_WALL;
						} else {
							Level.wallsMap[mark.y][mark.x] = 0;
							Level.objectsMap[mark.y][mark.x] = null;
							Level.passableMap[mark.y][mark.x] = 0;
						}

						if (Level.doorsMap[mark.y][mark.x] != null) {
							var newDoors = [];

							for (var i = 0; i < Level.doors.length; i++) {
								if (Level.doors[i] != Level.doorsMap[mark.y][mark.x]) {
									newDoors.push(Level.doors[i]);
								}
							}

							Level.doorsMap[mark.y][mark.x] = null;
							Level.doors = newDoors;
						}
					}
				}
				break;

				case Level.ACTION_NEXT_LEVEL:
					Hero.nextLevelTime = elapsedTime;
					break;

				case Level.ACTION_DISABLE_PISTOL:
					Hero.hasWeapon[Weapons.WEAPON_PISTOL] = false;
					Hero.weapon = Weapons.WEAPON_HAND;
					Weapons.updateWeapon();
					break;

				case Level.ACTION_ENABLE_PISTOL:
					Hero.reInitPistol();
					Hero.weapon = Weapons.WEAPON_PISTOL;
					Weapons.updateWeapon();
					break;

				case Level.ACTION_WEAPON_HAND:
					Hero.weapon = Weapons.WEAPON_HAND;
					Weapons.updateWeapon();
					break;

				case Level.ACTION_RESTORE_HEALTH:
					Hero.health = 100;
					break;

				case Level.ACTION_SECRET:
					if ((Hero.foundSecretsMask & act.param) == 0) {
						Hero.foundSecretsMask |= act.param;
						Hero.foundSecrets++;
						Overlay.showLabel(Overlay.LABEL_SECRET_FOUND, elapsedTime);
					}
					break;

				case Level.ACTION_ENSURE_WEAPON:
					if (act.param > 0 && act.param < Weapons.WEAPON_LAST) {
						Hero.hasWeapon[act.param] = true;

						if (act.param == Weapons.WEAPON_PISTOL || act.param == Weapons.WEAPON_CHAINGUN || act.param == Weapons.WEAPON_DBLCHAINGUN) {
							if (Hero.ammo[Weapons.AMMO_PISTOL] < Weapons.ENSURED_PISTOL_AMMO) {
								Hero.ammo[Weapons.AMMO_PISTOL] = Weapons.ENSURED_PISTOL_AMMO;
							}
						} else if (act.param == Weapons.WEAPON_SHOTGUN || act.param == Weapons.WEAPON_DBLSHOTGUN) {
							if (Hero.ammo[Weapons.AMMO_SHOTGUN] < Weapons.ENSURED_SHOTGUN_AMMO) {
								Hero.ammo[Weapons.AMMO_SHOTGUN] = Weapons.ENSURED_SHOTGUN_AMMO;
							}
						}

						Weapons.updateWeapon();
						Weapons.switchWeapon(act.param, 0);
					}
					break;

				case Level.ACTION_NEXT_TUTOR_LEVEL:
				case Level.ACTION_BTN_ON:
				case Level.ACTION_BTN_OFF:
				case Level.ACTION_MSG_ON:
				case Level.ACTION_MSG_OFF:
				case Level.ACTION_SELECT_CONTROLS:
					// do nothing
					break;
			}
		}

		return true;
	}
};
