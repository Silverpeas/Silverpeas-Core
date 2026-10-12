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

      jQuery("#securityDialog").popup("validation", {
        title: "",
        callback: function () {
          const code = document.getElementById("securityCode").value;
          fetch(link.href, {
            method: "GET",
            headers: { "X-Verification-Code": code }
          }).then(function (response) {
            const contentType = response.headers.get("Content-Type") || "";
            if (!response.ok || contentType.toLowerCase().includes("text/html")) {
              throw new Error("Unable to download shared file");
            }
            return response.blob();
          }).then(function (blob) {
            const url = URL.createObjectURL(blob);
            const download = document.createElement("a");
            download.href = url;
            download.download = link.dataset.filename || "download";
            document.body.appendChild(download);
            download.click();
            download.remove();
            setTimeout(function () { URL.revokeObjectURL(url); }, 60000);
          }).catch(function () {
            jQuery.popup.error(link.dataset.downloadError);
          });
          return true;
        }
      });
    });
  });
})();
