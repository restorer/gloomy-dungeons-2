window.Level = {
	WIDTH: 64,
	HEIGHT: 64,

	ACTION_CLOSE: 1,
	ACTION_OPEN: 2,
	ACTION_REQ_KEY: 3,
	ACTION_WALL: 4,
	ACTION_NEXT_LEVEL: 5,
	ACTION_NEXT_TUTOR_LEVEL: 6,
	ACTION_DISABLE_PISTOL: 7,
	ACTION_ENABLE_PISTOL: 8,
	ACTION_WEAPON_HAND: 9,
	ACTION_RESTORE_HEALTH: 10,
	ACTION_SECRET: 11,
	ACTION_UNMARK: 12,
	ACTION_ENSURE_WEAPON: 13,
	ACTION_BTN_ON: 14,
	ACTION_BTN_OFF: 15,
	ACTION_MSG_ON: 16,
	ACTION_MSG_OFF: 17,
	ACTION_SELECT_CONTROLS: 18,

	BUTTONS_MAP: {
		'FORWARD': 1,
		'BACKWARD': 2,
		'STRAFE_LEFT': 3,
		'STRAFE_RIGHT': 4,
		'ACTION': 5,
		'NEXT_WEAPON': 6,
		'ROTATE_LEFT': 7,
		'ROTATE_RIGHT': 8,
		'TOGGLE_MAP': 9,
		'STRAFE_MODE': 10
	},

	MESSAGES_MAP: {
		'PRESS_FORWARD': 'press_forward',
		'PRESS_ROTATE': 'press_rotate',
		'PRESS_ACTION_TO_OPEN_DOOR': 'press_action_to_open_door',
		'SWITCH_AT_RIGHT': 'switch_at_right',
		'PRESS_ACTION_TO_SWITCH': 'press_action_to_switch',
		'KEY_AT_LEFT': 'key_at_left',
		'PRESS_ACTION_TO_FIGHT': 'press_action_to_fight',
		'PRESS_MAP': 'press_map',
		'PRESS_NEXT_WEAPON': 'press_next_weapon',
		'OPEN_DOOR_USING_KEY': 'open_door_using_key',
		'PRESS_END_LEVEL_SWITCH': 'press_end_level_switch',
		'GO_TO_DOOR': 'go_to_door'
	},

	PASSABLE_IS_WALL: 1,
	PASSABLE_IS_TRANSP_WALL: 2,
	PASSABLE_IS_OBJECT: 4,
	PASSABLE_IS_DECORATION: 8,
	PASSABLE_IS_DOOR: 16,
	PASSABLE_IS_HERO: 32,
	PASSABLE_IS_MONSTER: 64,
	PASSABLE_IS_DEAD_CORPSE: 128,
	PASSABLE_IS_OBJECT_ORIG: 256,
	PASSABLE_IS_TRANSP: 512,
	PASSABLE_IS_OBJECT_KEY: 1024,
	PASSABLE_IS_DOOR_OPENED_BY_HERO: 2048,

	HIT_TYPE_EAT: 0,
	HIT_TYPE_PIST: 1,
	HIT_TYPE_SHTG: 2,

	h: 0,
	w: 0,
	levelData: [],
	marksMap: [],
	availMarks: {},
	marks: [],
	marksHash: {},

	wallsMap: [],
	passableMap: [],
	doorsMap: [],
	objectsMap: [],
	doors: [],
	monsters: [],
	monstersConf: [],
	actions: {},
	secretsMask: 0,
	totalSecrets: 0,
	totalItems: 0,

	init: function() {
		Level.PASSABLE_MASK_HERO = Level.PASSABLE_IS_WALL |
			Level.PASSABLE_IS_TRANSP_WALL |
			Level.PASSABLE_IS_DECORATION |
			Level.PASSABLE_IS_DOOR |
			Level.PASSABLE_IS_MONSTER;

		Level.PASSABLE_MASK_MONSTER = Level.PASSABLE_IS_WALL |
			Level.PASSABLE_IS_TRANSP_WALL |
			Level.PASSABLE_IS_DECORATION |
			Level.PASSABLE_IS_DOOR |
			Level.PASSABLE_IS_MONSTER |
			Level.PASSABLE_IS_HERO;

		Level.PASSABLE_MASK_SHOOT_W = Level.PASSABLE_IS_WALL | Level.PASSABLE_IS_DOOR;
		Level.PASSABLE_MASK_SHOOT_WM = Level.PASSABLE_IS_WALL | Level.PASSABLE_IS_DOOR | Level.PASSABLE_IS_DECORATION | Level.PASSABLE_IS_MONSTER;
		Level.PASSABLE_MASK_WALL_N_TRANSP = Level.PASSABLE_IS_WALL | Level.PASSABLE_IS_TRANSP;
		Level.PASSABLE_MASK_OBJECT = Level.PASSABLE_IS_OBJECT | Level.PASSABLE_IS_OBJECT_ORIG | Level.PASSABLE_IS_OBJECT_KEY;
		Level.PASSABLE_MASK_DOOR = ~Level.PASSABLE_IS_DOOR_OPENED_BY_HERO;

		Level.PASSABLE_MASK_OBJECT_DROP = Level.PASSABLE_IS_WALL |
			Level.PASSABLE_IS_TRANSP_WALL |
			Level.PASSABLE_IS_DECORATION |
			Level.PASSABLE_IS_DOOR |
			Level.PASSABLE_IS_OBJECT;
	},

	isPassable: function(x, y, wallDist, mask) {
		var fx = Math.max(0, (x - wallDist) | 0);
		var tx = Math.min(Level.WIDTH - 1, (x + wallDist) | 0);
		var fy = Math.max(0, (y - wallDist) | 0);
		var ty = Math.min(Level.HEIGHT - 1, (y + wallDist) | 0);

		for (var i = fx; i <= tx; i++) {
			for (var j = fy; j <= ty; j++) {
				if (Level.passableMap[j][i] & mask) {
					return false;
				}
			}
		}

		return true;
	},

	setPassable: function(x, y, wallDist, mask) {
		var fx = Math.max(0, (x - wallDist) | 0);
		var tx = Math.min(Level.WIDTH - 1, (x + wallDist) | 0);
		var fy = Math.max(0, (y - wallDist) | 0);
		var ty = Math.min(Level.HEIGHT - 1, (y + wallDist) | 0);

		for (var i = fx; i <= tx; i++) {
			for (var j = fy; j <= ty; j++) {
				Level.passableMap[j][i] |= mask;
			}
		}
	},

	clearPassable: function(x, y, wallDist, mask) {
		var fx = Math.max(0, (x - wallDist) | 0);
		var tx = Math.min(Level.WIDTH - 1, (x + wallDist) | 0);
		var fy = Math.max(0, (y - wallDist) | 0);
		var ty = Math.min(Level.HEIGHT - 1, (y + wallDist) | 0);

		mask = ~mask;

		for (var i = fx; i <= tx; i++) {
			for (var j = fy; j <= ty; j++) {
				Level.passableMap[j][i] &= mask;
			}
		}
	},

	// modified Level_CheckLine from wolf3d for iphone by Carmack
	traceLine: function(x1, y1, x2, y2, mask) {
		var cx1 = x1 | 0;
		var cy1 = y1 | 0;
		var cx2 = x2 | 0;
		var cy2 = y2 | 0;

		if ((cx1 < 0) || (cx1 >= Level.WIDTH) ||
			(cx2 < 0) || (cx2 >= Level.WIDTH) ||
			(cy1 < 0) || (cy1 >= Level.HEIGHT) ||
			(cy2 < 0) || (cy2 >= Level.HEIGHT)
		) {
			return false;
		}

		if (cx1 != cx2) {
			var stepX;
			var partial;

			if (cx2 > cx1) {
				partial = 1.0 - (x1 - (x1 | 0));
				stepX = 1;
			} else {
				partial = x1 - (x1 | 0);
				stepX = -1;
			}

			var dx = ((x2 >= x1) ? (x2 - x1) : (x1 - x2));
			var stepY = (y2 - y1) / dx;
			var y = y1 + (stepY * partial);

			cx1 += stepX;
			cx2 += stepX;

			do {
				if ((Level.passableMap[y | 0][cx1] & mask) != 0) {
					return false;
				}

				y += stepY;
				cx1 += stepX;
			} while (cx1 != cx2);
		}

		if (cy1 != cy2) {
			var stepY;
			var partial;

			if (cy2 > cy1) {
				partial = 1.0 - (y1 - (y1 | 0));
				stepY = 1;
			} else {
				partial = y1 - (y1 | 0);
				stepY = -1;
			}

			var dy = ((y2 >= y1) ? (y2 - y1) : (y1 - y2));
			var stepX = (x2 - x1) / dy;
			var x = x1 + (stepX * partial);

			cy1 += stepY;
			cy2 += stepY;

			do {
				if ((Level.passableMap[cy1][x | 0] & mask) != 0) {
					return false;
				}

				x += stepX;
				cy1 += stepY;
			} while (cy1 != cy2);
		}

		return true;
	},

	fillLine: function(value) {
		var line = [];

		for (var i = 0; i < Level.WIDTH; i++) {
			line.push(value);
		}

		return line;
	},

	prepareData: function(strData) {
		var strData = strData.replace(/[^0-9A-Fa-f]/g, '');
		var strPos = 0;

		function getHex() {
			if (strPos + 1 >= strData.length) {
				return -1;
			}

			var res = parseInt(strData.substr(strPos, 2), 16);
			strPos += 2;

			return res;
		}

		Level.h = getHex();
		Level.w = getHex();
		var y = getHex();
		var x = getHex();

		if (Level.h <= 0 || Level.w <= 0 || x < 0 || y < 0 || Level.h > Level.HEIGHT || Level.w > Level.WIDTH) {
			App.error('Invalid level data (size or position)');
			return false;
		}

		Level.levelData = [];
		Level.marksMap = [];
		Level.availMarks = {};
		Level.marks = [];

		for (var i = 0; i < Level.HEIGHT; i++) {
			Level.levelData.push(Level.fillLine(0));
			Level.marksMap.push(Level.fillLine(null));
			Level.wallsMap.push(Level.fillLine(0));
			Level.passableMap.push(Level.fillLine(0));
			Level.doorsMap.push(Level.fillLine(null));
			Level.objectsMap.push(Level.fillLine(null));
		}

		for (var i = 0; i < Level.h; i++) {
			for (var j = 0; j < Level.w; j++) {
				var idx = getHex();

				if (idx < 0) {
					App.error('Invalid level data (cell data at ' + i + ',' + j + ')');
					return false;
				}

				Level.levelData[i][Level.w - j] = idx;
			}
		}

		for (var i = 0; i < Level.h; i++) {
			for (var j = 0; j < Level.w; j++) {
				var idx = getHex();

				if (idx < 0) {
					App.error('Invalid level data (mark data at ' + i + ',' + j + ')');
					return false;
				}

				if (idx != 0) {
					var mark = { id: idx, x: Level.w - j, y: i };
					Level.marksMap[i][Level.w - j] = mark;
					Level.availMarks[idx] = true;
					Level.marks.push(mark);
				}
			}
		}

		return true;
	},

	parseLevel: function(strData) {
		if (!Level.prepareData(strData)) {
			return false;
		}

		for (var i = 0; i < Level.h; i++) {
			for (var j = 0; j < Level.w; j++) {
				var value = Level.levelData[i][j];

				// guarantee 1-cell wall border around level
				if ((value < 0x10 || value >= 0x30) && (i == 0 || j == 0 || i == (Level.h - 1) || j == (Level.w - 1))) {
					value = 0x10;
				}

				if (value < 0x10) {
					if (value > 0) {
						if (value <= 4) {
							Hero.x = j + 0.5;
							Hero.y = i + 0.5;
							Hero.a = (value * Engine.ANG_90) % Engine.ANG_360;

							Level.passableMap[i][j] |= Level.PASSABLE_IS_HERO;
						} else if (value == 5) {
							// transparents is not supported by this engine
						}
					}
				} else if (value < 0x30) {
					Level.wallsMap[i][j] = value - 0x10 + Tex.BASE_WALLS;
					Level.passableMap[i][j] |= Level.PASSABLE_IS_WALL;
				} else if (value < 0x50) {
					// transparents is not supported by this engine
				} else if (value < 0x70) {
					var door = new Door(
						j, i,
						(value > 0x53 ? 1 : value - 0x50) + Tex.BASE_DOORS_F,
						(value > 0x53 ? 1 : value - 0x50) + Tex.BASE_DOORS_S
					);

					Level.doors.push(door);
					Level.doorsMap[i][j] = door;
					Level.wallsMap[i][j] = -1;
					Level.passableMap[i][j] |= Level.PASSABLE_IS_DOOR;
				} else if (value < 0x80) {
					var tex = value - 0x70 + Tex.BASE_OBJECTS;
					Level.objectsMap[i][j] = new GObject(j, i, tex, false);
					Level.passableMap[i][j] |= Level.PASSABLE_IS_OBJECT | Level.PASSABLE_IS_OBJECT_ORIG;

					if (tex == Tex.OBJ_KEY_BLUE || tex == Tex.OBJ_KEY_RED || tex == Tex.OBJ_KEY_GREEN) {
						Level.passableMap[i][j] |= Level.PASSABLE_IS_OBJECT_KEY;
					}

					Level.totalItems += 1;
				} else if (value < 0xA0) {
					var tex;

					if (value == 0x88) {
						tex = 0x3E;
					} else if (value == 0x89) {
						tex = 0x3F;
					} else if (value == 0x8C) {
						tex = 0x2F;
					} else {
						tex = value - 0x80 + Tex.BASE_DECORATIONS;
					}

					Level.objectsMap[i][j] = new GObject(j, i, tex, true);

					if ((value % 0x10) < 12) {
						Level.passableMap[i][j] |= Level.PASSABLE_IS_DECORATION;
					}
				} else {
					var num = ((value - 0xA0) / 0x10) | 0;
					Level.passableMap[i][j] |= Level.PASSABLE_IS_MONSTER;

					Level.monsters.push(new Monster(
						j, i,
						(7 - (value % 4)) % 4,
						Level.monstersConf[num].tmap,
						Level.monstersConf[num].health,
						Level.monstersConf[num].hits,
						Level.monstersConf[num].hitType
					));
				}
			}
		}

		for (var i = 0; i < Level.doors.length; i++) {
			var door = Level.doors[i];
			door.ver = (Level.wallsMap[door.y][door.x - 1] == 0) && (Level.wallsMap[door.y][door.x + 1] == 0);
		}

		return true;
	},

	parseActions: function(strData) {
		var lines = strData.split(/[\r\n]+/);
		var usedMarks = {};

		for (var i = 0; i < lines.length; i++) {
			var line = lines[i].replace(/^\s+/, '').replace(/\s+$/, '');

			if (line == '') {
				continue;
			}

			var spl = lines[i].split(':');

			if (spl.length != 2) {
				App.error('Invalid action (' + lines[i] + ')');
				return false;
			}

			var key = spl[0].replace(/^\s+/, '').replace(/\s+$/, '');
			key = (key == '' ? 0 : Number(key));

			if (key > 0 && key < 100 && !Level.availMarks[key]) {
				App.error('Unknown mark (' + key + ')');
				return false;
			}

			var values = spl[1].replace(/^\s+/, '').replace(/\s+$/, '').split(/\s+/);

			if (key != 0) {
				usedMarks[key] = true;
			}

			if (!Level.actions[key]) {
				Level.actions[key] = [];
			}

			var pos = 0;

			while (pos < values.length) {
				var val = values[pos];
				pos += 1;

				if (['close', 'open', 'unmark'].indexOf(val) >= 0) {
					var mark = Number(values[pos]);
					pos += 1;

					if (Level.availMarks[mark]) {
						usedMarks[mark] = true;

						Level.actions[key].push({
							type: (val == 'unmark' ? Level.ACTION_UNMARK : (val == 'close' ? Level.ACTION_CLOSE : Level.ACTION_OPEN)),
							mark: mark,
							param: 0
						});
					}
				} else if (['req_key', 'wall'].indexOf(val) >= 0) {
					var mark = Number(values[pos]);
					var param = Number(values[pos + 1]);
					pos += 2;

					if (Level.availMarks[mark]) {
						usedMarks[mark] = true;

						Level.actions[key].push({
							type: (val == 'req_key' ? Level.ACTION_REQ_KEY : Level.ACTION_WALL),
							mark: mark,
							param: param
						});
					}
				} else if (val == 'next_level') {
					Level.actions[key].push({ type: Level.ACTION_NEXT_LEVEL, mark: 0, param: 0 });
				} else if (val == 'next_tutor_level') {
					Level.actions[key].push({ type: Level.ACTION_NEXT_TUTOR_LEVEL, mark: 0, param: 0 });
				} else if (val == 'disable_pistol') {
					Level.actions[key].push({ type: Level.ACTION_DISABLE_PISTOL, mark: 0, param: 0 });
				} else if (val == 'enable_pistol') {
					Level.actions[key].push({ type: Level.ACTION_ENABLE_PISTOL, mark: 0, param: 0 });
				} else if (val == 'weapon_hand') {
					Level.actions[key].push({ type: Level.ACTION_WEAPON_HAND, mark: 0, param: 0 });
				} else if (val == 'restore_health') {
					Level.actions[key].push({ type: Level.ACTION_RESTORE_HEALTH, mark: 0, param: 0 });
				} else if (['secret', 'ensure_weapon'].indexOf(val) >= 0) {
					var param = Number(values[pos]);
					pos += 1;

					Level.actions[key].push({
						type: (val == 'secret' ? Level.ACTION_SECRET : Level.ACTION_ENSURE_WEAPON),
						mark: 0,
						param: param
					});

					if (val == 'secret' && !(Level.secretsMask & param)) {
						Level.secretsMask |= param;
						Level.totalSecrets += 1;
					}
				} else if (val == 'btn_on') {
					var param = values[pos];
					pos += 1;

					if (Level.BUTTONS_MAP[param]) {
						param = Level.BUTTONS_MAP[param];
					} else {
						App.error('Unknown button name (' + param + ')');
						return false;
					}

					Level.actions[key].push({
						type: Level.ACTION_BTN_ON,
						mark: 0,
						param: (1 << (param - 1))
					});
				} else if (val == 'btn_off') {
					Level.actions[key].push({ type: Level.ACTION_BTN_OFF, mark: 0, param: 0 });
				} else if (val == 'msg_on') {
					var param = values[pos];
					pos += 1;

					if (Level.MESSAGES_MAP[param]) {
						param = Level.MESSAGES_MAP[param];
					} else {
						App.error('Unknown message (' + param + ')');
						return false;
					}

					Level.actions[key].push({
						type: Level.ACTION_BTN_ON,
						mark: 0,
						param: param
					});
				} else if (val == 'msg_off') {
					Level.actions[key].push({ type: Level.ACTION_MSG_OFF, mark: 0, param: 0 });
				} else if (val == 'select_controls') {
					Level.actions[key].push({ type: Level.ACTION_SELECT_CONTROLS, mark: 0, param: 0 });
				} else {
					App.error('Unknown action (' + val + ')');
				}
			}
		}


		var resMarks = [];

		for (var i = 0; i < Level.marks.length; i++) {
			var mark = Level.marks[i];

			if (usedMarks[mark.id]) {
				resMarks.push(mark);

				if (!Level.marksHash[mark.id]) {
					Level.marksHash[mark.id] = [];
				}

				Level.marksHash[mark.id].push(mark);

				if (Level.doorsMap[mark.y][mark.x]) {
					Level.doorsMap[mark.y][mark.x].mark = mark;
					Level.marksMap[mark.y][mark.x] = null;
				}
			} else {
				Level.marksMap[mark.y][mark.x] = null;
			}
		}

		Level.marks = resMarks;
		return true;
	},

	parseConf: function(confStr) {
		var lines = confStr.replace(/^[\r\n]+/, '').replace(/[\r\n]+$/, '').split(/[\r\n]+/);

		if (lines.length != 5) {
			App.error('Invalid level config (lines count)');
			return false;
		}

		for (var i = 1; i <= 4; i++) {
			var spl = lines[i].replace(/^\s+/, '').replace(/\s+$/, '').split(/\s+/);

			if (spl.length != 4) {
				App.error('Invalid level config (line ' + (i + 1) + ')');
				return false;
			}

			Level.monstersConf.push({
				tmap: Number(spl[0]) + 1,
				health: Number(spl[1]),
				hits: Number(spl[2]),
				hitType: Number(spl[3])
			});
		}

		return true;
	},

	load: function(num, callback) {
		App.log('Loading config');

		jQuery.get('gloomy/conf-' + num + '.txt', function(confStr) {
			App.log('Loading level');

			jQuery.get('gloomy/level-' + num + '.txt', function(levelStr) {
				App.log('Loading actions');

				jQuery.get('gloomy/actions-' + num + '.txt', function(actionsStr) {
					App.log('Parsing config');

					if (Level.parseConf(confStr)) {
						App.log('Parsing level');

						if (Level.parseLevel(levelStr)) {
							App.log('Parsing actions');

							if (Level.parseActions(actionsStr)) {
								callback();
							}
						}
					}
				});
			});
		});
	}
};
