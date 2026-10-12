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
let sharingVerificationCode = null;

function sharingAjax(url, onSuccess) {
  return $.ajax({
    url: url,
    dataType: "json",
    headers: sharingVerificationCode === null ? {} : {"X-Verification-Code": sharingVerificationCode},
    success: onSuccess
  });
}

function sharingFetch(url) {
  return fetch(url, {
    headers: sharingVerificationCode === null ? {} : {"X-Verification-Code": sharingVerificationCode}
  });
}

function sharingDownload(url, filename) {
  return sharingFetch(url).then(function(response) {
    if (!response.ok) {
      throw new Error("Download failed: " + response.status);
    }
    return response.blob();
  }).then(function(blob) {
    const objectUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = objectUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(function() { URL.revokeObjectURL(objectUrl); }, 60000);
  }).catch(function(error) {
    console.error(error);
    alert("Unable to download file.");
  });
}

function sharingDownloadLink(event, url, filename) {
  event.preventDefault();
  sharingDownload(url, filename);
}

function openSecurityCodeDialog(token, baseURL, onSuccess) {
  const dialog = document.getElementById("securityDialog");
  dialog.innerHTML = '<div id="securityCheck"><span>' + window.i18n.prop("sharing.security.code") +
      '</span><input type="text" id="securityCode" name="securityCode"/>' +
      '<div id="securityCodeError" class="ui-state-error" role="alert" aria-live="polite" style="display:none"></div></div>';
  $(dialog).dialog({
    modal: true,
    title: window.i18n.prop("sharing.security.code"),
    width: 400,
    buttons: [
      {
        text: window.i18n.prop("sharing.security.ok"),
        click: function() {
          const currentDialog = this;
          const code = document.getElementById("securityCode").value;
          const errorElement = document.getElementById("securityCodeError");
          errorElement.textContent = "";
          $(errorElement).hide();
          const uri = baseURL + "/silverpeas/services/sharing/" + token + "/check";
          fetch(uri, {
            method: "GET",
            headers: { "X-Verification-Code": code }
          }).then(function(response) {
            if (response.status === 429) {
              throw new Error("SHARING_CODE_LOCKED");
            }
            if (!response.ok) {
              throw new Error("SHARING_CODE_ERROR");
            }
            return response.json();
          }).then(function(result) {
            if (result) {
              sharingVerificationCode = code;
              $(currentDialog).dialog("close");
              onSuccess();
            } else {
              document.getElementById("securityCode").value = "";
              errorElement.textContent = window.i18n.prop("sharing.security.invalid");
              $(errorElement).show();
            }
          }).catch(function(error) {
            const locked = error.message === "SHARING_CODE_LOCKED";
            const messageKey = locked ? "sharing.security.locked" : "sharing.security.error";
            errorElement.textContent = window.i18n.prop(messageKey);
            $(errorElement).show();
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
