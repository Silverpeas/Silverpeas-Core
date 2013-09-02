//manage the tooltip displaying in publication
//the displaying personalization must be done here
$(document).ready(function() {
	$('.highlight-silver').each(function(){
		   $(this).qtip({
			content: {
				text: false // Use each elements title attribute
			},
			style: {
				tip: true,
				classes: "qtip-shadow qtip-green"
			},
			position: {
				at: "top right",
				my: "top left"
			}
		   });
		});
});
