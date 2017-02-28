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
    user: null,
    invitation: null,
    currentElement: null,
    initialized: false
  };

  var LABEL_OK = RelationshipBundle.get("GML.ok");
  var LABEL_CANCEL = RelationshipBundle.get("GML.cancel");
  var LABEL_YES = RelationshipBundle.get("GML.yes");
  var LABEL_NO = RelationshipBundle.get("GML.no");
  var LABEL_MESSAGE = RelationshipBundle.get("GML.notification.message");

  var LABEL_CANCEL_TITLE = RelationshipBundle.get("myProfile.invitations.dialog.cancel.title");
  var KEY_CANCEL_MESSAGE = "myProfile.invitations.dialog.cancel.message";
  var KEY_CANCEL_SUCCESS = "myProfile.invitations.cancel.feedback";

  var LABEL_IGNORE_TITLE = RelationshipBundle.get("myProfile.invitations.dialog.ignore.title");
  var KEY_IGNORE_MESSAGE = "myProfile.invitations.dialog.ignore.message";
  var KEY_IGNORE_SUCCESS = "myProfile.invitations.ignore.feedback";

  var KEY_INVITE_SUCCESS = "myProfile.invitations.sent.feedback";

  var LABEL_ACCEPT_TITLE = RelationshipBundle.get("myProfile.invitations.dialog.accept.title");
  var KEY_ACCEPT_MESSAGE = "myProfile.invitations.dialog.accept.message";
  var KEY_ACCEPT_SUCCESS = "myProfile.invitations.accept.feedback";

  var KEY_REMOVE_SUCCESS = "myProfile.relations.delete.feedback";
  var LABEL_REMOVE_TITLE = RelationshipBundle.get("myProfile.relations.dialog.delete.title");
  var KEY_REMOVE_MESSAGE = "myProfile.relations.dialog.delete.message";

  var actions = {
    sendInvitation:function(options) {
      __handleInvitation($(this), options);
    },
    cancelInvitation:function(options) {
      __handleInvitationCancel($(this), options);
    },
    acceptInvitation:function(options) {
      __handleInvitationAccept($(this), options);
    },
    deleteRelation:function(options) {
      __handleRelationDelete($(this), options);
    }
  };

  /**
   * The user invitation Silverpeas plugin built atop of JQuery.
   * It binds to the elements the click event for which it opens a popup window through which a user
   * can propose another one to make a social relationship.
   */
  $.fn.invitMe = function(action, param) {
    var method;
    var options;
    if (actions[action]) {
      method = actions[action];
      options = param;
    } else {
      return $.error('Method ' + action + ' does not exist on jQuery.invitMe');
    }

    if (!this.length)
      return this;

    if (!$.invitMe.initialized) {
      $.invitMe.initialized = true;
      preparePopups(options);
    }

    return this.each(function() {
      var $this = $(this);
      $this.data('invitMe', true);
      method.call($this, options);
    });
  };

  function __handleInvitation(target, options) {
    target.click(function() {
      var displayDialog = function(user) {
        $.invitMe.user = user;
        $.invitMe.currentElement = target;
        $("#invitationDialog").dialog("option", "title", user.fullName);
        $("#invitationDialog").dialog("open");
      };
      if (!options.user.fullName) {
        User.get(options.user.id).then(function(theUser) {
          displayDialog(theUser);
        });
      } else {
        displayDialog(options.user);
      }
      return false;
    });
  }

  function __handleInvitationCancel(target, options) {
    target.click(function() {
      __loadInvitation(options).then(function(invitation) {
        var isCurrentUserSender = invitation.senderId == currentUserId;
        var userId = isCurrentUserSender ? invitation.receiverId : invitation.senderId;
        User.get(userId).then(function(theUser) {
          invitation._user = theUser;
          invitation._isCurrentUserSender = isCurrentUserSender;
          $.invitMe.invitation = invitation;
          $.invitMe.currentElement = target;
          var label = isCurrentUserSender ? LABEL_CANCEL_TITLE : LABEL_IGNORE_TITLE;
          $("#invitationCancelDialog").dialog("option", "title", label);
          var messageKey = isCurrentUserSender ? KEY_CANCEL_MESSAGE : KEY_IGNORE_MESSAGE;
          $("#invitationCancelDialog p").html(RelationshipBundle.get(messageKey, theUser.fullName));
          $("#invitationCancelDialog").dialog("open");
        });
      });
      return false;
    });
  }

  function __handleInvitationAccept(target, options) {
    target.click(function() {
      __loadInvitation(options).then(function(invitation) {
        User.get(invitation.senderId).then(function(theUser) {
          invitation._user = theUser;
          $.invitMe.invitation = invitation;
          $.invitMe.currentElement = target;
          $("#invitationAcceptDialog").dialog("option", "title", LABEL_ACCEPT_TITLE);
          $("#invitationAcceptDialog p").html(RelationshipBundle.get(KEY_ACCEPT_MESSAGE, theUser.fullName));
          $("#invitationAcceptDialog").dialog("open");
        });
      });
      return false;
    });
  }

  function __handleRelationDelete(target, options) {
    target.click(function() {
      var displayDialog = function(user) {
        $.invitMe.user = user;
        $.invitMe.currentElement = target;
        $("#relationDeleteDialog").dialog("option", "title", LABEL_REMOVE_TITLE);
        $("#relationDeleteDialog p").html(RelationshipBundle.get(KEY_REMOVE_MESSAGE, user.fullName));
        $("#relationDeleteDialog").dialog("open");
      };
      if (!options.user.fullName) {
        User.get(options.user.id).then(function(theUser) {
          displayDialog(theUser);
        });
      } else {
        displayDialog(options.user);
      }
      return false;
    });
  }

  function __loadInvitation(options) {
    return new Promise(function(resolve, reject) {
      if (!options.invitation.receiverId) {
        var ajaxConfig = sp.ajaxConfig(webContext+"/services/invitations/"+options.invitation.id);
        silverpeasAjax(ajaxConfig).then(function (request) {
          resolve(JSON.parse(request.responseText));
        });
      } else {
        resolve(options.invitation);
      }
    });
  }

  function closeInvitationPopup() {
    $("#invitationDialog").dialog("close");
    $("#invitation-message").val("");
  }

  function closeInvitationCancelPopup() {
    $("#invitationCancelDialog").dialog("close");
  }

  function closeInvitationAcceptPopup() {
    $("#invitationAcceptDialog").dialog("close");
  }

  function closeRelationDeletePopup() {
    $("#relationDeleteDialog").dialog("close");
  }

  function preparePopups(options) {
    if ($("#invitationDialog").length === 0) {
      $('<div>', {
        'id': 'invitationDialog'
      }).append($('<form>').append($('<table>').append($('<tr>').
              append($('<td>').addClass('txtlibform').append(LABEL_MESSAGE + '&nbsp;:')).
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
            text: LABEL_OK,
            click: function() {
              var invitation = {
                "receiverId": $.invitMe.user.id,
                "message": $("#invitation-message").val()
              };

              silverpeasAjax({
                method: "POST",
                url: webContext + "/services/invitations",
                headers: {"Content-Type": "application/json"},
                data: JSON.stringify(invitation)
              }).then(function(request) {
                closeInvitationPopup();
                try {
                  $.invitMe.currentElement.remove();
                  notySuccess(RelationshipBundle.get(KEY_INVITE_SUCCESS, $.invitMe.user.fullName));
                  if (options.callback) {
                    options.callback.call(undefined, $.invitMe.user);
                  }
                } catch (e) {
                  //do nothing
                  //As fragment is externalized, class invitation can be missing
                }
              }, function(data) {
                notyError(data.error);
              });
            }
          },
          {
            text: LABEL_CANCEL,
            click: function() {
              closeInvitationPopup();
            }
          }
        ]
      });
    }

    if ($("#invitationCancelDialog").length === 0) {
      $('<div>', {
        'id' : 'invitationCancelDialog'
      }).append($('<p>')).appendTo($(document.body));

      $( "#invitationCancelDialog" ).dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        buttons: [{
          text : LABEL_YES,
          click: function() {
            silverpeasAjax({
              method: "DELETE",
              url: webContext + "/services/invitations/"+$.invitMe.invitation.id,
            }).then(function(request) {
              closeInvitationCancelPopup();
              try {
                var invitation = $.invitMe.invitation;
                $.invitMe.currentElement.remove();
                var successKey = invitation._isCurrentUserSender ? KEY_CANCEL_SUCCESS : KEY_IGNORE_SUCCESS;
                notySuccess(RelationshipBundle.get(successKey, invitation._user.fullName));
                if (options.callback) {
                  options.callback.call(undefined, invitation);
                }
              } catch (e) {
                //do nothing
                //As fragment is externalized, class invitation can be missing
              }
            }, function(data) {
              notyError(data.error);
            });
          }
        },
          {
            text: LABEL_NO,
            click: function() {
              closeInvitationCancelPopup();
            }
          }]
      });
    }

    if ($("#invitationAcceptDialog").length === 0) {
      $('<div>', {
        'id' : 'invitationAcceptDialog'
      }).append($('<p>')).appendTo($(document.body));

      $( "#invitationAcceptDialog" ).dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        buttons: [{
          text : LABEL_YES,
          click: function() {
            silverpeasAjax({
              method: "PUT",
              url: webContext + "/services/invitations/"+$.invitMe.invitation.id,
            }).then(function(request) {
              closeInvitationAcceptPopup();
              try {
                var invitation = $.invitMe.invitation;
                $.invitMe.currentElement.remove();
                notySuccess(RelationshipBundle.get(KEY_ACCEPT_SUCCESS, invitation._user.fullName));
                if (options.callback) {
                  options.callback.call(undefined, invitation);
                }
              } catch (e) {
                //do nothing
                //As fragment is externalized, class invitation can be missing
              }
            }, function(data) {
              notyError(data.error);
            });
          }
        },
          {
            text: LABEL_NO,
            click: function() {
              closeInvitationAcceptPopup();
            }
          }]
      });
    }

    if ($("#relationDeleteDialog").length === 0) {
      $('<div>', {
        'id' : 'relationDeleteDialog'
      }).append($('<p>')).appendTo($(document.body));

      $( "#relationDeleteDialog" ).dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        buttons: [{
          text : LABEL_YES,
          click: function() {
            closeRelationDeletePopup();
            silverpeasAjax({
              method: "DELETE",
              url: "/silverpeas/services/relations/"+$.invitMe.user.id
            }).then(function(request) {
              $.invitMe.currentElement.remove();
              notySuccess(RelationshipBundle.get(KEY_REMOVE_SUCCESS, $.invitMe.user.fullName));
            }, function(data) {
              notyError(data.error);
            });
          }
        },
          {
            text: LABEL_NO,
            click: function() {
              closeRelationDeletePopup();
            }
          }]
      });
    }
  }
})(jQuery);

