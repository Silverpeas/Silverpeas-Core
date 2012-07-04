function initLI() {
	if (IN.User.isAuthorized()) {
		$('#LinkedInPublishButton').show();
	} else {
		$('#LinkedInLoginButton').show();
	}
}

function logIntoLinkedIN() {
	IN.User.authorize(function(response) {
		$('#LinkedInLoginButton').hide();
		$('#LinkedInPublishButton').show();
		});
}

function publishToLinkedIN() {
	var newStatus = $( "#myProfileFiche .statut").html();
	IN.API.Raw("/people/~/current-status") // Update (PUT) the status
	  .method("PUT")
	  .body(JSON.stringify(newStatus))
	  .result( function(result) { statusPublished(); } )
	  .error(  function(error)  { statusPublishFailed(); } );
}