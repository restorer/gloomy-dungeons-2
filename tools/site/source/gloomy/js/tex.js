window.Tex = {
	BASE_ICONS: 0x00,
	BASE_WALLS: 0x10,
	BASE_TRANSPARENTS: 0x30,
	BASE_DOORS_F: 0x40,
	BASE_DOORS_S: 0x44,
	BASE_OBJECTS: 0x50,
	BASE_DECORATIONS: 0x48,

	HAND: 5,
	PIST: 9,
	SHTG: 14,
	CHGN: 19,

	MAPS: [
		{ img: 'background.jpg', wdt: 250, hgt: 175 }, // 0
		{ img: 'texmap.png', wdt: 64, hgt: 64, cols: 16 }, // 1
		{ img: 'texmap_mon_1.png', wdt: 128, hgt: 128, cols: 8 }, // 2
		{ img: 'texmap_mon_2.png', wdt: 128, hgt: 128, cols: 8 }, // 3
		{ img: 'texmap_mon_3.png', wdt: 128, hgt: 128, cols: 8 }, // 4
		{ img: 'hit_hand_1.png', wdt: 256, hgt: 128 }, // 5
		{ img: 'hit_hand_2.png', wdt: 256, hgt: 128 }, // 6
		{ img: 'hit_hand_3.png', wdt: 256, hgt: 128 }, // 7
		{ img: 'hit_hand_4.png', wdt: 256, hgt: 128 }, // 8
		{ img: 'hit_pist_1.png', wdt: 256, hgt: 128 }, // 9
		{ img: 'hit_pist_2.png', wdt: 256, hgt: 128 }, // 10
		{ img: 'hit_pist_3.png', wdt: 256, hgt: 128 }, // 11
		{ img: 'hit_pist_4.png', wdt: 256, hgt: 128 }, // 12
		{ img: 'hit_pist_5.png', wdt: 256, hgt: 128 }, // 13
		{ img: 'hit_shtg_1.png', wdt: 256, hgt: 128 }, // 14
		{ img: 'hit_shtg_2.png', wdt: 256, hgt: 128 }, // 15
		{ img: 'hit_shtg_3.png', wdt: 256, hgt: 128 }, // 16
		{ img: 'hit_shtg_4.png', wdt: 256, hgt: 128 }, // 17
		{ img: 'hit_shtg_5.png', wdt: 256, hgt: 128 }, // 18
		{ img: 'hit_chgn_1.png', wdt: 256, hgt: 128 }, // 19
		{ img: 'hit_chgn_2.png', wdt: 256, hgt: 128 }, // 20
		{ img: 'hit_chgn_3.png', wdt: 256, hgt: 128 }, // 21
		{ img: 'hit_chgn_4.png', wdt: 256, hgt: 128 } // 22
	],

	init: function() {
		Tex.OBJ_ARMOR_GREEN = Tex.BASE_OBJECTS + 0;
		Tex.OBJ_ARMOR_RED = Tex.BASE_OBJECTS + 1;
		Tex.OBJ_KEY_BLUE = Tex.BASE_OBJECTS + 2;
		Tex.OBJ_KEY_RED = Tex.BASE_OBJECTS + 3;
		Tex.OBJ_STIM = Tex.BASE_OBJECTS + 4;
		Tex.OBJ_MEDI = Tex.BASE_OBJECTS + 5;
		Tex.OBJ_CLIP = Tex.BASE_OBJECTS + 6;
		Tex.OBJ_AMMO = Tex.BASE_OBJECTS + 7;
		Tex.OBJ_SHELL = Tex.BASE_OBJECTS + 8;
		Tex.OBJ_SBOX = Tex.BASE_OBJECTS + 9;
		Tex.OBJ_BPACK = Tex.BASE_OBJECTS + 10;
		Tex.OBJ_SHOTGUN = Tex.BASE_OBJECTS + 11;
		Tex.OBJ_KEY_GREEN = Tex.BASE_OBJECTS + 12;
		Tex.OBJ_CHAINGUN = Tex.BASE_OBJECTS + 13;
		Tex.OBJ_DBLSHOTGUN = Tex.BASE_OBJECTS + 14;
	},

	load: function(callback) {
		var loaded = 0;

		var errorHandler = function() {
			App.error("Can't load textures");
		}

		var loadHandler = function() {
			loaded += 1;

			if (loaded >= Tex.MAPS.length) {
				callback();
			} else {
				App.log('Loading textures ' + (loaded + 1) + '/' + Tex.MAPS.length);
			}
		}

		App.log('Loading textures 1/' + Tex.MAPS.length);

		for (var i = 0; i < Tex.MAPS.length; i++) {
			var img = new Image();
			img.onerror = errorHandler;
			img.onload = loadHandler;
			img.src = 'gloomy/' + Tex.MAPS[i].img;

			Tex.MAPS[i].img = img;
		}
	}
};
