/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

//# sourceURL=/silverpeas/util/javaScript/password.js

/**
 * Setting password UI & behaviour and form submit.
 * @param params
 * @return {boolean}
 */
function handlePasswordForm(params) {
  const settings = $.extend({
    passwordFormId: '',
    passwordInputId: '',
    passwordFormAction: '',
    extraValidations: undefined
  }, params);
  if (!settings.passwordFormId || !settings.passwordInputId || !settings.passwordFormAction) {
    return false;
  }
  sp.i18n.load({
    bundle : 'org.silverpeas.authentication.multilang.authentication',
    async : true
  });
  let $pwdInput = $('#' + settings.passwordInputId);
  $pwdInput.password();
  $('#' + settings.passwordFormId).on("submit", function() {
    let errorStack = [];
    let _self = this;
    let nextValidations = function (checkId) {
      if ($pwdInput.val() === $('#oldPassword').val()) {
        errorStack.push(sp.i18n.get('authentication.password.newMustBeDifferentToOld'));
      }
      if ($pwdInput.val() !== $('#confirmPassword').val()) {
        errorStack.push(sp.i18n.get('authentication.password.different'));
      }
      let $submit = function () {
        if (errorStack.length) {
          jQuery.popup.error(errorStack.join("\n"));
        } else {
          _self.action = settings.passwordFormAction;
          $('#' + settings.passwordFormId).append('<input type="hidden" name="checkId" value="' + checkId + '">')
          _self.submit();
        }
      };
      if (typeof settings.extraValidations === 'function') {
        settings.extraValidations(errorStack).then(function () {
          $submit();
        });
      } else {
        $submit();
      }
    };
    $pwdInput.password('verify', {
      onSuccess: function(checkId) {
        nextValidations(checkId);
      },
      onError: function() {
        errorStack.push(sp.i18n.get('authentication.password.error'));
        nextValidations('');
      }
    });
    return false;
  });

  return true;
}