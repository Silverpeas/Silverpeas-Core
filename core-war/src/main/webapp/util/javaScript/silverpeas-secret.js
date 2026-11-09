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
// Generation and clipboard operations shared by password and sharing forms.
window.SilverpeasSecret = (() => {
  "use strict";

  async function generate(profile) {
    const paths = {
      password: '/password/generate',
      sharing: '/secrets/sharing-code'
    };
    if (!Object.hasOwn(paths, profile)) {
      throw new TypeError('Unknown secret generation profile');
    }
    const secret = await jQuery.ajax({
      url: webContext + '/services' + paths[profile],
      type: 'POST',
      dataType: 'json',
      cache: false
    });
    if (typeof secret?.value !== 'string' || !secret.value) {
      throw new TypeError('Missing generated secret');
    }
    return secret.value;
  }

  async function copy(input) {
    if (!input.disabled && input.value) {
      await navigator.clipboard.writeText(input.value);
    }
  }

  function addControls(input, options) {
    if (input.dataset.secretControls) {
      return;
    }
    input.dataset.secretControls = 'true';
    const controls = document.createElement('span');
    controls.className = 'password-generation-controls';
    const status = document.createElement('span');
    status.setAttribute('role', 'status');
    const generate = document.createElement('button');
    generate.type = 'button';
    generate.textContent = options.labels.generate;
    const copyButton = document.createElement('button');
    copyButton.type = 'button';
    copyButton.textContent = options.labels.copy;
    controls.append(generate, copyButton, status);
    input.insertAdjacentElement('afterend', controls);

    let busy = false;
    const update = () => {
      generate.disabled = busy || input.disabled || input.readOnly;
      copyButton.disabled = generate.disabled || !input.value;
    };
    const observer = new MutationObserver(update);
    observer.observe(input, {attributes: true, attributeFilter: ['disabled', 'readonly']});
    input.addEventListener('input', update);
    input.addEventListener('change', update);
    generate.addEventListener('click', async () => {
      if (generate.disabled) {
        return;
      }
      busy = true;
      update();
      status.textContent = '';
      try {
        const value = await options.generate();
        if (typeof value !== 'string' || !value) {
          throw new TypeError('Missing generated secret');
        }
        if (!input.disabled && !input.readOnly && input.isConnected) {
          input.value = value;
          const confirmation = input.form?.querySelector(
              '[id="' + input.dataset.passwordConfirmation + '"]');
          if (confirmation) {
            confirmation.value = value;
            confirmation.dispatchEvent(new Event('input', {bubbles: true}));
            confirmation.dispatchEvent(new Event('change', {bubbles: true}));
          }
          input.dispatchEvent(new Event('input', {bubbles: true}));
          input.dispatchEvent(new Event('change', {bubbles: true}));
          status.textContent = options.labels.generated;
        }
      } catch (error) {
        status.textContent = options.labels.error;
      } finally {
        busy = false;
        update();
      }
    });
    copyButton.addEventListener('click', async () => {
      if (copyButton.disabled) {
        return;
      }
      try {
        await copy(input);
        status.textContent = options.labels.copied;
      } catch (error) {
        status.textContent = options.labels.copyError;
      }
    });
    update();
  }

  return Object.freeze({generate, copy, addControls});
})();
