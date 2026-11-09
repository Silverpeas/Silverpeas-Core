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
    generate.title = options.labels.generate;
    generate.setAttribute('aria-label', options.labels.generate);
    generate.className = 'secret-control secret-generate';
    generate.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 11a8 8 0 0 0-14-5L4 8m0-5v5h5M4 13a8 8 0 0 0 14 5l2-2m0 5v-5h-5"/></svg>';
    const copyButton = document.createElement('button');
    copyButton.type = 'button';
    copyButton.title = options.labels.copy;
    copyButton.setAttribute('aria-label', options.labels.copy);
    copyButton.className = 'secret-control secret-copy';
    copyButton.innerHTML = '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2"/></svg>';
    if (input.type === 'password') {
      const visibility = document.createElement('button');
      visibility.type = 'button';
      visibility.className = 'secret-control secret-visibility';
      const eye = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>';
      const eyeOff = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m3 3 18 18M10.6 5.1A11 11 0 0 1 12 5c6.5 0 10 7 10 7a16 16 0 0 1-3.2 4.1M6.3 6.3C3.5 8.1 2 12 2 12s3.5 7 10 7a10 10 0 0 0 4.1-.9"/><path d="M10 10a3 3 0 0 0 4 4"/></svg>';
      const refreshVisibility = () => {
        const shown = input.type === 'text';
        const label = shown ? options.labels.hide : options.labels.show;
        visibility.title = label;
        visibility.setAttribute('aria-label', label);
        visibility.setAttribute('aria-pressed', String(shown));
        visibility.innerHTML = shown ? eyeOff : eye;
      };
      visibility.addEventListener('click', () => {
        input.type = input.type === 'password' ? 'text' : 'password';
        refreshVisibility();
      });
      refreshVisibility();
      controls.append(visibility);
    }
    controls.append(generate, copyButton);
    if (controls.querySelector('.secret-visibility')) {
      const wrapper = document.createElement('span');
      wrapper.className = 'password-input-with-actions';
      input.parentNode.insertBefore(wrapper, input);
      wrapper.append(input, controls);
      status.className = 'password-action-status';
      wrapper.insertAdjacentElement('afterend', status);
      const confirmation = input.form?.querySelector(
          '[id="' + input.dataset.passwordConfirmation + '"]');
      if (confirmation) {
        confirmation.classList.add('password-confirmation-matched');
        const syncWidth = () => {
          if (confirmation.isConnected) {
            wrapper.style.width = confirmation.getBoundingClientRect().width + 'px';
          }
        };
        syncWidth();
        if (typeof ResizeObserver !== 'undefined') {
          new ResizeObserver(syncWidth).observe(confirmation);
        } else {
          window.addEventListener('resize', syncWidth);
        }
      }
    } else {
      controls.append(status);
      input.insertAdjacentElement('afterend', controls);
    }

    let busy = false;
    const update = () => {
      generate.disabled = busy || input.disabled || input.readOnly;
      const visibility = controls.querySelector('.secret-visibility');
      if (visibility) visibility.disabled = input.disabled;
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
