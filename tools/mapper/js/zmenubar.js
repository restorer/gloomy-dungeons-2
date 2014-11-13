var zmenubar = (function() {
	return {
		init: function() {
			lib.query('.b-map-zoom').on('click', function() {
				lib.query('.b-map-zoom').removeClass('active');
				lib.query(this).addClass('active');
				lib.query('.map').set('className', 'map ' + this.getAttribute('rel'));
				map.zoomMult = Number(this.getAttribute('_mult'));
				map.zoomCellSize = 64 / map.zoomMult;
			});

			lib.query('.b-sidebar-zoom').on('click', function() {
				lib.query('.b-sidebar-zoom').removeClass('active');
				lib.query(this).addClass('active');
				lib.query('.sidebar').set('className', 'sidebar ' + this.getAttribute('rel'));
			});

			lib.query('.b-draw').on('click', function() {
				selection.active = false;
				selection.update();
				app.setMode(app.MODE_DRAW);
			});

			lib.query('.b-select').on('click', function() {
				if (app.mode != app.MODE_SELECT) {
					app.setMode(app.MODE_SELECT);
				}
			});

			lib.query('.b-deselect').on('click', function() {
				if (selection.active) {
					selection.active = false;
					selection.update();
					app.setMode(app.MODE_DRAW);
				}
			});

			lib.query('.b-copy').on('click', function() {
				if (selection.active && app.mode != app.MODE_COPY) {
					map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);
					app.setMode(app.MODE_COPY);
				}
			});

			lib.query('.b-move').on('click', function() {
				if (selection.active && app.mode != app.MODE_MOVE) {
					map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);
					app.setMode(app.MODE_MOVE);
				}
			});

			lib.query('.b-fill').on('click', function() {
				if (selection.active) {
					map.tmpMap = null;

					for (var i = selection.sy; i <= selection.ey; i++) {
						for (var j = selection.sx; j <= selection.ex; j++) {
							map.setCellValue(j, i);
						}
					}

					if (map.tmpMap != null) {
						map.addToUndo(map.tmpMap);
						map.tmpMap = null;
						map.update(selection.sx, selection.sy, selection.w, selection.h);
					}

					if (app.mode != app.MODE_SELECT) {
						app.setMode(app.MODE_SELECT);
					}
				}
			});

			/*
			lib.query('.b-rotate-rt').on('click', function() {
				if (selection.active) {
					map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);
					map.tmpMap = null;

					for (var i = selection.sy; i <= selection.ey; i++) {
						for (var j = selection.sx; j <= selection.ex; j++) {
						}
					}

					if (map.tmpMap != null) {
						map.addToUndo(map.tmpMap);
						map.tmpMap = null;
						map.update(selection.sx, selection.sy, selection.w, selection.h);
						map.update(selection.sx, selection.sy, selection.h, selection.w);
					}

					if (app.mode != app.MODE_SELECT) {
						app.setMode(app.MODE_SELECT);
					}
				}
			});

			lib.query('.b-rotate-lt').on('click', function() {
				if (selection.active) {
				}
			});
			*/

			lib.query('.b-flip-hor').on('click', function() {
				if (selection.active) {
					map.tmpMap = map.getMapCopy();
					map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);

					for (var i = selection.sy; i <= selection.ey; i++) {
						for (var j = selection.sx; j <= selection.ex; j++) {
							cell.copyToFrom(map.m[i][j], map.copyBuffer[i - selection.sy][selection.w - (j - selection.sx) - 1]);
						}
					}

					map.addToUndo(map.tmpMap);
					map.tmpMap = null;
					map.update(selection.sx, selection.sy, selection.w, selection.h);

					if (app.mode != app.MODE_SELECT) {
						app.setMode(app.MODE_SELECT);
					}
				}
			});

			lib.query('.b-flip-ver').on('click', function() {
				if (selection.active) {
					map.tmpMap = map.getMapCopy();
					map.copyBuffer = map.copyToBuffer(selection.sx, selection.sy, selection.ex, selection.ey);

					for (var i = selection.sy; i <= selection.ey; i++) {
						for (var j = selection.sx; j <= selection.ex; j++) {
							cell.copyToFrom(map.m[i][j], map.copyBuffer[selection.h - (i - selection.sy) - 1][j - selection.sx]);
						}
					}

					map.addToUndo(map.tmpMap);
					map.tmpMap = null;
					map.update(selection.sx, selection.sy, selection.w, selection.h);

					if (app.mode != app.MODE_SELECT) {
						app.setMode(app.MODE_SELECT);
					}
				}
			});

			lib.query('.b-mark').on('click', function() {
				app.setMode(app.mode == app.MODE_MARK ? app.MODE_DRAW : app.MODE_MARK);
			});

			lib.query('.b-undo').on('click', function() {
				if (map.undoPos > 0) {
					if (map.undoPos == map.undoBuffer.length) {
						if (map.addToUndo(map.getMapCopy(), map.undoBuffer[map.undoPos - 1])) {
							map.undoPos--;
						}
					}

					map.undoPos--;
					map.copyMapFrom(map.undoBuffer[map.undoPos]);
				}
			});

			lib.query('.b-redo').on('click', function() {
				if ((map.undoPos + 1) < map.undoBuffer.length) {
					map.undoPos++;
					map.copyMapFrom(map.undoBuffer[map.undoPos]);
				}
			});

			lib.query('.b-options').on('click', function() {
				var willActive = !lib.query(this).hasClass('active');
				lib.query(this).addRemoveClass(willActive, 'active');
				lib.query('.b-options-sub').toggle(willActive);
				file.hideSubDialog();
			});

			lib.query('.b-save-load').on('click', function() {
				var willActive = !lib.query(this).hasClass('active');
				lib.query(this).addRemoveClass(willActive, 'active');
				lib.query('.b-save-load-sub').toggle(willActive);
				options.hideSubDialog();
			});

			lib.query('.b-mark-clear').on('click', function() {
				lib.query('.b-mark-value')
					.set('value', '')
					.exec('focus');
			});
		}
	};
})();
