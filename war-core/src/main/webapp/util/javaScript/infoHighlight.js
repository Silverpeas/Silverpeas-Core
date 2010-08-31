//manage the tooltip displaying in publication
//the displaying personalization must be done here
$(document).ready(function() {
	$('.highlight-silver').each(function(){
		   $(this).qtip({
			   content: { text: false // Use each elements title attribute
			   },				   
		       style: {
                  border: {
                     width: 5,
                     radius: 5
                  },
                  padding: 7, 
                  textAlign: 'center',
                  tip: true, 
                  name: 'green' 
               },

		      position: {
				corner: {
				   target: 'topRight',
				   tooltip: 'topLeft'
				}
			  }
						      
		   });
		});
});
