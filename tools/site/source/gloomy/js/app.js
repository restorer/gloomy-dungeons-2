// http://oisyn.nl/wolfjs/game.js
// view-source:http://devfiles.myopera.com/articles/650/step_4_enemies.htm

window.realRequestAnimationFrame = window.requestAnimationFrame ||
	window.mozRequestAnimationFrame ||
	window.webkitRequestAnimationFrame ||
	window.msRequestAnimationFrame;

window.App = {
	MOVE_FREQ: 20,

	onscreenWidth: 0,
	onscreenHeight: 0,
	onscreenCanvas: null,
	onscreenContext: null,
	offscreenCanvas: null,
	offscreenContext: null,
	infoElement: null,
	labelElement: null,
	startTime: 0,
	prevTime: 0,
	isGameOverFlag: false,

	init: function() {
		Controls.init();
		Engine.init();
		Hero.init();
		Level.init();
		Tab.init();
		Tex.init();
		Weapons.init();
	},

	error: function(msg) {
		jQuery('#viewport').html('<span style="color:#F00;font-size:24px;">' + msg + '</span>');
	},

	createView: function() {
		var container = jQuery('#viewport');
		App.onscreenWidth = container.width();
		App.onscreenHeight = container.height();

		var offscreenCanvasElement = jQuery([
			'<canvas width="', Engine.VIEWPORT_WIDTH,
			'" height="', Engine.VIEWPORT_HEIGHT,
			'"></canvas>'
		].join(''));

		var onscreenCanvasElement = jQuery([
			'<canvas width="', App.onscreenWidth,
			'" height="', App.onscreenHeight,
			'" style="width:', App.onscreenWidth,
			'px;height:', App.onscreenHeight,
			'px;"></canvas>'
		].join(''));

		App.offscreenCanvas = offscreenCanvasElement.get(0);
		App.offscreenContext = App.offscreenCanvas.getContext('2d');

		App.onscreenCanvas = onscreenCanvasElement.get(0);
		App.onscreenContext = App.onscreenCanvas.getContext('2d');

		container.html('');
		container.append(onscreenCanvasElement);

		App.labelElement = jQuery('<div class="label"></div>');
		container.append(App.labelElement);

		App.infoElement = jQuery('<div class="log"></div>');
		container.append(App.infoElement);

		// container.append(jQuery('<div style="position:absolute;top:0;bottom:0;left:250px;width:1px;overflow:hidden;background:red;"></div>'));
	},

	log: function(msg) {
		App.infoElement.html(App.infoElement.html() + msg + '<br />');
		App.infoElement.scrollTop(App.infoElement.get(0).scrollHeight - App.infoElement.height());
	},

	update: function(elapsedTime) {
		Controls.update();
		Weapons.update(elapsedTime);
	},

	tick: function(elapsedTime) {
		Hero.tick(elapsedTime);
		Weapons.tick(elapsedTime);

		for (var i = 0; i < Level.monsters.length; i++) {
			Level.monsters[i].tick(elapsedTime);
		}

		for (var i = 0; i < Level.doors.length; i++) {
			Level.doors[i].tick();
		}
	},

	render: function(elapsedTime) {
		var walkTime = 0;

		if (Hero.hasMoved) {
			if (prevMovedTime != 0) {
				walkTime = elapsedTime - prevMovedTime;
			} else {
				prevMovedTime = elapsedTime;
			}
		} else {
			prevMovedTime = 0;
		}

		var yoff = Math.sin(walkTime / 100.0) * (Engine.VIEWPORT_HEIGHT / 50.0);

		if (Hero.killedTime > 0) {
			Hero.a = ((Hero.killedHeroAngle + (Hero.killedAngle - Hero.killedHeroAngle) * Math.min(1, (elapsedTime - Hero.killedTime) / 1000) + Engine.ANG_720) | 0) % Engine.ANG_360;
		}

		Engine.render(elapsedTime, yoff | 0);
		Weapons.render(elapsedTime, walkTime);
		Overlay.render(elapsedTime);

		App.onscreenContext.drawImage(
			App.offscreenCanvas,
			0, 0, Engine.VIEWPORT_WIDTH, Engine.VIEWPORT_HEIGHT,
			0, 0, App.onscreenWidth, App.onscreenHeight
		);

		Stats.render();

		if (Hero.nextLevelTime > 0) {
			if (elapsedTime - Hero.nextLevelTime > 1000) {
				if (App.isGameOverFlag) {
					App.showGameOverScreen();
				} else {
					App.showEndLevelScreen();
				}
			}
		} else if ((Hero.killedTime > 0) && (elapsedTime - Hero.killedTime > 3500)) {
			App.isGameOverFlag = true;
			Hero.nextLevelTime = elapsedTime;
		}
	},

	process: function() {
		App.infoElement.html('');

		var time = (new Date()).valueOf();
		var elapsedTime = time - App.startTime;
		var loopCnt = Math.floor((time - App.prevTime) / App.MOVE_FREQ);

		App.update(elapsedTime);

		if (loopCnt > 0) {
			App.prevTime += (loopCnt * App.MOVE_FREQ);

			if (loopCnt > 32) {
				loopCnt = 32;
			}

			while (loopCnt > 0) {
				App.tick(elapsedTime);
				loopCnt--;
			}
		}

		App.render(elapsedTime);

		if (typeof(realRequestAnimationFrame) == 'function') {
			realRequestAnimationFrame(App.process);
		} else {
			setTimeout(App.process, 16);
		}
	},

	showGameOverScreen: function() {
		App.error('Game Over');
	},

	showEndLevelScreen: function() {
		App.error('End Level');
	},

	start: function() {
		App.infoElement.html('');
		Engine.executeActions(0, 0);
		App.startTime = (new Date()).valueOf();
		App.prevTime = App.startTime;
		App.process();
	},

	load: function() {
		App.createView();
		Stats.createView();
		Controls.bind();

		Level.load(4, function() {
			Tex.load(function() {
				setTimeout(App.start, 1);
			});
		});
	}
};

jQuery(function() {
	App.init();
	jQuery('#start-gloomy').click(function(){ App.load(); });
});
