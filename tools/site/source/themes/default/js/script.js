jQuery(function() {
	function scrollSliderTo(idx) {
		jQuery('.slider .bullets b').removeClass('current');
		jQuery('.slider .bullets b').eq(idx).addClass('current');

		jQuery('.slider .img').removeClass('current');
		jQuery('.slider .img').eq(idx).addClass('current');

		jQuery('.slider .inner').animate({ right: ((465 / 2) - (9 - idx) * 465) });
	}

	jQuery('.slider .bullets b').each(function(idx) {
		jQuery(this).click(function() {
			scrollSliderTo(idx);
		});
	});

	jQuery('.slider .inner img').each(function(idx) {
		jQuery(this).click(function() {
			scrollSliderTo(idx);
		});
	});

	var bodyElem = jQuery('html,body');

	jQuery('a').each(function() {
		var el = jQuery(this);
		var href = el.attr('href');

		if (href && href.substr(0, 1) == '#') {
			var name = el.attr('href').substr(1);

			el.click(function() {
				var pos = jQuery('a[name="' + name + '"]').next().offset().top;

				bodyElem.stop(true, true).animate({ scrollTop: pos }, 400, 'linear', function() {
					setTimeout(function() {
						location.href = href;
					}, 1);
				});

				return false;
			});
		}
	});
});
