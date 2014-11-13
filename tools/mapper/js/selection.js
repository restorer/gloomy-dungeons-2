var selection = (function() {
	return {
		active: false,
		isFirstPoint: false,
		tsx: 0,
		tsy: 0,
		tex: 0,
		tey: 0,
		sx: 0,
		sy: 0,
		ex: 0,
		ey: 0,
		element: null,
		style: null,
		infoElement: null,

		init: function() {
			var selectionElement = lib.query('.selection').dom();

			selection.element = selectionElement;
			selection.style = selectionElement.style;

			selection.infoElement = lib.query('.b-selection-info').dom();
		},

		update: function() {
			if (selection.active) {
				selection.style.top = (selection.sy * 64) + 'px';
				selection.style.left = (selection.sx * 64) + 'px';
				selection.style.width = (selection.w * 64) + 'px';
				selection.style.height = (selection.h * 64) + 'px';
				selection.style.display = 'block';

				selection.infoElement.innerHTML = selection.w + '&times;' + selection.h;
			} else {
				selection.style.display = 'none';
				selection.infoElement.innerHTML = '';
			}
		}
	};
})();
