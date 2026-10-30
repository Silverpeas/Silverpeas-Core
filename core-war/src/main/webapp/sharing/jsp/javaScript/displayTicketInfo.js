/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
        errorElement.hidden = false;
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
              errorElement.hidden = true;
              const code = codeInput.value;
              fetch(link.href, {
                method: "GET",
                headers: { "X-Verification-Code": code }
              }).then(function (response) {
                if (response.status === 429) {
                  throw new Error("SHARING_CODE_LOCKED");
                }
                if (!response.ok) {
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
