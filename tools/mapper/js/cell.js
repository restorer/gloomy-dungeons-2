var cell = (function() {
	return {
		create: function() {
			return {
				type: 0,
				value: 0,
				floor: [ 0, 0, 0, 0 ],
				ceil: [ 0, 0, 0, 0 ],
				arrow: 0,
				noTrans: false,
				mark: ''
			};
		},

		equals: function(itemA, itemB) {
			return (
				itemA.type == itemB.type
				&& itemA.value == itemB.value
				&& itemA.floor[0] == itemB.floor[0]
				&& itemA.floor[1] == itemB.floor[1]
				&& itemA.floor[2] == itemB.floor[2]
				&& itemA.floor[3] == itemB.floor[3]
				&& itemA.ceil[0] == itemB.ceil[0]
				&& itemA.ceil[1] == itemB.ceil[1]
				&& itemA.ceil[2] == itemB.ceil[2]
				&& itemA.ceil[3] == itemB.ceil[3]
				&& itemA.arrow == itemB.arrow
				&& itemA.noTrans == itemB.noTrans
				&& itemA.mark == itemB.mark
			);
		},

		clear: function(item, clearFloor) {
			item.type = 0;
			item.value = 0;

			if (typeof(clearFloor) == 'undefined' || clearFloor) {
				item.floor = [ 0, 0, 0, 0 ];
				item.ceil = [ 0, 0, 0, 0 ];
			}

			item.arrow = 0;
			item.noTrans = false;
			item.mark = '';
		},

		getCopy: function(item) {
			return {
				type: item.type,
				value: item.value,
				floor: [ item.floor[0], item.floor[1], item.floor[2], item.floor[3] ],
				ceil: [ item.ceil[0], item.ceil[1], item.ceil[2], item.ceil[3] ],
				arrow: item.arrow,
				noTrans: item.noTrans,
				mark: item.mark
			};
		},

		copyToFrom: function(to, from) {
			to.type = from.type;
			to.value = from.value;
			to.floor = [ from.floor[0], from.floor[1], from.floor[2], from.floor[3] ];
			to.ceil = [ from.ceil[0], from.ceil[1], from.ceil[2], from.ceil[3] ];
			to.arrow = from.arrow;
			to.noTrans = from.noTrans;
			to.mark = from.mark;
		},

		isEmpty: function(item) {
			return (
				item.type == 0
				&& item.floor[0] == 0
				&& item.floor[1] == 0
				&& item.floor[2] == 0
				&& item.floor[3] == 0
				&& item.arrow == 0
				&& item.mark == ''
			);
		},

		isShownAsEmpty: function(item) {
			return (
				item.type == 0
				&& item.floor[0] == 0
				&& item.floor[1] == 0
				&& item.floor[2] == 0
				&& item.floor[3] == 0
				&& item.arrow == 0
			);
		}
	};
})();
