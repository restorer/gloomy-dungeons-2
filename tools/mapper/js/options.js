var options = (function() {
	return {
		init: function() {
			lib.query('#graphics-set').on('change', function() {
				paths.changeGraphicsSet(this.value);
			});

			lib.query('#find-hero').on('click', function() {
				for (var i = 0; i < map.MAX_HEIGHT; i++) {
					for (var j = 0; j < map.MAX_WIDTH; j++) {
						if (map.m[i][j].type == app.T_HERO) {
							map.centerViewAt(j, i);
							options.hideSubDialog();
							break;
						}
					}
				}
			});

			lib.query('#find-mark').on('click', function() {
				var mark = String(lib.query('#find-mark-value').get('value')).trim()

				for (var i = 0; i < map.MAX_HEIGHT; i++) {
					for (var j = 0; j < map.MAX_WIDTH; j++) {
						if (map.m[i][j].mark == mark) {
							map.centerViewAt(j, i);
							options.hideSubDialog();
							break;
						}
					}
				}
			});

			lib.query('#center-map').on('click', function() {
				var bounds = map.getBounds();

				if (bounds == null) {
					return;
				}

				var undoMap = map.getMapCopy();
				var buf = map.copyToBuffer(bounds.sx, bounds.sy, bounds.ex, bounds.ey);

				for (var i = 0; i < map.MAX_HEIGHT; i++) {
					for (var j = 0; j < map.MAX_WIDTH; j++) {
						cell.clear(map.m[i][j]);
					}
				}

				var x = Math.floor((map.MAX_WIDTH - bounds.w) / 2);
				var y = Math.floor((map.MAX_HEIGHT - bounds.h) / 2);

				map.restoreFromBuffer(buf, x, y, undoMap);
				map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);

				map.centerViewAt(x + Math.floor(bounds.w / 2), y + Math.floor(bounds.h / 2));
				options.hideSubDialog();
			});
		},

		hideSubDialog: function() {
			lib.query('.b-options').removeClass('active');
			lib.query('.b-options-sub').hide();
		}
	};
})();
