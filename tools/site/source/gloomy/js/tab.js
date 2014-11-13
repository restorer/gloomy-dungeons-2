window.Tab = {
	sin: [],
	cos: [],
	corr: [],
	sinDivAbsCos: [],
	cosDivAbsSin: [],
	colAng: [],

	init: function() {
		for (var i = 0; i < Engine.ANG_360; i++) {
			Tab.sin[i] = Math.sin(i * Math.PI / (Engine.ANG_360 / 2));
			Tab.cos[i] = Math.cos(i * Math.PI / (Engine.ANG_360 / 2));

			if (Math.abs(Tab.sin[i]) < Engine.INFINITY) {
				Tab.sin[i] = 0;
			}

			if (Math.abs(Tab.cos[i]) < Engine.INFINITY) {
				Tab.cos[i] = 0;
			}

			Tab.sinDivAbsCos[i] = (Tab.cos[i] == 0 ? 0 : Tab.sin[i] / Math.abs(Tab.cos[i]));
			Tab.cosDivAbsSin[i] = (Tab.sin[i] == 0 ? 0 : Tab.cos[i] / Math.abs(Tab.sin[i]));
		}

		for (var i = 0; i < Engine.VIEWPORT_WIDTH; i++) {
			Tab.corr[i] = Tab.cos[ Math.round(Engine.ANG_30 - (i * Engine.ANG_60 / Engine.VIEWPORT_WIDTH) + Engine.ANG_360) % Engine.ANG_360 ];
			Tab.colAng[i] = Math.floor(Engine.ANG_30 - (i * Engine.ANG_60 / Engine.VIEWPORT_WIDTH) + Engine.ANG_360);
		}
	}
};
