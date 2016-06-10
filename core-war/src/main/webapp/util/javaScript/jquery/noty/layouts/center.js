;(function($) {

	$.noty.layouts.center = {
		name: 'center',
		options: { // overrides options

		},
		container: {
			object: '<ul id="noty_center_layout_container" />',
			selector: 'ul#noty_center_layout_container',
			style: function() {
				$(this).css({
					position: 'fixed',
          width: 'auto',
          maxWidth: '1000px',
					height: 'auto',
					margin: 0,
					padding: 0,
					listStyleType: 'none',
					zIndex: 10000000
				});

				// getting hidden height
				var dupe = $(this).clone().css({visibility:"hidden", display:"block", position:"absolute", top: 0, left: 0, width: 'auto', maxWidth: "1000px"}).attr('id', 'dupe');
				$("body").append(dupe);
				dupe.find('.i-am-closing-now').remove();
				dupe.find('li').css('display', 'block');
				var actual_width = dupe.outerWidth(false);
				var actual_height = dupe.height();
				dupe.remove();

        var position = {
          left: ($(window).width() - actual_width) / 2 + 'px',
          top: ($(window).height() - actual_height) / 2 + 'px'
        };

				if ($(this).hasClass('i-am-new')) {
					$(this).css(position);
				} else {
					$(this).animate(position, 500);
				}

			}
		},
		parent: {
			object: '<li />',
			selector: 'li',
			css: {}
		},
		css: {
			display: 'none',
      width: 'auto',
      maxWidth: '1000px'
		},
		addClass: ''
	};

})(jQuery);