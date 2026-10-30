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

  function randomIndex(max) {
    const limit = Math.floor(0x100000000 / max) * max;
    const value = new Uint32Array(1);
    do {
      crypto.getRandomValues(value);
    } while (value[0] >= limit);
    return value[0] % max;
  }

  function generateSecureCode(length = 32) {
    const groups = [
      "abcdefghijklmnopqrstuvwxyz",
      "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
      "0123456789",
      "!@#$%^&*()_+-=[]{}|;:'\",.<>?/\\~`"
    ];
    const all = groups.join("");
    const result = groups.map(group => group[randomIndex(group.length)]);

    for (let i = result.length; i < length; i++) {
      result.push(all[randomIndex(all.length)]);
    }
    for (let i = result.length - 1; i > 0; i--) {
      const j = randomIndex(i + 1);
      [result[i], result[j]] = [result[j], result[i]];
    }
    return result.join("");
  }

  document.addEventListener("click", async function (event) {
    const generateButton = event.target.closest("#generateSharingSecurityCode");
    if (generateButton) {
      document.getElementById("securityCode").value = generateSecureCode();
      return;
    }
    const copyButton = event.target.closest("#copySharingSecurityCode");
    if (copyButton) {
      const input = document.getElementById("securityCode");
      try {
        await navigator.clipboard.writeText(input.value);
        notyInfo(copyButton.dataset.copyMessage);
      } catch (error) {
        console.error("Unable to copy sharing security code", error);
      }
    }
  });
})();
