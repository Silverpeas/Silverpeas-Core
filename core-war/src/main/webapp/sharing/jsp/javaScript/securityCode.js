/* Security-code actions for the sharing-ticket dialog. */
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
