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

  document.addEventListener("click", async function (event) {
    const generateButton = event.target.closest("#generateSharingSecurityCode");
    if (generateButton) {
      if (generateButton.disabled) {
        return;
      }
      const input = document.getElementById("securityCode");
      generateButton.disabled = true;
      try {
        const value = await SilverpeasSecret.generate('sharing');
        if (input.isConnected) {
          input.value = value;
          input.dispatchEvent(new Event('input', {bubbles: true}));
          input.dispatchEvent(new Event('change', {bubbles: true}));
        }
      } catch (error) {
        notyError(generateButton.dataset.errorMessage);
      } finally {
        generateButton.disabled = false;
      }
      return;
    }
    const copyButton = event.target.closest("#copySharingSecurityCode");
    if (copyButton) {
      const input = document.getElementById("securityCode");
      try {
        await SilverpeasSecret.copy(input);
        notyInfo(copyButton.dataset.copyMessage);
      } catch (error) {
        console.error("Unable to copy sharing security code", error);
      }
    }
  });
})();
