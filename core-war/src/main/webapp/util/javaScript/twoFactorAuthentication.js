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
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('formTwoFactor');
  const submit = document.getElementById('submitTwoFactor');
  submit.addEventListener('click', (event) => {
    event.preventDefault();
    form.requestSubmit();
  });
  const totpMode = document.getElementById('totpMode');
  const recoveryMode = document.getElementById('recoveryMode');
  const totpCode = document.getElementById('TwoFactorCode');
  const recoveryCode = document.getElementById('RecoveryCode');
  const useRecoveryCode = document.getElementById('useRecoveryCode');
  const useTotpCode = document.getElementById('useTotpCode');
  const introduction = document.getElementById('introduction');

  const setRecoveryMode = (enabled) => {
    introduction.classList.toggle('is-hidden', enabled);
    totpMode.classList.toggle('is-hidden', enabled);
    recoveryMode.classList.toggle('is-hidden', !enabled);
    useRecoveryCode.classList.toggle('is-hidden', enabled);
    useTotpCode.classList.toggle('is-hidden', !enabled);
    totpCode.disabled = enabled;
    recoveryCode.disabled = !enabled;
    const activeCode = enabled ? recoveryCode : totpCode;
    const inactiveCode = enabled ? totpCode : recoveryCode;
    inactiveCode.removeAttribute('name');
    activeCode.setAttribute('name', 'TwoFactorCode');
    activeCode.focus();
  };
  recoveryCode.disabled = true;
  if (useRecoveryCode && useTotpCode) {
    useRecoveryCode.addEventListener('click', (event) => {
      event.preventDefault();
      setRecoveryMode(true);
    });
    useTotpCode.addEventListener('click', (event) => {
      event.preventDefault();
      setRecoveryMode(false);
    });
  }
});
