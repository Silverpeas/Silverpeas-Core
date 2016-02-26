/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Setting password UI & behaviour and form submit.
 * @param params
 * @return {boolean}
 */
function handlePasswordForm(params) {
  var settings = $.extend({
    passwordFormId: '',
    passwordInputId: '',
    passwordFormAction: ''
  }, params);
  if (!settings.passwordFormId || !settings.passwordInputId || !settings.passwordFormAction) {
    return false;
  }
  $.i18n.properties({
    name: 'authentication',
    path: webContext + '/services/bundles/org/silverpeas/authentication/multilang/',
    language: '$$', /* by default the language of the user in the current session */
    mode: 'map'
  });

  var $pwdInput = $('#' + settings.passwordInputId);
  $pwdInput.password();
  $('#' + settings.passwordFormId).on("submit", function() {
    var errorMsg = "";
    if ($pwdInput.val() === $('#oldPassword').val()) {
      errorMsg += "- " + $.i18n.prop('authentication.password.newMustBeDifferentToOld') + "\n";
    }
    $pwdInput.password('verify', {onError: function() {
        errorMsg += "- " + $.i18n.prop('authentication.password.error') + "\n";
      }});
    if ($pwdInput.val() !== $('#confirmPassword').val()) {
      errorMsg += "- " + $.i18n.prop('authentication.password.different') + "\n";
    }
    if (errorMsg) {
      alert(errorMsg);
      return false;
    }
    this.action = settings.passwordFormAction;
    return true;
  });

  return true;
}