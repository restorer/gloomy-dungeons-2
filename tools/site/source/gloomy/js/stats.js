window.Stats = {
	healthElement: null,
	armorElement: null,
	ammoElement: null,

	createView: function() {
		Stats.healthElement = jQuery('<div class="stats" id="stats-health"></div>');
		Stats.armorElement = jQuery('<div class="stats" id="stats-armor"></div>');
		Stats.ammoElement = jQuery('<div class="stats" id="stats-ammo"></div>');

		var container = jQuery('#viewport');

		container.append(Stats.healthElement);
		container.append(Stats.armorElement);
		container.append(Stats.ammoElement);
	},

	renderSprite: function(idx, x, y) {
		var tmap = Tex.MAPS[1];
		var sx = (idx % tmap.cols) * tmap.wdt;
		var sy = ((idx / tmap.cols) | 0) * tmap.hgt;

		App.onscreenContext.drawImage(
			tmap.img,
			sx, sy, tmap.wdt, tmap.hgt,
			x, y, tmap.wdt, tmap.hgt
		);
	},

	render: function() {
		Stats.renderSprite(8, -16, -16);
		Stats.healthElement.html(Hero.health);

		Stats.renderSprite(9, -16 + 64, -16);
		Stats.armorElement.html(Hero.armor);

		if ((Weapons.currentParams.ammoIdx >= 0) && (Hero.ammo[Weapons.currentParams.ammoIdx] >= 0)) {
			Stats.renderSprite(10, -16 + 128, -16);
			Stats.ammoElement.html(Hero.ammo[Weapons.currentParams.ammoIdx]);
		} else {
			Stats.ammoElement.html('');
		}

		if (Hero.keysMask & 1) {
			Stats.renderSprite(11, -16, -16 + 32);
		}

		if (Hero.keysMask & 2) {
			Stats.renderSprite(12, -16 + 32, -16 + 32);
		}

		if (Hero.keysMask & 4) {
			Stats.renderSprite(13, -16 + 64, -16 + 32);
		}
	}
};
