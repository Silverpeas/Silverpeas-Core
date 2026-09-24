function bytesToSize(bytes, precision) {
  var sizes = [ window.i18n.prop("file.size.bytes"),
      window.i18n.prop("file.size.Kb"), window.i18n.prop("file.size.Mb"),
      window.i18n.prop("file.size.Gb"), window.i18n.prop("file.size.Tb") ];
  var posttxt = 0;
  if (bytes == 0)
    return 'n/a';
  while (bytes >= 1024) {
    posttxt++;
    bytes = bytes / 1024;
  }
  return bytes.toFixed(precision) + " " + sizes[posttxt];
}
function openSecurityCodeDialog(token, baseURL, onSuccess) {
  var dialog = document.getElementById("securityDialog");
  dialog.innerHTML = '<div id="securityCheck"><span>' + window.i18n.prop("sharing.security.code") +
      '</span><input type="text" id="securityCode" name="securityCode"/></div>';
  $(dialog).dialog({
    modal: true,
    title: window.i18n.prop("sharing.security.code"),
    width: 400,
    buttons: [
      {
        text: window.i18n.prop("sharing.security.ok"),
        click: function() {
          var currentDialog = this;
          var code = document.getElementById("securityCode").value;
          var uri = baseURL + "/silverpeas/services/sharing/" + token + "/check";
          fetch(uri, {
            method: "GET",
            headers: { "X-Verification-Code": code }
          }).then(function(response) {
            return response.json();
          }).then(function(result) {
            if (result) {
              $(currentDialog).dialog("close");
              onSuccess();
            } else {
              document.getElementById("securityCode").value = "";
            }
          });
        }
      },
      {
        text: window.i18n.prop("sharing.security.cancel"),
        click: function() {
          $(this).dialog("close");
        }
      }
    ]
  });
}
