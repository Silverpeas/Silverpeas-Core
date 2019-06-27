function confirmRemove(strURL, strQuestion) {
  jQuery.popup.confirm(strQuestion, function() {
    location.href = strURL;
  });
}
