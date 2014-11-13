var paths = (function() {
	var RTIME = (new Date()).valueOf();

	function fill(type, prepend, append) {
		paths.list[type] = {};

		for (var i = 1; i < 10; i++) {
			paths.list[type][i] = GRAPHICS_URL + '/' + prepend + '0' + i + append + '.png?t=' + RTIME;
		}

		for (var i = 10; i < 100; i++) {
			paths.list[type][i] = GRAPHICS_URL + '/' + prepend + i + append + '.png?t=' + RTIME;
		}
	}

	return {
		list: {},
		graphicsSet: 'set-1',

		init: function() {
			fill(app.T_HERO, 'common/misc/hero_', '');
			fill(app.T_ARROW, 'common/misc/arrow_', '');
			fill(app.T_WALL, paths.graphicsSet + '/walls/wall_', '');
			fill(app.T_TWALL, paths.graphicsSet + '/twall/twall_', '');
			fill(app.T_TPASS, paths.graphicsSet + '/tpass/tpass_', '');
			fill(app.T_TWIND, paths.graphicsSet + '/twind/twind_', '');
			fill(app.T_DOOR, paths.graphicsSet + '/doors/door_', '_s'); // door_N_f and door_N_s swapped for better level readability
			fill(app.T_DITEM, paths.graphicsSet + '/ditem/ditem_', '');
			fill(app.T_DLAMP, paths.graphicsSet + '/dlamp/dlamp_', '');
			fill(app.T_OBJ, 'common/objects/obj_', '');
			fill(app.T_MON, 'common/monsters/mon_', '_a3');
			paths.list[app.T_EMPTY] = [ GRAPHICS_URL + '/common/misc/empty.png?t=' + RTIME ];
			paths.list[app.T_JUST_FLOOR] = [ GRAPHICS_URL + '/common/misc/just_floor.png?t=' + RTIME ];
			paths.list[app.T_NO_TRANS] = [ GRAPHICS_URL + '/common/misc/no_trans.png?t=' + RTIME ];
			paths.list[app.T_NO_CEIL] = [ GRAPHICS_URL + '/common/misc/no_ceil.png?t=' + RTIME ];
			fill(app.T_FLOOR, paths.graphicsSet + '/floor/floor_', '');
			fill(app.T_CEIL, paths.graphicsSet + '/ceil/ceil_', '');
			fill(app.T_DOOR_S, paths.graphicsSet + '/doors/door_', '_f'); // door_N_f and door_N_s swapped for better level readability
		},

		changeGraphicsSet: function(value) {
			if (value != paths.graphicsSet) {
				var replaceRegExp = new RegExp(lib.quoteRegExp('/' + paths.graphicsSet + '/'), 'g');
				var replaceTo = '/' + value + '/';

				lib.query('.sidebar .item').each(function(element) {
					element.style.backgroundImage = element.style.backgroundImage.replace(replaceRegExp, replaceTo);
				});

				lib.query('.sidebar .item-disp-inner').each(function(element) {
					element.style.backgroundImage = element.style.backgroundImage.replace(replaceRegExp, replaceTo);
				});

				paths.graphicsSet = value;
				paths.init();

				map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);
			}
		}
	};
})();
