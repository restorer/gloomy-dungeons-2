window.Overlay = {
	BLOOD: [ 1, 0, 0 ],
	ITEM: [ 1, 1, 1 ],
	MARK: [ 1, 1, 1 ],

	LABEL_CANT_OPEN: 'cant_open',
	LABEL_NEED_BLUE_KEY: 'need_blue_key',
	LABEL_NEED_RED_KEY: 'need_red_key',
	LABEL_NEED_GREEN_KEY: 'need_green_key',
	LABEL_SECRET_FOUND: 'secret_found',

	overlayColor: null,
	overlayTime: 0,
	shownLabelId: '',
	labelId: '',
	labelTime: 0,

	render: function(elapsedTime) {
		Overlay.renderOverlay(elapsedTime);
		Overlay.renderLabel(elapsedTime);

		if (Hero.nextLevelTime > 0) {
			Overlay.renderEndLevelLayer(elapsedTime);
		}
	},

	showLabel: function(labelId, elapsedTime) {
		Overlay.labelId = labelId;
		Overlay.labelTime = elapsedTime;
	},

	showOverlay: function(color, elapsedTime) {
		Overlay.overlayColor = color;
		Overlay.overlayTime = elapsedTime;
	},

	appendOverlayColor: function(res, c, a) {
		var d = res[3] + a - res[3] * a;

		if (d < 0.001) {
			return;
		}

		res[0] = (res[0] * res[3] - res[0] * res[3] * a + c[0] * a) / d;
		res[1] = (res[1] * res[3] - res[1] * res[3] * a + c[1] * a) / d;
		res[2] = (res[2] * res[3] - res[2] * res[3] * a + c[2] * a) / d;
		res[3] = d;
	},

	renderOverlay: function(elapsedTime) {
		var res = [ 0, 0, 0, 0 ];
		var bloodAlpha = Math.max(0.0, 0.4 - (Hero.health / 20.0) * 0.4);

		if (bloodAlpha > 0.0) {
			Overlay.appendOverlayColor(res, Overlay.BLOOD, bloodAlpha);
		}

		if (Overlay.overlayColor) {
			var alpha = 0.5 - (elapsedTime - Overlay.overlayTime) / 300.0;

			if (alpha > 0.0) {
				Overlay.appendOverlayColor(res, Overlay.overlayColor, alpha);
			} else {
				Overlay.overlayColor = null;
			}
		}

		if (res[3] < 0.001) {
			return;
		}

		res[0] = (res[0] * 255) | 0;
		res[1] = (res[1] * 255) | 0;
		res[2] = (res[2] * 255) | 0;

		App.offscreenContext.fillStyle = 'rgba(' + res[0] + ',' + res[1] + ',' + res[2] + ',' + res[3] + ')';
		App.offscreenContext.fillRect(0, 0, Engine.VIEWPORT_WIDTH, Engine.VIEWPORT_HEIGHT);
		App.offscreenContext.fillStyle = '#000000';
	},

	renderLabel: function(elapsedTime) {
		if (Overlay.labelId != '') {
			var op = Math.min(1.0, 3.0 - (elapsedTime - Overlay.labelTime) / 500.0);

			if (op <= 0) {
				App.labelElement.html('');
				Overlay.labelId = '';
				Overlay.shownLabelId = '';
			} else {
				if (Overlay.shownLabelId != Overlay.labelId) {
					App.labelElement.html(window.loc[Overlay.labelId]);
					Overlay.shownLabelId = Overlay.labelId;
				}

				App.labelElement.css('opacity', op);
			}
		} else if (Overlay.shownLabelId != '') {
			App.labelElement.html('');
			Overlay.shownLabelId = '';
		}
	},

	renderEndLevelLayer: function(elapsedTime) {
		var dt = (elapsedTime - Hero.nextLevelTime) / 500.0;

		var grd = App.offscreenContext.createLinearGradient(0, 0, 0, Engine.VIEWPORT_HEIGHT);
		grd.addColorStop(0.0, 'rgba(0,0,0,' + (Math.min(1.0, dt) * 0.9) + ')');
		grd.addColorStop(1.0, 'rgba(0,0,0,' + (Math.min(1.0, dt * 0.5) * 0.9) + ')');

		App.offscreenContext.fillStyle = grd;
		App.offscreenContext.fillRect(0, 0, Engine.VIEWPORT_WIDTH, Engine.VIEWPORT_HEIGHT);
		App.offscreenContext.fillStyle = '#000000';
	}
};
