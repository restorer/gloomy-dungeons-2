window.Monster = function(x, y, dir, tmap, health, hits, hitType) {
	this.cellX = x;
	this.cellY = y;
	this.x = x;
	this.y = y;
	this.dir = dir;
	this.tmap = tmap;
	this.health = health;
	this.hits = hits;

	this.setAttackDist(hitType != Level.HIT_TYPE_EAT);

	if (hitType == Level.HIT_TYPE_PIST) {
		this.ammoType = Tex.OBJ_CLIP;
	} else if (hitType == Level.HIT_TYPE_SHTG) {
		this.ammoType = Tex.OBJ_SHELL;
	} else { // HIT_TYPE_EAT
		this.ammoType = 0;
	}

	this.renderWeight = 0.1;
	this.step = 0;
	this.maxStep = 50;
	this.hitTimeout = 0;
	this.attackTimeout = 0;
	this.dieTime = 0;
	this.aroundReqDir = -1;
	this.inverseRotation = false;
	this.prevAroundX = -1;
	this.prevAroundY = -1;
	this.visibleDistSq = 15.0 * 15.0;
	this.hitHeroTimeout = 0;
	this.chaseMode = false;
	this.waitForDoor = false;

	this.isInAttackState = false;
	this.isAimedOnHero = false;
	this.prevX = this.cellX;
	this.prevY = this.cellY;
	this.shootAngle = 0;
	this.hitHeroHits = 0;
};

Monster.prototype.setAttackDist = function(longAttackDist) {
	this.attackDistSq = (longAttackDist ? (10.0 * 10.0) : (1.8 * 1.8));
};

Monster.prototype.hit = function(amt, hitTm) {
	this.hitTimeout = hitTm;
	this.health -= amt;
	this.aroundReqDir = -1;

	if (this.health <= 0) {
		Level.passableMap[this.cellY][this.cellX] &= ~Level.PASSABLE_IS_MONSTER;
		Level.passableMap[this.cellY][this.cellX] |= Level.PASSABLE_IS_DEAD_CORPSE;

		if (this.ammoType > 0) {
			if ((Level.passableMap[this.cellY][this.cellX] & Level.PASSABLE_MASK_OBJECT_DROP) == 0) {
				Level.objectsMap[this.cellY][this.cellX] = new GObject(this.cellX, this.cellY, this.ammoType, false);
				Level.passableMap[this.cellY][this.cellX] |= Level.PASSABLE_IS_OBJECT;
			} else {
				outer: for (var dy = -1; dy <= 1; dy++) {
					for (var dx = -1; dx <= 1; dx++) {
						if (
							((dy != 0) || (dx != 0)) &&
							((Level.passableMap[this.cellY + dy][this.cellX + dx] & Level.PASSABLE_MASK_OBJECT_DROP) == 0)
						) {
							Level.objectsMap[this.cellY + dy][this.cellX + dx] = new GObject(this.cellX + dx, this.cellY + dy, this.ammoType, false);
							Level.passableMap[this.cellY + dy][this.cellX + dx] |= Level.PASSABLE_IS_OBJECT;
							break outer;
						}
					}
				}
			}
		}

		Hero.killedMonsters++;
	}
};

