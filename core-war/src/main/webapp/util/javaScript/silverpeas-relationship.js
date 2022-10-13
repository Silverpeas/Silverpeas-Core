/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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

(function($) {

  $.relationShip = {
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
    sendInvitation : function(options) {
      __handleInvitation($(this), options);
    },
    viewInvitation : function(options) {
      __handleInvitationView($(this), options);
    },
    cancelInvitation : function(options) {
      __handleInvitationCancel($(this), options);
    },
    acceptInvitation : function(options) {
      __handleInvitationAccept($(this), options);
    },
    deleteRelation : function(options) {
      __handleRelationDelete($(this), options);
    }
  };

  /**
   * The user invitation Silverpeas plugin built atop of JQuery.
   * It binds to the elements the click event for which it opens a popup window through which a user
   * can propose another one to make a social relationship.
   */
  $.fn.relationShip = function(action, param) {
    var method;
    var options;
    if (actions[action]) {
      method = actions[action];
      options = param;
    } else {
      return $.error('Method ' + action + ' does not exist on jQuery.relationShip');
    }

    if (!this.length)
      return this;

    if (!$.relationShip.initialized) {
      $.relationShip.initialized = true;
      preparePopups(options);
    }

    return this.each(function() {
      var $this = $(this);
      $this.data('relationShip', true);
      method.call($this, options);
    });
  };

  function __handleInvitation(target, options) {
    target.click(function() {
      var displayDialog = function(user) {
        $.relationShip.user = user;
        $.relationShip.currentElement = target;
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

  function __handleInvitationView(target, options) {
    target.click(function() {
      silverpeasFormSubmit(sp.formConfig(webContext + '/RMyProfil/jsp/MyInvitations'));
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
          $.relationShip.invitation = invitation;
          $.relationShip.currentElement = target;
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
          $.relationShip.invitation = invitation;
          $.relationShip.currentElement = target;
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
        $.relationShip.user = user;
        $.relationShip.currentElement = target;
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
    if (!options.invitation.receiverId) {
      var ajaxConfig = sp.ajaxConfig(webContext+"/services/invitations/"+options.invitation.id);
      return silverpeasAjax(ajaxConfig).then(function (request) {
        return request.responseAsJson();
      });
    } else {
      return sp.promise.resolveDirectlyWith(options.invitation);
    }
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
                "receiverId": $.relationShip.user.id,
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
                  $.relationShip.currentElement.remove();
                  notySuccess(RelationshipBundle.get(KEY_INVITE_SUCCESS, $.relationShip.user.fullName));
                  if (options.callback) {
                    options.callback.call(undefined, $.relationShip.user);
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
              url: webContext + "/services/invitations/"+$.relationShip.invitation.id,
            }).then(function(request) {
              closeInvitationCancelPopup();
              try {
                var invitation = $.relationShip.invitation;
                $.relationShip.currentElement.remove();
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
              url: webContext + "/services/invitations/"+$.relationShip.invitation.id,
            }).then(function(request) {
              closeInvitationAcceptPopup();
              try {
                var invitation = $.relationShip.invitation;
                $.relationShip.currentElement.remove();
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
            var relationUser = $.relationShip.user;
            silverpeasAjax({
              method: "DELETE",
              url: webContext + "/services/relations/"+relationUser.id
            }).then(function(request) {
              try {
                $.relationShip.currentElement.remove();
                notySuccess(RelationshipBundle.get(KEY_REMOVE_SUCCESS, relationUser.fullName));
                if (options.callback) {
                  options.callback.call(undefined, relationUser);
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
              closeRelationDeletePopup();
            }
          }]
      });
    }
  }
})(jQuery);

function activateRelationShip() {
  jQuery(document).ready(function() {
    jQuery('.invitation, .delete-relation').each(function(i, element) {
      var params = jQuery(element).attr('rel');
      if (params && params.length > 1) {
        params = params.split(',');
        if (!jQuery(element).data('relationShip')) {
          var options = {
            user : {
              id : params[0],
              fullName : params[1]
            }
          };
          if (params.length > 2) {
            var callbackFunction = params[2];
            if (callbackFunction && typeof window[callbackFunction] === 'function') {
              options.callback = window[callbackFunction];
            }
          }
          if (jQuery(element).hasClass('invitation')) {
            jQuery(element).relationShip('sendInvitation', options);
          } else {
            jQuery(element).relationShip('deleteRelation', options);
          }

        }
      }
    });

    jQuery('.view-invitation, .cancel-invitation, .accept-invitation').each(function(i, element) {
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
        if (callbackFunction && typeof window[callbackFunction] === 'function') {
          options.callback = window[callbackFunction];
        }

        if (!jQuery(element).data('relationShip')) {
          if (jQuery(element).hasClass('view-invitation')) {
            jQuery(element).relationShip('viewInvitation', options);
          } else if (jQuery(element).hasClass('cancel-invitation')) {
            jQuery(element).relationShip('cancelInvitation', options);
          } else {
            jQuery(element).relationShip('acceptInvitation', options);
          }
        }
      }
    });
  });
}

/**
 * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
 * javascript plugin.
 */
activateRelationShip();