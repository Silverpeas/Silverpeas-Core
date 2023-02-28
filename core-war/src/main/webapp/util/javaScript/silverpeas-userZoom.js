// noinspection HtmlRequiredAltAttribute,RequiredAttributes,JSUnresolvedVariable

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

  const __getLabel = function(key) {
    return sp.i18n.get(key);
  };

  /**
   * The structure with information about the current tooltip rendered with some data on a given
   * user:
   * - the HTML element as the current rendered tooltip,
   * - the current user session within which the WEB page with this plugin is used,
   * - the HTML element as parent for all the defined tooltip, that is the target for the plugin,
   * - a flag indicating if the plugin is initialized.
   *
   * At initialization, the plugin loads the profile of the user in the current WEB session and
   * enriches it with additional functions related to its contacts and to its invitations sent to
   * others users.
   */
  $.userZoom = {
    currentTooltip: null,
    currentUser: null,
    target: null,
    initialized: false,
    clear: function() {
      if (this.currentTooltip) {
        this.currentTooltip.hide();
        this.currentTooltip.remove();
      }
      this.currentTooltip = null;
      this.target = null;
    },
    set: function(target, aTooltip) {
      this.clear();
      this.currentTooltip = aTooltip;
      this.target = target;
    },
    initialize: function() {
      const self = this;
      User.get('me').then(function(me) {
        self.currentUser = me;
        self.currentUser.hasRelationWith = function(aUser) {
          return aUser.relationships().then(function(contacts) {
            for (let i = 0; i < contacts.length; i++) {
              let contact = contacts[i];
              if (me.id === contact.id) {
                return true;
              }
            }
            return false;
          });
        };

        self.currentUser.getInvitationWith = function(aUser) {
          return self.currentUser.myInvitationsPromise.then(function(invitations) {
            for (let i = 0; i < invitations.length; i++) {
              let invitation = invitations[i];
              if (invitation.receiverId.toString() === aUser.id || invitation.senderId.toString() === aUser.id) {
                return invitation;
              }
            }
            return undefined;
          });
        };

        self.currentUser.onMyInvitations = function() {
          const promises = [];
          promises.push(silverpeasAjax(sp.ajaxRequest(webContext + '/services/invitations/inbox')));
          promises.push(silverpeasAjax(sp.ajaxRequest(webContext + '/services/invitations/outbox')));
          return self.currentUser.myInvitationsPromise =
              sp.promise.whenAllResolved(promises).then(function(requests) {
                const invitations = [];
                requests.forEach(function(request) {
                  Array.prototype.push.apply(invitations, request.responseAsJson());
                });
                return invitations;
              });
        };
      });

      this.initialized = true;
    }
  };

  let currentTarget = null;
  let settingHandlersDone = false;

  /**
   * Open the Silverpeas chat WEB page.
   */
  function chatWith(user) {
    if (user.connected) {
      SilverChat.gui.openChatWindow(user.chatId, user.fullName);
    }
  }

  /**
   * Returns the HTML element with which the user status information is displayed.
   */
  function connectionStatus(user) {
    let onlineStatus;
    let onlineStatusAlt;
    if (user.connected) {
      onlineStatus = webContext + '/util/icons/online.gif';
      onlineStatusAlt = __getLabel('connected');
    } else {
      onlineStatus = webContext + '/util/icons/offline.gif';
      onlineStatusAlt = __getLabel('notConnected');
    }
    return $('<img>', {
      src: onlineStatus,
      alt: onlineStatusAlt,
      title: onlineStatusAlt
    });
  }

  /**
   * Returns the HTML element with the user interaction tool (including links to chat, to send
   * messages, to send an invitation, ...)
   */
  function interactionWith(user) {
    let disabledCss = '', interactionBox = $('<div>').addClass('userzoom-tooltip-interaction');
    let interactionActions = $('<div>').addClass('userzoom-tooltip-interaction-action');
    if (!user.connected) {
      disabledCss = ' disabled';
    }
    interactionBox.append(interactionActions);
    interactionBox.append($('<a>', {
      href: user.webPage
    }).addClass('userzoom-tooltip-interaction-accessProfil').append($('<span>').append(__getLabel('myProfile.tab.profile')))).
    append($('<a>', {
      href: '#'
    }).addClass('userzoom-tooltip-interaction-accessNotification notification').append($('<span>').append(__getLabel('ToContact'))).click(function() {
      sp.messager.open('', {recipientUsers: user.id, recipientEdition: false});
      $.userZoom.clear();
    }));
    if (user.chatEnabled) {
      interactionBox.addClass('chat-enabled').append($('<a>', {
        href: '#'
      }).addClass('userzoom-tooltip-interaction-accessChat' + disabledCss).append($('<span>').append(__getLabel('chat'))).click(function() {
        chatWith(user);
        $.userZoom.clear();
      }));
    }
    interactionBox.append($('<div>').addClass('userzoom-tooltip-arrow'));

    const appendRelationActions = function(contact) {
      if (contact) {
        let $link = $('<a>', {href : '#'}).addClass('userzoom-tooltip-interaction-action-relation');
        $link.addClass('delete-relation');
        $link.append($('<span>').append(__getLabel('relation.delete'))).relationShip(
            'deleteRelation', {user : contact});
        interactionActions.append($link);
      }
    };

    const appendInvitationActions = function(invitation) {
      let $link = $('<a>', {href : '#'}).addClass('userzoom-tooltip-interaction-action-invitation');
      if (!invitation) {
        $link.addClass('invitation');
        $link.append($('<span>').append(__getLabel('invitation.send'))).relationShip(
            'sendInvitation', {user : user});
      } else {
        if ($.userZoom.currentUser.id === invitation.receiverId) {
          $link.addClass('view-invitation');
          $link.append($('<span>').append(__getLabel('invitation.view'))).relationShip(
              'viewInvitation', {invitation : invitation});
        } else {
          $link.addClass('cancel-invitation');
          $link.append($('<span>').append(__getLabel('invitation.cancel'))).relationShip(
              'cancelInvitation', {invitation : invitation});
        }
      }
      interactionActions.append($link);
    };

    $.userZoom.currentUser.hasRelationWith(user).then(function(isRelation) {
      if (isRelation) {
        appendRelationActions(user);
      } else {
        $.userZoom.currentUser.getInvitationWith(user).then(function(invitation) {
          appendInvitationActions(invitation)
        })
      }
    });

    return interactionBox;
  }

  /**
   * Adjusts the position of the given tooltip associated to the specified target.
   */
  function __adjustingPositions(aTooltip, target) {
    let picto = $('.userzoom-infoConnection img', target);
    let tooltipEdgeXOffset = __getCachedArrowCss("right") + picto.width();
    let tooltipEdgeYOffset = __getCachedArrowCss("height");
    aTooltip.position({
      of : target,
      at : 'right-' + tooltipEdgeXOffset + ' bottom+' + tooltipEdgeYOffset,
      my : 'left top',
      collision : 'flip'
    });
    let targetPosition = target.offset(), tooltipPosition = aTooltip.offset();
    let position = (targetPosition.top > tooltipPosition.top ? 'above' : 'below');
    let isTooltipArrowOnRight = targetPosition.left > tooltipPosition.left;
    let toolTipClass = position + (isTooltipArrowOnRight ? ' right' : ' left');
    if (isTooltipArrowOnRight) {
      let paddingAndMarginLeftOffset = Number(target.css('padding-left').replace(/[^0-9]/g, '')) +
          Number(target.css('margin-left').replace(/[^0-9]/g, ''));
      aTooltip.position({
        of : target,
        at : 'left bottom+' + tooltipEdgeYOffset,
        my : 'right+' + (tooltipEdgeXOffset + paddingAndMarginLeftOffset) + ' top',
        collision : 'flip'
      });
    }
    aTooltip.addClass(toolTipClass);
  }

  /**
   * Gets the arrow X offset (so css value can be changed... awesome)
   * @return {*}
   * @private
   */
  function __getCachedArrowCss(css) {
    let $dataContainer = $(document);
    let dataCssKey = 'tooltip-arrow-' + css + '-value';
    let cachedArrowCssValue = $dataContainer.data(dataCssKey);
    if (typeof cachedArrowCssValue === 'undefined') {
      try {
        let $arrow = $('<div>', {"class" : "userzoom-tooltip-arrow", "style" : "display:none"});
        let $hiddenForComputing = $('<div>', {"class" : "above"}).append($arrow);
        $(document.body).append($hiddenForComputing);
        cachedArrowCssValue = $arrow.css(css).replace(/[^0-9]/g, '');
      } catch (e) {
        cachedArrowCssValue = css === 'right' ? 30 : 7;
      }
      $dataContainer.data(dataCssKey, cachedArrowCssValue);
    }
    return Number(cachedArrowCssValue);
  }

  /**
   * The user presential Silverpeas plugin based on JQuery.
   * This JQuery plugin renders a tooltip with status information about the user and from which
   * anyone can establish a communication with him. The tooltip is displayed when the mouse hovers
   * above the target HTML element.
   */
  $.fn.userZoom = function(user) {

    if (!this.length)
      return this;

    if (!$.userZoom.initialized) {
      $.userZoom.initialized = true;
      sp.i18n.load({
        bundle: 'org.silverpeas.social.multilang.socialNetworkBundle',
        async: true
      }).then(function() {
        $.userZoom.initialize();
      });
    }

    return this.each(function() {
      let profile = user, $this = $(this);
      $this.data('userZoom', new Date());
      if (!(profile.fullName && profile.lastName && profile.avatar && profile.status !== null &&
              profile.status !== undefined && profile.connected !== null && profile.connected !== undefined)) {
        User.get(user.id).then(function(theUser) {
          profile = theUser;
          if (!profile.system && !profile.deletedState && !profile.removedState && !profile.deactivatedState) {
            render($this, profile);
          }
        });
      }
    });
  };

  /**
   * Animation of the target that is hovered.
   * @param target
   * @param isIn
   * @private
   */
  function __animate(target, isIn) {
    if (typeof isIn === 'undefined') {
      isIn = true;
    }
    let nextState = isIn ? {opacity : "-=0.5"} : {opacity : "+=0.5"};
    let currentDisplayed = ($.userZoom.target && $.userZoom.target === target);
    if (currentDisplayed && isIn) {
      return;
    }
    target.animate(nextState, 250, function() {
      if (__isRenderingInProgress(target) || isIn) {
        __animate(target, !isIn);
      }
    });
  }

  /**
   * Marking that the rendering of the popin associated to the given target is in progress.
   * @param target the aimed target.
   * @private
   */
  function __markRenderingInProgress(target) {
    target.data('rendering_in_progress', true);
  }

  /**
   * Marking that the rendering of the popin associated to the given target is done (diplayed or
   * not).
   * @param target the aimed target.
   * @private
   */
  function __markRenderingDone(target) {
    target.data('rendering_in_progress', false);
  }

  /**
   * Indicates id the rendering of the popin associated to the given target is in progress.
   * @param target the aimed target.
   * @return {boolean|*}
   * @private
   */
  function __isRenderingInProgress(target) {
    let isRenderInProgress = target.data('rendering_in_progress');
    return (typeof isRenderInProgress !== 'undefined' && isRenderInProgress);
  }

  /**
   * Renders into the specified target a userZoom tooltip for the specified user.
   * The tooltip is bound to the mouse pointer movement:
   * - it is displayed when the mouse hovers above the target,
   * - it is removed when the mouse moves away the tooltip.
   */
  function render(target, user) {
    let status = connectionStatus(user);
    target.append('&nbsp;').append($('<span>').addClass('userzoom-infoConnection').append(status)).hover(function() {
      if (__isRenderingInProgress(target)) {
        return;
      }
      __markRenderingInProgress(target);
      __animate(target);
      setTimeout(function() {
        let $currentTarget = $(currentTarget);
        if (!$(currentTarget).hasClass('userToZoom')) {
         $currentTarget = $(currentTarget).parents("span.userToZoom");
        }
        if (!__isRenderingInProgress(target) || $currentTarget[0] !== target[0] ||
            ($.userZoom.target && $.userZoom.target === target)) {
          __markRenderingDone(target);
          return;
        }
        $.userZoom.currentUser.onMyInvitations().then(function() {
          let element = tooltip(target, user);
          $.userZoom.set(target, element);
          __markRenderingDone(target);
        });
      }, 750);
    }, function(){
      __markRenderingDone(target);
    });
    if (!settingHandlersDone) {
      $(document).mousedown(function(event) {
        if ($.userZoom.currentTooltip !== null && $.userZoom.currentTooltip !== undefined) {
          let eventTarget = $(event.target);
          if (!eventTarget.hasClass('userzoom-tooltip') &&
              eventTarget.parents('.userzoom-tooltip').length === 0) {
            $.userZoom.clear();
          }
        }
      });
      $(document).mousemove(function(event) {
        currentTarget = event.target;
      });
      settingHandlersDone = true;
    }
  }

  /**
   * Creates into the specified target the tooltip with short information about the specified user
   * and with an interaction tool to communicate with him through Silverpeas.
   */
  function tooltip(target, user) {
    let userinfo = $('<div>').addClass('userzoom-tooltip').
            append($('<div>').addClass('userzoom-tooltip-profilPhoto profilPhoto').
                    append($('<a>', {
                      href: user.webPage
                    }).append($('<img>', {
                      src: user.avatar,
                      alt: 'viewUser'
                    }).addClass('avatar')))).
            append($('<div>').addClass('userzoom-tooltip-info').
                    append($('<div>').addClass('userzoom-tooltip-info-userName').append($('<a>', {
                      href: user.webPage
                    }).append(user.fullName))).
                    append($('<div>').addClass('userzoom-tooltip-info-infoConnection').append(connectionStatus(user))).
                    append($('<div>').addClass('userzoom-tooltip-info-status').append(user.status))).
            append(interactionWith(user)).
            appendTo($(document.body));
    __adjustingPositions(userinfo, target);
    return userinfo;
  }

})(jQuery);

function activateUserZoom() {
  //activateMessageMe();
  activateRelationShip();
  jQuery(document).ready(function() {
    jQuery('.userToZoom').each(function() {
      let $this = jQuery(this);
      if (!$this.data('userZoom')) {
        $this.userZoom({
          id : $this.attr('rel')
        });
      }
    });
  });
}

/**
 * Using "jQuery" instead of "$" at this level prevents of getting conflicts with another
 * javascript plugin.
 */
activateUserZoom();