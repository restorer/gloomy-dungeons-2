var map = (function() {
	var lmbPressed = false;
	var prevX = -1;
	var prevY = -1;
	var currentNoTrans = false;
	var currentArrow = 0;
	var tmpMarkValue = '';
	var preloadedImages = {};

	function doMove(x, y) {
		for (var i = selection.sy; i <= selection.ey; i++) {
			for (var j = selection.sx; j <= selection.ex; j++) {
				cell.clear(map.m[i][j], false);
			}
		}

		map.restoreFromBuffer(map.copyBuffer, x, y);
		map.update(selection.sx, selection.sy, selection.w, selection.h);
		map.update(x, y, selection.w, selection.h);

		selection.sx = x;
		selection.sy = y;
		selection.ex = x + selection.w - 1;
		selection.ey = y + selection.h - 1;

		selection.update();
	}

	function mapMouseEvent(event) {
		var x = Math.floor((event.pageX - map.wrapElement.offsetLeft + map.wrapElement.scrollLeft) * map.zoomMult / 64);
		var y = Math.floor((event.pageY - map.wrapElement.offsetTop + map.wrapElement.scrollTop) * map.zoomMult / 64);

		if (x < 0 || y < 0 || x >= map.MAX_WIDTH || y >= map.MAX_HEIGHT) {
			mapMouseOut();
			return false;
		}

		if ((app.mode == app.MODE_COPY || app.mode == app.MODE_MOVE) && selection.active) {
			if ((x + selection.w) >= map.MAX_WIDTH) {
				x = map.MAX_WIDTH - selection.w;
			}

			if ((y + selection.h) >= map.MAX_HEIGHT) {
				y = map.MAX_HEIGHT - selection.h;
			}
		}

		map.cursorElementStyle.top = (y * 64) + 'px';
		map.cursorElementStyle.left = (x * 64) + 'px';
		map.cursorElementStyle.display = 'block';

		if ((event.type == 'mousemove' && !(lmbPressed && (app.mode == app.MODE_DRAW || app.mode == app.MODE_SELECT || app.mode == app.MODE_MARK))) ||
			(prevX == x && prevY == y)
		) {
			return false;
		}

		prevX = x;
		prevY = y;

		if (app.mode == app.MODE_DRAW) {
			map.setCellValue(x, y);
			map.update(x, y, 1, 1);
		} else if (app.mode == app.MODE_MARK) {
			if (map.tmpMap == null) {
				map.tmpMap = map.getMapCopy();
				tmpMarkValue = String(lib.query('.b-mark-value').get('value')).trim();
			}

			map.m[y][x].mark = tmpMarkValue;
			map.update(x, y, 1, 1);
		} else if (app.mode == app.MODE_MOVE && selection.active) {
			doMove(x, y);
		} else if (app.mode == app.MODE_COPY && selection.active) {
			map.restoreFromBuffer(map.copyBuffer, x, y);
			map.update(x, y, selection.w, selection.h);
		} else if (app.mode == app.MODE_SELECT) {
			if (!lmbPressed && (!selection.active || selection.isFirstPoint)) {
				selection.active = true;
				selection.isFirstPoint = true;
				selection.tsx = x;
				selection.tsy = y;
				app.setMode(app.mode);
			}

			selection.tex = x;
			selection.tey = y;

			selection.sx = Math.min(selection.tsx, selection.tex);
			selection.sy = Math.min(selection.tsy, selection.tey);
			selection.ex = Math.max(selection.tsx, selection.tex);
			selection.ey = Math.max(selection.tsy, selection.tey);
			selection.w = selection.ex - selection.sx + 1;
			selection.h = selection.ey - selection.sy + 1;

			selection.update();
		}

		lmbPressed = true;
		return false;
	}

	function mapMouseOut(event) {
		map.cursorElementStyle.display = 'none';
		prevX = -1;
		prevY = -1;
	}

	function mapMouseUp(event) {
		lmbPressed = false;
		prevX = -1;
		prevY = -1;

		if (selection.active) {
			selection.isFirstPoint = (selection.sx != selection.ex || selection.sy != selection.ey || !selection.isFirstPoint);
		}

		if (map.tmpMap != null) {
			map.addToUndo(map.tmpMap);
			map.tmpMap = null;
		}

		return false;
	}

	function fillPreloadedImage(path, dli, drawList, preloadList) {
		if (preloadedImages[path] && preloadedImages[path].loaded) {
			var pitem = preloadedImages[path];

			dli.img = pitem.img;
			dli.iw = pitem.w;
			dli.ih = pitem.h;

			drawList.push(dli);
		} else {
			preloadList.push(path);
		}
	}

	function addCallback(cbList, cbItem) {
		var found = false;

		for (var i = 0; i < cbList.length; i++) {
			if (cbList[i].callback == cbItem.callback) {
				found = true;
				break;
			}
		}

		if (!found) {
			cbList.push(cbItem);
		}
	}

	function preloadImages(preloadList, callback) {
		for (var i = 0; i < preloadList.length; i++) {
			var path = preloadList[i];

			if (preloadedImages[path]) {
				addCallback(preloadedImages[path].cbList, { ensure:preloadList, callback:callback });
			} else {
				var pitem = {
					loaded: false,
					cbList: [ { ensure:preloadList, callback:callback } ]
				};

				var onLoadCb = function(_pitem) {
					return function() {
						_pitem.loaded = true;
						_pitem.w = _pitem.img.width;
						_pitem.h = _pitem.img.height;

						var cbList = _pitem.cbList;

						for (var i = 0; i < cbList.length; i++) {
							var succ = true;
							var ensureList = cbList[i].ensure;

							for (var j = 0; j < ensureList.length; j++) {
								var ensurePath = ensureList[j];

								if (!preloadedImages[ensurePath]) {
									succ = false;
									alert('Preload error');
								} else if (!preloadedImages[ensurePath].loaded) {
									addCallback(preloadedImages[ensurePath].cbList, cbList[i]);
								}
							}

							if (succ) {
								cbList[i].callback();
							}
						}

						_pitem.cbList = null;
						_pitem.cbList = [];
					}
				}(pitem);

				var onErrorCb = function(_path, _pitem) {
					return function() {
						_pitem.img = null;

						setTimeout(function() {
							_pitem.img = new Image();
							_pitem.img.onload = onLoadCb;
							_pitem.img.onerror = onErrorCb;
							_pitem.img.src = path;
						}, 100);
					}
				}(path, pitem);

				preloadedImages[path] = pitem;

				pitem.img = new Image();
				pitem.img.onload = onLoadCb;
				pitem.img.onerror = onErrorCb;
				pitem.img.src = path;
			}
		}
	}

	function updateCell(px, py, item) {
		var drawList = [];
		var preloadList = [];

		if (cell.isShownAsEmpty(item)) {
			fillPreloadedImage(paths.list[app.T_EMPTY][0], { x:0, y:0, w:64, h:64 }, drawList, preloadList);
		} else {
			if (map.renderLowerType) {
				var list = item[map.renderLowerName];
				var pathsFloor = paths.list[map.renderLowerType];

				if (list[0] != 0) {
					fillPreloadedImage(pathsFloor[list[0]], { x:0, y:0, w:32, h:32 }, drawList, preloadList);
				}

				if (list[1] != 0) {
					fillPreloadedImage(pathsFloor[list[1]], { x:32, y:0, w:32, h:32 }, drawList, preloadList);
				}

				if (list[2] != 0) {
					fillPreloadedImage(pathsFloor[list[2]], { x:0, y:32, w:32, h:32 }, drawList, preloadList);
				}

				if (list[3] != 0) {
					fillPreloadedImage(pathsFloor[list[3]], { x:32, y:32, w:32, h:32 }, drawList, preloadList);
				}
			}

			if (item.arrow != 0) {
				fillPreloadedImage(paths.list[app.T_ARROW][item.arrow], { x:0, y:0, w:64, h:64 }, drawList, preloadList);
			}

			if (item.noTrans) {
				fillPreloadedImage(paths.list[app.T_NO_TRANS][0], { x:0, y:0, w:64, h:64 }, drawList, preloadList);
			}

			if (map.renderWalls && item.type != 0 && item.value != 0) {
				fillPreloadedImage(paths.list[item.type][item.value], { x:0, y:0, w:64, h:64 }, drawList, preloadList);
			}

			if (map.renderUpperType) {
				var list = item[map.renderUpperName];
				var pathsFloor = paths.list[map.renderUpperType];

				if (list[0] != 0) {
					fillPreloadedImage(pathsFloor[list[0]], { x:0, y:0, w:32, h:32 }, drawList, preloadList);
				}

				if (list[1] != 0) {
					fillPreloadedImage(pathsFloor[list[1]], { x:32, y:0, w:32, h:32 }, drawList, preloadList);
				}

				if (list[2] != 0) {
					fillPreloadedImage(pathsFloor[list[2]], { x:0, y:32, w:32, h:32 }, drawList, preloadList);
				}

				if (list[3] != 0) {
					fillPreloadedImage(pathsFloor[list[3]], { x:32, y:32, w:32, h:32 }, drawList, preloadList);
				}
			}
		}

		if (preloadList.length) {
			map.canvasContext.fillStyle = '#400';
			map.canvasContext.fillRect(px, py, 64, 64);

			preloadImages(preloadList, function() {
				updateCell(px, py, item);
			});

			return;
		}

		map.canvasContext.fillStyle = '#000';
		map.canvasContext.fillRect(px, py, 64, 64);

		for (var i = 0; i < drawList.length; i++) {
			var dli = drawList[i];

			var xm = dli.iw / 64;
			var ym = dli.ih / 64;

			map.canvasContext.drawImage(
				dli.img,
				dli.x * xm,
				dli.y * ym,
				dli.w * xm,
				dli.h * ym,
				px + dli.x,
				py + dli.y,
				dli.w,
				dli.h
			);
		}

		if (item.mark != '') {
			map.canvasContext.strokeStyle = '#000';
			map.canvasContext.lineWidth = 4;
			map.canvasContext.strokeText(item.mark, px + 32, py + 32, 64);

			map.canvasContext.fillStyle = '#F00';
			map.canvasContext.fillText(item.mark, px + 32, py + 32, 64);
		}
	}

	return {
		MAX_WIDTH: 64,
		MAX_HEIGHT: 64,

		m: [],
		tmpMap: null,
		copyBuffer: null,
		undoBuffer: [],
		undoPos: 0,
		zoomMult: 2,
		zoomCellSize: 32,
		mapWrapElement: null,
		cursorElementStyle: null,
		renderLowerType: null,
		renderLowerName: null,
		renderWalls: true,
		renderUpperType: null,
		renderUpperName: null,
		canvasElement: null,
		canvasContext: null,

		init: function() {
			var html = '';
			map.m = [];

			for (var i = 0; i < map.MAX_HEIGHT; i++) {
				var line = [];

				for (var j = 0; j < map.MAX_WIDTH; j++) {
					line.push(cell.create());
				}

				map.m.push(line);
			}

			var html = '<canvas id="map-canvas" width="4096" height="4096"></canvas>';
			html += '<div class="selection" style="display:none;"></div>';
			html += '<div class="cursor" style="width:64px;height:64px;display:none;"></div>';

			lib.query('.map').set('innerHTML', html);

			map.canvasElement = lib.query('#map-canvas').dom();
			map.canvasContext = map.canvasElement.getContext('2d');
			map.wrapElement = lib.query('.map-wrap').dom();
			map.cursorElementStyle = lib.query('.cursor').dom().style;

			lib.query('.map')
				.on('mousedown', mapMouseEvent)
				.on('mousemove', mapMouseEvent);

			document.onmouseup = mapMouseUp;
			map.wrapElement.onmouseout = mapMouseOut;

			map.renderLowerType = app.T_FLOOR;
			map.renderLowerName = 'floor';

			lib.query('.b-map-rendermode').on('click', function() {
				lib.query('.b-map-rendermode').removeClass('active');
				lib.query(this).addClass('active');

				switch (this.getAttribute('rel')) {
					case 'f':
						map.renderLowerType = app.T_FLOOR;
						map.renderLowerName = 'floor';
						map.renderWalls = false;
						map.renderUpperType = null;
						map.renderUpperName = null;
						break;

					case 'fw':
						map.renderLowerType = app.T_FLOOR;
						map.renderLowerName = 'floor';
						map.renderWalls = true;
						map.renderUpperType = null;
						map.renderUpperName = null;
						break;

					case 'cw':
						map.renderLowerType = app.T_CEIL;
						map.renderLowerName = 'ceil';
						map.renderWalls = true;
						map.renderUpperType = null;
						map.renderUpperName = null;
						break;

					case 'wc':
						map.renderLowerType = null;
						map.renderLowerName = null;
						map.renderWalls = true;
						map.renderUpperType = app.T_CEIL;
						map.renderUpperName = 'ceil';
						break;

					case 'c':
						map.renderLowerType = app.T_CEIL;
						map.renderLowerName = 'ceil';
						map.renderWalls = false;
						map.renderUpperType = null;
						map.renderUpperName = null;
						break;
				}

				map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);
			});

			map.canvasContext.font = 'bold 24px Tahoma,Arial';
			map.canvasContext.textAlign = 'center';
			map.canvasContext.textBaseline = 'middle';

			map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);
		},

		isColumnEmpty: function(col) {
			for (var i = 0; i < map.MAX_HEIGHT; i++) {
				if (!cell.isEmpty(map.m[i][col])) {
					return false;
				}
			}

			return true;
		},

		isRowEmpty: function(row) {
			for (var i = 0; i < map.MAX_WIDTH; i++) {
				if (!cell.isEmpty(map.m[row][i])) {
					return false;
				}
			}

			return true;
		},

		copyToBuffer: function(sx, sy, ex, ey) {
			var res = [];

			for (var i = sy; i <= ey; i++) {
				var line = [];

				for (var j = sx; j <= ex; j++) {
					line.push(cell.getCopy(map.m[i][j]));
				}

				res.push(line);
			}

			return res;
		},

		restoreFromBuffer: function(buf, x, y, undoMap) {
			if (buf == null) {
				return;
			}

			if (!undoMap) {
				undoMap = map.getMapCopy();
			}

			for (var i = 0; i < buf.length; i++) {
				var my = i + y;

				if (my >= 0 && my < map.MAX_HEIGHT) {
					var line = buf[i];

					for (var j = 0; j < line.length; j++) {
						var mx = j + x;

						if (mx >= 0 && mx < map.MAX_WIDTH) {
							cell.copyToFrom(map.m[my][mx], line[j]);
						}
					}
				}
			}

			map.addToUndo(undoMap);
			map.tmpMap = null;
		},

		getMapCopy: function() {
			var res = [];

			for (var i = 0; i < map.MAX_HEIGHT; i++) {
				var list = [];

				for (var j = 0; j < map.MAX_WIDTH; j++) {
					list.push(cell.getCopy(map.m[i][j]));
				}

				res.push(list);
			}

			return res;
		},

		copyMapFrom: function(fromMap) {
			for (var i = 0; i < map.MAX_HEIGHT; i++) {
				for (var j = 0; j < map.MAX_WIDTH; j++) {
					cell.copyToFrom(map.m[i][j], fromMap[i][j]);
				}
			}

			map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);
			map.updateUndoRedoButtons();
		},

		centerViewAt: function(x, y) {
			map.wrapElement.scrollLeft = (x - Math.floor(map.wrapElement.offsetWidth / map.zoomCellSize / 2)) * map.zoomCellSize;
			map.wrapElement.scrollTop = (y - Math.floor(map.wrapElement.offsetHeight / map.zoomCellSize / 2)) * map.zoomCellSize;
		},

		update: function(x, y, w, h) {
			var ex = x + w;
			var ey = y + h;

			for (var i = y; i < ey; i++) {
				for (var j = x; j < ex; j++) {
					updateCell(j * 64, i * 64, map.m[i][j]);
				}
			}
		},

		updateUndoRedoButtons: function() {
			lib.query('.b-undo').addRemoveClass((map.undoPos <= 0), 'disabled');
			lib.query('.b-redo').addRemoveClass(((map.undoPos + 1) >= map.undoBuffer.length), 'disabled');
		},

		addToUndo: function(undoMap, compareToMap) {
			var differs = false;

			if (!compareToMap) {
				compareToMap = map.m;
			}

			outer: for (var i = 0; i < map.MAX_HEIGHT; i++) {
				for (var j = 0; j < map.MAX_WIDTH; j++) {
					if (!cell.equals(compareToMap[i][j], undoMap[i][j])) {
						differs = true;
						break outer;
					}
				}
			}

			if (!differs) {
				return false;
			}

			if (map.undoBuffer.length > map.undoPos + 1) {
				map.undoBuffer.splice(map.undoPos);
			}

			if (map.undoBuffer.length > 64) {
				map.undoBuffer.splice(0, map.undoBuffer.length - 64);
			}

			map.undoBuffer.push(undoMap);
			map.undoPos = map.undoBuffer.length;

			map.updateUndoRedoButtons();
			return true;
		},

		setCellValue: function(x, y) {
			if (app.selectedTool == null) {
				return;
			}

			var item = map.m[y][x];

			if (app.selectedTool.typeA == app.T_EMPTY) {
				if (!cell.isShownAsEmpty(item)) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
					}

					cell.clear(item);
				}
			} else if (app.selectedTool.typeA == app.T_JUST_FLOOR) {
				if (item.type != 0) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
					}

					item.type = 0;
					item.value = 0;
				}
			} else if (app.selectedTool.typeA == app.T_NO_TRANS) {
				if (!cell.isShownAsEmpty(item)) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
						currentNoTrans = !item.noTrans;
					}

					item.noTrans = currentNoTrans;
				}
			} else if (app.selectedTool.typeA == app.T_ARROW) {
				if (!cell.isShownAsEmpty(item)) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
						currentArrow = (item.arrow != 0 ? 0 : app.selectedTool.valueA);
					}

					item.arrow = currentArrow;
				}
			} else if (app.selectedTool.typeA == app.T_FLOOR) {
				if (item.floor[0] != app.selectedTool.valueA
					|| item.floor[1] != app.selectedTool.valueA
					|| item.floor[2] != app.selectedTool.valueA
					|| item.floor[3] != app.selectedTool.valueA
				) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
					}

					item.floor = [ app.selectedTool.valueA, app.selectedTool.valueA, app.selectedTool.valueA, app.selectedTool.valueA ];
				}
			} else if (app.selectedTool.typeA == app.T_CEIL) {
				if (item.ceil[0] != app.selectedTool.valueA
					|| item.ceil[1] != app.selectedTool.valueA
					|| item.ceil[2] != app.selectedTool.valueA
					|| item.ceil[3] != app.selectedTool.valueA
				) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
					}

					item.ceil = [ app.selectedTool.valueA, app.selectedTool.valueA, app.selectedTool.valueA, app.selectedTool.valueA ];
				}
			} else if (app.selectedTool.typeA == app.T_NO_CEIL) {
				if (item.ceil[0] != 0
					|| item.ceil[1] != 0
					|| item.ceil[2] != 0
					|| item.ceil[3] != 0
				) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
					}

					item.ceil = [ 0, 0, 0, 0 ];
				}
			} else if (app.selectedTool.typeA == app.T_FLOOR_DISP) {
				if (item.floor[0] != app.specialFloor[0]
					|| item.floor[1] != app.specialFloor[1]
					|| item.floor[2] != app.specialFloor[2]
					|| item.floor[3] != app.specialFloor[3]
				) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
					}

					item.floor = [ app.specialFloor[0], app.specialFloor[1], app.specialFloor[2], app.specialFloor[3] ];
				}
			} else if (app.selectedTool.typeA == app.T_CEIL_DISP) {
				if (item.ceil[0] != app.specialCeil[0]
					|| item.ceil[1] != app.specialCeil[1]
					|| item.ceil[2] != app.specialCeil[2]
					|| item.ceil[3] != app.specialCeil[3]
				) {
					if (map.tmpMap == null) {
						map.tmpMap = map.getMapCopy();
					}

					item.ceil = [ app.specialCeil[0], app.specialCeil[1], app.specialCeil[2], app.specialCeil[3] ];
				}
			} else if ((item.floor[0] != 0
				|| item.floor[1] != 0
				|| item.floor[2] != 0
				|| item.floor[3] != 0
				) && (item.type != app.selectedTool.typeA || item.value != app.selectedTool.valueA)
			) {
				if (map.tmpMap == null) {
					map.tmpMap = map.getMapCopy();
				}

				item.type = app.selectedTool.typeA;
				item.value = app.selectedTool.valueA;
			}
		},

		getBounds: function() {
			var sx, sy, ex, ey;

			for (sx = 0; sx < 64; sx++) {
				if (!map.isColumnEmpty(sx)) {
					break;
				}
			}

			for (ex = 63; ex >= 0; ex--) {
				if (!map.isColumnEmpty(ex)) {
					break;
				}
			}

			for (sy = 0; sy < 64; sy++) {
				if (!map.isRowEmpty(sy)) {
					break;
				}
			}

			for (ey = 63; ey >= 0; ey--) {
				if (!map.isRowEmpty(ey)) {
					break;
				}
			}

			if (sx > ex || sy > ey) {
				return null;
			}

			return {
				sx: sx,
				sy: sy,
				ex: ex,
				ey: ey,
				w: (ex - sx + 1),
				h: (ey - sy + 1)
			};
		}
	};
})();