Monster.prototype.tick = function(elapsedTime) {
	if (this.health <= 0) {
		return;
	}

	if (this.hitHeroTimeout > 0) {
		this.hitHeroTimeout--;

		if (this.hitHeroTimeout <= 0) {
			Hero.hit(this.hitHeroHits, this, elapsedTime);
		}
	}

	if (this.step <= 0) {
		var tryAround = false;

		this.isInAttackState = false;
		this.isAimedOnHero = false;
		this.prevX = this.cellX;
		this.prevY = this.cellY;

		var dx = Hero.x - (this.cellX + 0.5);
		var dy = Hero.y - (this.cellY + 0.5);
		var distSq = dx*dx + dy*dy;

		if (this.aroundReqDir >= 0) {
			if (!this.waitForDoor) {
				this.dir = (this.dir + (this.inverseRotation ? 3 : 1)) % 4;
			}
		} else if (distSq <= this.visibleDistSq) {
			if (Math.abs(dy) <= 1.0) {
				this.dir = (dx < 0 ? 2 : 0);
			} else {
				this.dir = (dy < 0 ? 1 : 3);
			}

			tryAround = true;
		}

		Level.passableMap[this.cellY][this.cellX] &= ~Level.PASSABLE_IS_MONSTER;
		var vis = false;

		if ((distSq <= this.visibleDistSq) && Level.traceLine(this.cellX + 0.5, this.cellY + 0.5, Hero.x, Hero.y, Level.PASSABLE_MASK_SHOOT_WM)) {
			this.chaseMode = true;
			vis = true;
		}

		if (vis && (distSq <= this.attackDistSq)) {
			var angleToHero = (Math.atan2(dy, dx) * Engine.ANG_180 / Math.PI) | 0;
			var angleDiff = angleToHero - this.shootAngle;

			if (angleDiff > Engine.ANG_180) {
				angleDiff -= Engine.ANG_360;
			} else if (angleDiff < -Engine.ANG_180) {
				angleDiff += Engine.ANG_360;
			}

			var dist = Math.sqrt(distSq);
			var minAngle = Math.max(1, 15 - ((dist * 3.0) | 0));

			if (Math.abs(angleDiff) <= minAngle) {
				this.isAimedOnHero = true;
				this.hitHeroHits = Engine.getRealHits(this.hits, dist);
				this.hitHeroTimeout = 2;
				this.attackTimeout = 15;
				this.step = 50;
			} else {
				this.step = (8 + angleDiff / Engine.ANG_5) | 0;
			}

			this.isInAttackState = true;
			this.shootAngle = angleToHero;
			this.dir = (((angleToHero + Engine.ANG_45 + Engine.ANG_720) % Engine.ANG_360) / Engine.ANG_90) | 0;
			this.aroundReqDir = -1;
		} else {
			this.waitForDoor = false;

			for (var i = 0; i < 4; i++) {
				switch (this.dir) {
					case 0:
						this.cellX++;
						break;

					case 1:
						this.cellY++;
						break;

					case 2:
						this.cellX--;
						break;

					case 3:
						this.cellY--;
						break;
				}

				if ((Level.passableMap[this.cellY][this.cellX] & Level.PASSABLE_MASK_MONSTER) == 0) {
					if (this.dir == this.aroundReqDir) {
						this.aroundReqDir = -1;
					}

					this.step = this.maxStep;
					break;
				}

				if (this.chaseMode &&
					(Level.passableMap[this.cellY][this.cellX] & Level.PASSABLE_IS_DOOR) &&
					(Level.passableMap[this.cellY][this.cellX] & Level.PASSABLE_IS_DOOR_OPENED_BY_HERO)
				) {
					var door = Level.doorsMap[this.cellY][this.cellX];

					if (door && !door.sticked) {
						door.open();

						this.waitForDoor = true;
						this.cellX = this.prevX;
						this.cellY = this.prevY;
						this.step = 10;
						break;
					}
				}

				this.cellX = this.prevX;
				this.cellY = this.prevY;

				if (tryAround) {
					if ((this.prevAroundX == this.cellX) && (this.prevAroundY == this.cellY)) {
						this.inverseRotation = !this.inverseRotation;
					}

					this.aroundReqDir = this.dir;
					this.prevAroundX = this.cellX;
					this.prevAroundY = this.cellY;
					this.tryAround = false;
				}

				this.dir = (this.dir + (this.inverseRotation ? 1 : 3)) % 4;
			}

			if (this.step == 0) {
				this.step = this.maxStep / 2;
			}

			this.shootAngle = this.dir * Engine.ANG_90;
		}

		Level.passableMap[this.cellY][this.cellX] |= Level.PASSABLE_IS_MONSTER;
	}

	this.x = this.cellX + ((this.prevX - this.cellX) * this.step / this.maxStep);
	this.y = this.cellY + ((this.prevY - this.cellY) * this.step / this.maxStep);

	if (this.attackTimeout > 0) {
		this.attackTimeout--;
	}

	if (this.hitTimeout > 0) {
		this.hitTimeout--;
	} else if (this.step > 0) {
		this.step--;
	}
};