/**
 * Using "jQuery" instead of "$" at this level prevents of getting conflicts with another
 * javascript plugin.
 */
jQuery(document).ready(function() {
  jQuery('.invitation, .delete-relation').each(function(i, element) {
    var userParams = jQuery(element).attr('rel');
    if (userParams && userParams.length > 1) {
      userParams = userParams.split(',');
      if (!jQuery(element).data('invitMe')) {
        var options = {
          user : {
            id : userParams[0],
            fullName : userParams[1]
          }
        };
        if (jQuery(element).hasClass('invitation')) {
          jQuery(element).invitMe('sendInvitation', options);
        } else {
          jQuery(element).invitMe('deleteRelation', options);
        }

      }
    }
  });

  jQuery('.cancel-invitation, .accept-invitation').each(function(i, element) {
    var options = {};
    var params = jQuery(element).attr('rel');
    if (params) {
      var invitationId = params;
      var callbackFunction;
      params = params.split(',');
      if (params.length > 1) {
        invitationId = params[0];
        callbackFunction = params[1];
      }
      options.invitation = {
        id: invitationId
      };
      if (callbackFunction) {
        options.callback = window[callbackFunction];
      }

      if (!jQuery(element).data('invitMe')) {
        if (jQuery(element).hasClass('cancel-invitation')) {
          jQuery(element).invitMe('cancelInvitation', options);
        } else {
          jQuery(element).invitMe('acceptInvitation', options);
        }
      }
    }
  });
});