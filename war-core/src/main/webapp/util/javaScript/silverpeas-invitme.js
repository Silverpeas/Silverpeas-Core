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

  $.invitMe = {
    userId: null,
    currentElement: null,
    initialized: false
  };

  /**
   * The user invitation Silverpeas plugin built atop of JQuery.
   * It binds to the elements the click event for which it opens a popup window through which a user
   * can propose another one to make a social relationship.
   */
  $.fn.invitMe = function(user) {

    if (!this.length)
      return this;

    if (!$.invitMe.initialized) {
      $.i18n.properties({
        name: 'socialNetworkBundle',
        path: webContext + '/services/bundles/org/silverpeas/social/multilang/',
        language: '$$', /* by default the language of the user in the current session */
        mode: 'map'
      });
      prepareInvitationPopup();
      $.invitMe.initialized = true;
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
    target.data('invitMe', true);
    target.click(function() {
      $.invitMe.userId = user.id;
      $.invitMe.currentElement = target;
      $("#invitationDialog").dialog("option", "title", user.fullName);
      $("#invitationDialog").dialog("open");
      return false;
    });
  }

  function closeInvitationPopup() {
    $("#invitationDialog").dialog("close");
    $("#invitation-message").val("");
  }

  function prepareInvitationPopup() {
    if ($("#invitationDialog").length === 0) {
      $('<div>', {
        'id': 'invitationDialog'
      }).append($('<form>').append($('<table>').append($('<tr>').
              append($('<td>').addClass('txtlibform').append($.i18n.prop('GML.notification.message') + '&nbsp;:')).
              append($('<td>').append($('<textarea>', {
                'name': 'textMessage',
                'id': 'invitation-message',
                'cols': '60',
                'rows': '8'
              })))))).appendTo($(document.body));

      $("#invitationDialog").dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        height: "auto",
        width: 550,
        buttons: [
          {
            text: $.i18n.prop('GML.ok'),
            click: function() {
              var message = $("#invitation-message").val();
              $.ajax({
                url: webContext + '/InvitationJSON',
                type: 'GET',
                data: {
                  Action: 'SendInvitation',
                  Message: message,
                  TargetUserId: $.invitMe.userId
                },
                dataType: 'json',
                cache: false,
                success: function(data, status, jqXHR) {
                  closeInvitationPopup();
                  try {
                    $.invitMe.currentElement.hide('slow');
                  } catch (e) {
                    //do nothing
                    //As fragment is externalized, class invitation can be missing
                  }
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
              closeInvitationPopup();
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
  jQuery('.invitation').each(function(i, element) {
    var userParams = jQuery(element).attr('rel');
    if (userParams && userParams.length > 1) {
      userParams = userParams.split(',');
      if (!jQuery(element).data('invitMe')) {
        jQuery(element).invitMe({
          id: userParams[0],
          fullName: userParams[1]
        });
      }
    }
  });
});
