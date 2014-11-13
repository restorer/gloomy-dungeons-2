window.Door = function(x, y, ftex, stex) {
	this.x = x;
	this.y = y;
	this.ftex = ftex;
	this.stex = stex;
	this.pos = 0;
	this.dir = 0;
	this.sticked = false;
	this.requiredKey = 0;
	this.mark = null;
	this.waitForClose = 0;
};

Door.MAX_WAIT_FOR_CLOSE = 250;

Door.prototype.stick = function(opened) {
	this.sticked = true;

	if (opened) {
		// normal open
		this.dir = 1;
	} else {
		// but instant close
		this.dir = 0;
		this.pos = 0;
		Level.passableMap[this.y][this.x] |= Level.PASSABLE_IS_DOOR;
	}
};

Door.prototype.open = function() {
	if (!this.sticked) {
		this.dir = 1;
		return true;
	}

	return false;
};

Door.prototype.tick = function() {
	if (this.dir > 0) {
		this.pos += 0.05;

		if (this.pos >= 1) {
			this.dir = 0;
			this.pos = 1;
			Level.passableMap[this.y][this.x] &= ~Level.PASSABLE_IS_DOOR;
			this.waitForClose = Door.MAX_WAIT_FOR_CLOSE;
		}
	} else if (this.dir < 0) {
		this.pos -= 0.1;

		if (this.pos <= 0) {
			this.dir = 0;
			this.pos = 0;
		}
	}

	if (!this.sticked && this.dir == 0 && this.pos == 1) {
		if ((Level.passableMap[this.y][this.x] & Level.PASSABLE_MASK_DOOR) == 0) {
			if (this.waitForClose > 0) {
				this.waitForClose -= 1;
			} else {
				Level.passableMap[this.y][this.x] |= Level.PASSABLE_IS_DOOR;
				this.dir = -1;
			}
		} else {
			this.waitForClose = Door.MAX_WAIT_FOR_CLOSE;
		}
	}
};
