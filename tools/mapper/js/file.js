var file = (function() {
	var active = false;
	var levelsList = [];

	function refreshLevelsList(prevSelected) {
		if (active) {
			return;
		}

		active = true;
		levelsList = [];

		if (!window.SERVER_URL) {
			lib.query('#levels-list').set('innerHTML', '<option value="">SERVER_URL not configured</option>');
			return;
		}

		if (!prevSelected) {
			prevSelected = lib.query('#levels-list').get('value');
		}

		lib.query('#levels-list')
			.set('disabled', true)
			.set('innerHTML', '<option value="">Loading ...</option>');

		lib.ajax({
			url: window.SERVER_URL,
			data: {
				mode: 'list'
			},
			callback: function(data) {
				active = false;

				if (data == null) {
					alert('Server error');
				} else if (data.error) {
					alert(data.error);
				} else {
					levelsList = data.data;

					if (levelsList.length) {
						var html = '';

						for (var i = 0; i < levelsList.length; i++) {
							html += '<option value="' + lib.htmlEscape(levelsList[i]) + '"';

							if (prevSelected == levelsList[i]) {
								html += ' selected="selected"';
							}

							html += '>' + lib.htmlEscape(levelsList[i]) + '</option>';
						}

						lib.query('#levels-list')
							.set('disabled', false)
							.set('innerHTML', html);
					} else {
						lib.query('#levels-list').set('innerHTML', '<option value="">Empty list</option>');
					}
				}
			}
		});
	}

	function loadLevel() {
		var name = lib.query('#levels-list').get('value');

		if (active || name == '' || name == null || !window.SERVER_URL) {
			return;
		}

		active = true;

		lib.ajax({
			url: SERVER_URL,
			data: {
				mode: 'load',
				name: name
			},
			callback: function(data) {
				active = false;

				if (data == null) {
					alert('Server error');
				} else if (data.error) {
					alert(data.error);
				} else {
					data = updateDataFormat(data.data);
					paths.changeGraphicsSet(data.graphics);

					lib.query('#actions').set('value', data.actions);
					lib.query('#graphics-set').set('value', data.graphics);
					lib.query('#ensure').set('value', data.ensureLevel ? data.ensureLevel : 2);
					lib.query('#difficulty').set('value', data.difficultyLevel ? data.difficultyLevel : 1);

					var undoMap = map.getMapCopy();

					for (var i = 0; i < map.MAX_HEIGHT; i++) {
						for (var j = 0; j < map.MAX_WIDTH; j++) {
							cell.clear(map.m[i][j]);
						}
					}

					map.restoreFromBuffer(data.map, data.xpos, data.ypos, undoMap);
					map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);

					map.centerViewAt(data.xpos + Math.floor(data.map[0].length / 2), data.ypos + Math.floor(data.map.length / 2));
					file.hideSubDialog();

					lib.query('#save-level-name').set('value', lib.query('#levels-list').get('value'));
				}
			}
		});
	}

	function updateDataFormat(data) {
		if (data.format != 1) {
			for (var i = 0; i < data.map.length; i++) {
				var line = data.map[i];

				for (var j = 0; j < line.length; j++) {
					var item = line[j];

					if (item.floor < 3) {
						item.floor = [ item.floor, item.floor, item.floor, item.floor ];
						item.arrow = 0;
					} else if (item.floor == 3) {
						item.floor = [ 1, 2, 1, 2 ];
						item.arrow = 0;
					} else if (item.floor == 4) {
						item.floor = [ 2, 1, 2, 1 ];
						item.arrow = 0;
					} else if (item.floor == 5) {
						item.floor = [ 1, 1, 2, 2 ];
						item.arrow = 0;
					} else if (item.floor == 6) {
						item.floor = [ 2, 2, 1, 1 ];
						item.arrow = 0;
					} else if (item.floor == 7) {
						item.floor = [ 1, 1, 1, 1 ];
						item.arrow = 1;
					} else if (item.floor == 8) {
						item.floor = [ 1, 1, 1, 1 ];
						item.arrow = 2;
					} else if (item.floor == 9) {
						item.floor = [ 1, 1, 1, 1 ];
						item.arrow = 3;
					} else if (item.floor == 10) {
						item.floor = [ 1, 1, 1, 1 ];
						item.arrow = 4;
					} else if (item.floor == 11) {
						item.floor = [ 2, 2, 2, 2 ];
						item.arrow = 1;
					} else if (item.floor == 12) {
						item.floor = [ 2, 2, 2, 2 ];
						item.arrow = 2;
					} else if (item.floor == 13) {
						item.floor = [ 2, 2, 2, 2 ];
						item.arrow = 3;
					} else if (item.floor == 14) {
						item.floor = [ 2, 2, 2, 2 ];
						item.arrow = 4;
					}

					if (item.ceil == 0) {
						item.ceil = [ 0, 0, 0, 0 ];
					} else if (item.ceil < 3) {
						item.ceil = [ item.ceil - 1, item.ceil - 1, item.ceil - 1, item.ceil - 1 ];
					} else if (item.ceil == 3) {
						item.ceil = [ 0, 1, 0, 1 ];
					} else if (item.ceil == 4) {
						item.ceil = [ 1, 0, 1, 0 ];
					} else if (item.ceil == 5) {
						item.ceil = [ 0, 0, 1, 1 ];
					} else if (item.ceil == 6) {
						item.ceil = [ 1, 1, 0, 0 ];
					}
				}
			}
		}

		if (data.format != 2) {
			for (var i = 0; i < data.map.length; i++) {
				var line = data.map[i];

				for (var j = 0; j < line.length; j++) {
					var item = line[j];

					if (item.ceil[0] < 0) {
						item.ceil[0] = 0;
					}

					if (item.ceil[1] < 0) {
						item.ceil[1] = 0;
					}

					if (item.ceil[2] < 0) {
						item.ceil[2] = 0;
					}

					if (item.ceil[3] < 0) {
						item.ceil[3] = 0;
					}
				}
			}
		}

		return data;
	}

	function saveLevel() {
		var name = String(lib.query('#save-level-name').get('value')).trim();
		var bounds = map.getBounds();

		if (active || name == '' || bounds == null || !window.SERVER_URL) {
			return;
		}

		active = true;

		var ensureLevel = parseInt(lib.query('#ensure').dom().value, 10);
		var difficultyLevel = parseFloat(lib.query('#difficulty').dom().value);

		lib.ajax({
			url: SERVER_URL,
			data: {
				mode: 'save',
				name: name,
				data: {
					format: 2,
					xpos: bounds.sx,
					ypos: bounds.sy,
					map: map.copyToBuffer(bounds.sx, bounds.sy, bounds.ex, bounds.ey),
					graphics: paths.graphicsSet,
					ensureLevel: ensureLevel,
					difficultyLevel: difficultyLevel,
					actions: lib.query('#actions').get('value')
				}
			},
			callback: function(data) {
				active = false;

				if (data == null) {
					alert('Server error');
				} else if (data.error) {
					alert(data.error);
				} else {
					refreshLevelsList(name);
					file.hideSubDialog();
				}
			}
		});
	}

	return {
		init: function() {
			refreshLevelsList();

			lib.query('.b-refresh-list').on('click', function() { refreshLevelsList(); });
			lib.query('.b-load-level').on('click', loadLevel);
			lib.query('.b-save-level').on('click', saveLevel);

			lib.query('.b-clear-level').on('click', function() {
				var undoMap = map.getMapCopy();

				for (var i = 0; i < map.MAX_HEIGHT; i++) {
					for (var j = 0; j < map.MAX_WIDTH; j++) {
						cell.clear(map.m[i][j]);
					}
				}

				map.addToUndo(undoMap);
				map.tmpMap = null;

				map.update(0, 0, map.MAX_WIDTH, map.MAX_HEIGHT);
				file.hideSubDialog();
			});

			lib.query('#levels-list').on('change', function() {
				lib.query('#save-level-name').set('value', this.value);
				loadLevel();
			});
		},

		hideSubDialog: function() {
			lib.query('.b-save-load').removeClass('active');
			lib.query('.b-save-load-sub').hide();
		}
	};
})();
