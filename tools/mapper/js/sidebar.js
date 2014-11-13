var sidebar = (function() {
	function getSingleItemHtml(type, value) {
		if (type == app.T_FLOOR_SEL || type == app.T_CEIL_SEL) {
			return '<div class="item item-sel" _ta="'
				+ type + '" _va="0" _tb="0" _vb="0">'
				+ '<div class="item-sel-inner item-inner-tl" _t="' + type + '" _v="1"></div>'
				+ '<div class="item-sel-inner item-inner-tr" _t="' + type + '" _v="2"></div>'
				+ '<div class="item-sel-inner item-inner-bl" _t="' + type + '" _v="3"></div>'
				+ '<div class="item-sel-inner item-inner-br" _t="' + type + '" _v="4"></div>'
				+ '</div>';
		} else if (type == app.T_FLOOR_DISP || type == app.T_CEIL_DISP) {
			return '<div class="item item-disp" _ta="'
				+ type + '" _va="0" _tb="0" _vb="0">'
				+ '<div class="item-disp-inner b-disp-' + type + '-1 item-inner-tl" style="background-position:0 0;"></div>'
				+ '<div class="item-disp-inner b-disp-' + type + '-2 item-inner-tr" style="background-position:-32px 0;"></div>'
				+ '<div class="item-disp-inner b-disp-' + type + '-3 item-inner-bl" style="background-position:0 -32px;"></div>'
				+ '<div class="item-disp-inner b-disp-' + type + '-4 item-inner-br" style="background-position:-32px -32px;"></div>'
				+ '<div class="item-disp-border"></div>'
				+ '</div>';
		} else {
			return '<div class="item" _ta="'
				+ type + '" _va="' + value
				+ '" _tb="0" _vb="0" style="background-image:url('
				+ paths.list[type][value] + ');"></div>';
		}
	}

	function toolbox(id, typeA, typeB, list) {
		var html = '';

		if (typeA == 0 && typeB == 0) {
			for (var i = 0; i < list.length; i += 2) {
				html += getSingleItemHtml(list[i], list[i + 1]);
			}
		} else if (typeA != 0 && typeB != 0) {
			for (var i = 0; i < list.length; i += 2) {
				html += '<div class="item item-double" _ta="'
					+ typeA + '" _va="' + list[i]
					+ '" _tb="' + typeB + '" _vb="' + list[i + 1]
					+ '" style="background-image:url('
					+ paths.list[typeA][list[i]] + '),url('
					+ paths.list[typeB][list[i + 1]] + ');"></div>';
			}
		} else {
			for (var i = 0; i < list.length; i++) {
				html += getSingleItemHtml(typeA, list[i]);
			}
		}

		lib.query('#' + id + ' .toolbox-items').set('innerHTML', html);
	}

	return {
		init: function() {
			toolbox('special', 0, 0, [
				app.T_HERO, 1, app.T_HERO, 2, app.T_HERO, 3, app.T_HERO, 4,
				app.T_ARROW, 1, app.T_ARROW, 2, app.T_ARROW, 3, app.T_ARROW, 4,
				app.T_FLOOR_SEL, 0, app.T_FLOOR_DISP, 0, app.T_CEIL_SEL, 0, app.T_CEIL_DISP, 0,
				app.T_EMPTY, 0, app.T_JUST_FLOOR, 0, app.T_NO_CEIL, 0, app.T_NO_TRANS, 0
			]);

			toolbox('floor', app.T_FLOOR, 0, [
				1, 2, 3, 4, 5, 6, 7, 8, 9, 10
			]);

			toolbox('ceil', app.T_CEIL, 0, [
				1, 2, 3, 4, 5, 6
			]);

			toolbox('walls', app.T_WALL, 0, [
				1, 29, 32, // 33
				16,
				6, 7, 34, // 35
				17,
				11, 12, 20, // 21
				18,
				5, 26, 27, // 28
				19,
				8, 9, 10, 30, // 31
				22, 23, // 24
				2, 3,
				13, 14, 15,
				25,
				36, 37, 38, 39,
				40, 41, 42, 43,
				4
			]);

			toolbox('twall', app.T_TWALL, 0, [
				1, 2, 3,
				4
			]);

			toolbox('tpass', app.T_TPASS, 0, [
				1,
				2, 3, 4, 5, 6
			]);

			toolbox('twind', app.T_TWIND, 0, [
				1, 2,
				3, 4,
				5, 7,
				6, 8
			]);

			toolbox('doors', app.T_DOOR, app.T_DOOR_S, [
				1, 1,
				2, 2,
				3, 3,
				4, 4,
				5, 5,
				6, 6,
				7, 7,
				8, 8
			]);

			toolbox('ditem', app.T_DITEM, 0, [
				4,
				7,
				1, 10,
				2, 8,
				6, 9,
				3, 5
			]);

			toolbox('dlamp', app.T_DLAMP, 0, [
				1
			]);

			toolbox('objects', app.T_OBJ, 0, [
				5, 6,
				1, 2,
				7, 8, 9, 10,
				19, 20, 11, 18,
				3, 4, 13,
				16, 12, 14, 15, 17, 21
			]);

			toolbox('monsters', app.T_MON, 0, [
				1, 2, 3, 4, 5, 6, 7, 8
			]);

			lib.query('.toolbox .item').on('click', function() {
				var typeA = Number(this.getAttribute('_ta'));

				if (typeA != app.T_FLOOR_SEL && typeA != app.T_CEIL_SEL) {
					lib.query('.toolbox .item').removeClass('active');
					lib.query(this).addClass('active');

					app.selectedTool = {
						typeA: typeA,
						valueA: Number(this.getAttribute('_va')),
						typeB: Number(this.getAttribute('_tb')),
						valueB: Number(this.getAttribute('_vb'))
					};

					if (app.mode != app.MODE_DRAW) {
						app.setMode(app.MODE_DRAW);
					}
				}
			});

			lib.query('.toolbox .item-sel .item-sel-inner').on('click', function() {
				var type = Number(this.getAttribute('_t'));
				var value = Number(this.getAttribute('_v'));
				var toolType = (type == app.T_FLOOR_SEL ? app.T_FLOOR : app.T_CEIL);
				var dispType = (type == app.T_FLOOR_SEL ? app.T_FLOOR_DISP : app.T_CEIL_DISP);

				if (app.selectedTool.typeA == toolType) {
					lib.query('.b-disp-' + dispType + '-' + value).setStyle(
						'backgroundImage',
						'url(' + paths.list[toolType][app.selectedTool.valueA] + ')'
					);

					app[type == app.T_FLOOR_SEL ? 'specialFloor' : 'specialCeil'][value - 1] = app.selectedTool.valueA;
				} else if (type == app.T_CEIL_SEL && app.selectedTool.typeA == app.T_NO_CEIL) {
					lib.query('.b-disp-' + dispType + '-' + value).setStyle(
						'backgroundImage',
						'none'
					);

					app.specialCeil[value - 1] = 0;
				}
			});

			for (var i = 1; i <= 4; i++) {
				lib.query('.b-disp-' + app.T_FLOOR_DISP + '-' + i).setStyle(
					'backgroundImage',
					'url(' + paths.list[app.T_FLOOR][1] + ')'
				);

				lib.query('.b-disp-' + app.T_CEIL_DISP + '-' + i).setStyle(
					'backgroundImage',
					'url(' + paths.list[app.T_CEIL][1] + ')'
				);
			}
		}
	};
})();
