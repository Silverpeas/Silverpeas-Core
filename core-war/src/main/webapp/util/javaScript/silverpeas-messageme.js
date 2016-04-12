/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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

(function($) {

  $.messageMe = {
    userId: null,
    currentElement: null,
    initialized: false
  };

  /**
   * The user messaging Silverpeas plugin built atop of JQuery.
   * It binds the click events onto the HTML element for which it opens a popup window through which a user
   * can send a message to another one.
   */
  $.fn.messageMe = function(user) {

    if (!this.length)
      return this;

    if (!$.messageMe.initialized) {
      $.i18n.properties({
        name: 'socialNetworkBundle',
        path: webContext + '/services/bundles/org/silverpeas/social/multilang/',
        language: '$$', /* by default the language of the user in the current session */
        mode: 'map'
      });
      prepareMessagingPopup();
      $.messageMe.initialized = true;
    }

    return this.each(function() {
      var $this = $(this), profile = user;
      if (!user.fullName)
        User.get(user.id).then(function(theUser) {
          profile = theUser;
        });
      render($this, profile);
    });
  };

  function render(target, user) {
    target.data('messageMe', true);
    target.click(function() {
      $.messageMe.userId = user.id;
      $.messageMe.currentElement = target;
      $("#notificationDialog").dialog("option", "title", user.fullName);
      $("#notificationDialog").dialog("open");
      return false;
    });
  }

  function closeMessagingPopup() {
    $("#notificationDialog").dialog("close");
    $("#notification-subject").val("");
    $("#notification-message").val("");
  }

  function prepareMessagingPopup() {
    if ($("#notificationDialog").length === 0) {
      $('<div>', {
        'id': 'notificationDialog'
      }).append($('<form>').append($('<table>').append($('<tr>').
              append($('<td>').addClass('txtlibform').append($.i18n.prop('GML.notification.subject') + '&nbsp;:')).
              append($('<td>').append($('<input>', {
                'type': 'text',
                'name': 'textSubject',
                'id': 'notification-subject',
                'maxlength': '1023',
                'value': ''
              }).attr('size', 50)).append('&nbsp;').append($('<img>', {
                'src': webContext + '/util/icons/mandatoryField.gif',
                'width': '5',
                'height': '5',
                'alt': 'mandatoryField'
              })))).append($('<tr>').
              append($('<td>').addClass('txtlibform').append($.i18n.prop('GML.notification.message') + '&nbsp;:')).
              append($('<td>').append($('<textarea>', {
                'name': 'textMessage',
                'id': 'notification-message',
                'cols': '60',
                'rows': '8'
              })))).
              append($('<tr>').append($('<td>', {
                'colspan': 2
              }).append($('<img>', {
                'src': webContext + '/util/icons/mandatoryField.gif',
                'width': '5',
                'height': '5',
                'alt': 'mandatoryField'
              })).append('&nbsp;: ' + $.i18n.prop('GML.requiredField')))))
      ).appendTo($(document.body));

      $("#notificationDialog").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        height: "auto",
        width: 550,
        buttons: [
          {
            text: $.i18n.prop('GML.ok'),
            click: function() {
              var subject = $("#notification-subject").val();
              var message = $("#notification-message").val();
              if ($.trim(subject).length === 0)
                alert($.i18n.prop('GML.thefield') + ' ' + $.i18n.prop('GML.notification.subject') + ' ' + $.i18n.prop('GML.isRequired'));
              else
                $.ajax({
                  url: webContext + '/DirectoryJSON',
                  type: 'GET',
                  data: {
                    Action: 'SendMessage',
                    Title: subject,
                    Message: message,
                    TargetUserId: $.messageMe.userId
                  },
                  dataType: 'json',
                  cache: false,
                  success: function(data, status, jqXHR) {
                    closeMessagingPopup();
                  },
                  error: function(jqXHR, textStatus, errorThrown) {
                    alert(errorThrown);
                  }
                });
            }
          },
          {
            text: $.i18n.prop('GML.cancel'),
            click: function() {
              closeMessagingPopup();
            }
          }
        ]
      });
    }
  }
})(jQuery);

/**
 * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
 * javascript plugin.
 */
jQuery(document).ready(function() {
  jQuery('.notification').each(function(i, element) {
    var userParams = jQuery(element).attr('rel');
    if (userParams && userParams.length > 1) {
      userParams = userParams.split(',');
      if (!jQuery(element).data('messageMe')) {
        jQuery(element).messageMe({
          id: userParams[0],
          fullName: userParams[1]
        });
      }
    }
  });
});