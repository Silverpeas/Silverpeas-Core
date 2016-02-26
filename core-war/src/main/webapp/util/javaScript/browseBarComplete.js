function removeBreadCrumbElements() {
    $('#breadCrumb .component').nextAll().remove();
}
function addBreadCrumbElement(link, label) {
	$('#breadCrumb').append('<span class="connector">&nbsp;>&nbsp;</span><a href="'+link+'">'+label+'</a>');
}