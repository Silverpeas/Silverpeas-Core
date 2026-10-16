/* Download a shared file after optional verification of its security code. */
(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    const link = document.getElementById("downloadLink");
    if (!link) {
      return;
    }

    link.addEventListener("click", function (event) {
      if (link.dataset.noCode === "true") {
        // Keep the original link, filename and browser download behavior.
        return;
      }
      event.preventDefault();

      const dialog = jQuery("#securityDialog");
      const codeInput = document.getElementById("securityCode");
      const errorElement = document.getElementById("securityCodeError");
      let verifying = false;

      function showError(message) {
        errorElement.textContent = message;
        jQuery(errorElement).show();
      }

      dialog.dialog({
        modal: true,
        title: link.dataset.securityTitle || "",
        width: 400,
        buttons: [
          {
            text: link.dataset.confirmLabel,
            click: function () {
              if (verifying) {
                return;
              }
              verifying = true;
              errorElement.textContent = "";
              jQuery(errorElement).hide();
              const code = codeInput.value;
              fetch(link.href, {
                method: "GET",
                headers: { "X-Verification-Code": code }
              }).then(function (response) {
                const contentType = response.headers.get("Content-Type") || "";
                if (response.status === 429) {
                  throw new Error("SHARING_CODE_LOCKED");
                }
                if (!response.ok || contentType.toLowerCase().includes("text/html")) {
                  throw new Error("SHARING_CODE_INVALID");
                }
                return response.blob();
              }).then(function (blob) {
                dialog.dialog("close");
                const url = URL.createObjectURL(blob);
                const download = document.createElement("a");
                download.href = url;
                download.download = link.dataset.filename || "download";
                document.body.appendChild(download);
                download.click();
                download.remove();
                setTimeout(function () { URL.revokeObjectURL(url); }, 60000);
              }).catch(function (error) {
                if (error.message === "SHARING_CODE_LOCKED") {
                  showError(link.dataset.lockedError);
                } else if (error.message === "SHARING_CODE_INVALID") {
                  codeInput.value = "";
                  showError(link.dataset.downloadError);
                } else {
                  showError(link.dataset.verificationError);
                }
              }).finally(function () {
                verifying = false;
              });
            }
          },
          {
            text: link.dataset.cancelLabel,
            click: function () {
              dialog.dialog("close");
            }
          }
        ]
      });
    });
  });
})();
