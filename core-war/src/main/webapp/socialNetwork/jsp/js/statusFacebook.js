function initFB() {
    FB.getLoginStatus(function(response) {
	  if (response.status === 'connected') {
		  $('#FBPublishButton').show();
	  } else if (response.status === 'not_authorized') {
		  $('#FBLoginButton').show();
	  } else {
	    $('#FBLoginButton').show();
	  }
	 });
}

function logIntoFB() {
	  FB.login(function(response) {
		   if (response.authResponse) {
			   $('#FBLoginButton').hide();
			   $('#FBPublishButton').show();
		   } else {
		     // User cancelled login or did not fully authorize
		   }
		 }, {scope: 'email,publish_stream,offline_access'});
}

function publishToFB() {
	  var newStatus = $( "#myProfileFiche .statut").html();
	  FB.api('/me/feed', 'post', { message: newStatus }, function(response) {
		  if (!response || response.error) {
			statusPublishFailed();
		  } else {
			statusPublished();
		  }
		});
}