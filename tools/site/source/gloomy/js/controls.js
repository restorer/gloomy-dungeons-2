window.Controls = {
	FORWARD: 1,
	BACKWARD: 2,
	LEFT: 3,
	RIGHT: 4,
	ACTION: 5,
	SW_HAND: 6,
	SW_PIST: 7,
	SW_SHTG: 8,
	SW_CHGN: 9,
	MAP: 10,

	state: {},
	rstate: {},
	keymap: {},

	init: function() {
		Controls.state[Controls.FORWARD] = false;
		Controls.state[Controls.BACKWARD] = false;
		Controls.state[Controls.LEFT] = false;
		Controls.state[Controls.RIGHT] = false;
		Controls.state[Controls.ACTION] = false;
		Controls.state[Controls.SW_HAND] = false;
		Controls.state[Controls.SW_PIST] = false;
		Controls.state[Controls.SW_SHTG] = false;
		Controls.state[Controls.SW_CHGN] = false;
		Controls.state[Controls.MAP] = false;

		for (var k in Controls.state) {
			if (Controls.state.hasOwnProperty(k)) {
				Controls.rstate[k] = { masked: false, active: false };
			}
		}

		Controls.keymap[-1] = { state: Controls.FORWARD, active: false };
		Controls.keymap[-2] = { state: Controls.BACKWARD, active: false };
		Controls.keymap[-3] = { state: Controls.LEFT, active: false };
		Controls.keymap[-4] = { state: Controls.RIGHT, active: false };
		Controls.keymap[-5] = { state: Controls.ACTION, active: false };
		Controls.keymap[-6] = { state: Controls.SW_HAND, active: false };
		Controls.keymap[-7] = { state: Controls.SW_PIST, active: false };
		Controls.keymap[-8] = { state: Controls.SW_SHTG, active: false };
		Controls.keymap[-9] = { state: Controls.SW_CHGN, active: false };
		Controls.keymap[-10] = { state: Controls.MAP, active: false };

		Controls.keymap[38] = { state: Controls.FORWARD, active: false };
		Controls.keymap[40] = { state: Controls.BACKWARD, active: false };
		Controls.keymap[37] = { state: Controls.LEFT, active: false };
		Controls.keymap[39] = { state: Controls.RIGHT, active: false };
		Controls.keymap[32] = { state: Controls.ACTION, active: false };
		Controls.keymap[9] = { state: Controls.MAP, active: false };

		Controls.keymap['W'.charCodeAt(0)] = { state: Controls.FORWARD, active: false };
		Controls.keymap['S'.charCodeAt(0)] = { state: Controls.BACKWARD, active: false };
		Controls.keymap['A'.charCodeAt(0)] = { state: Controls.LEFT, active: false };
		Controls.keymap['D'.charCodeAt(0)] = { state: Controls.RIGHT, active: false };
		Controls.keymap['0'.charCodeAt(0)] = { state: Controls.SW_HAND, active: false };
		Controls.keymap['1'.charCodeAt(0)] = { state: Controls.SW_PIST, active: false };
		Controls.keymap['2'.charCodeAt(0)] = { state: Controls.SW_SHTG, active: false };
		Controls.keymap['3'.charCodeAt(0)] = { state: Controls.SW_CHGN, active: false };
		Controls.keymap['M'.charCodeAt(0)] = { state: Controls.MAP, active: false };
	},

	bind: function() {
		jQuery('.controls-wrapper').fadeIn();

		function evStop(ev) {
			if (ev.stopPropagation) {
				ev.stopPropagation();
			}

			if (ev.preventDefault) {
				ev.preventDefault();
			}

			ev.cancelBubble = true;
		}

		function keyHandler(ev, active) {
			if (typeof(Controls.keymap[ev.keyCode]) == 'undefined') {
				return true;
			}

			Controls.keymap[ev.keyCode].active = active;
			evStop(ev);
			return false;
		}

		jQuery.each([
			['forward', -1],
			['backward', -2],
			['left', -3],
			['right', -4],
			['action', -5],
			['sw-hand', -6],
			['sw-pist', -7],
			['sw-shtg', -8],
			['sw-chgn', -9],
			['map', -10]
		], function(idx, item) {
			var el = jQuery('#control-' + item[0]);
			var kmap = Controls.keymap[item[1]];

			el.mousedown(function(ev){ kmap.active = true; evStop(ev || event); return false; });
			el.mouseup(function(ev){ kmap.active = false; evStop(ev || event); return false; });

			el.bind('touchstart', function(ev){ kmap.active = true; evStop(ev || event); return false; });
			el.bind('touchend', function(ev){ kmap.active = false; evStop(ev || event); return false; });
		});

		jQuery(document).bind('keydown', function(ev){ return keyHandler(ev || event, true); });
		jQuery(document).bind('keyup', function(ev){ return keyHandler(ev || event, false); });
	},

	update: function() {
		for (var k in Controls.rstate) {
			if (Controls.rstate.hasOwnProperty(k)) {
				Controls.rstate[k].active = false;
			}
		}

		for (var k in Controls.keymap) {
			if (Controls.keymap.hasOwnProperty(k)) {
				Controls.rstate[Controls.keymap[k].state].active |= Controls.keymap[k].active;
			}
		}

		for (var k in Controls.rstate) {
			if (Controls.rstate.hasOwnProperty(k)) {
				Controls.state[k] = Controls.rstate[k].active && !Controls.rstate[k].masked;
				Controls.rstate[k].masked = Controls.rstate[k].masked && Controls.rstate[k].active;
			}
		}
	},

	keyUp: function(key) {
		Controls.state[key] = false;
		Controls.rstate[key].masked = true;
	}
};
