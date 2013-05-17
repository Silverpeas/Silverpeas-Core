/*
 * Copyright (C) 2000 - 2012 Silverpeas
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

  /**
   * The structure with information about the current tooltip rendered with some data on a given user:
   * - the HTML element as the current rendered tooltip,
   * - the current user session within which the WEB page with this plugin is used,
   * - the HTML element as parent for all the defined tooltip, that is the target for the plugin,
   * - a flag indicating if the plugin is initialized.
   *
   * At initialization, the plugin loads the profile of the user in the current WEB session and
   * enrichs it with additional functions related to its contacts and to its invitations sent to
   * others users.
   */
  $.userZoom = {
    currentTooltip: null,
    currentUser: null,
    target: null,
    initialized: false,
    clear: function() {
      if (this.currentTooltip != null) {
        this.currentTooltip.hide();
        this.currentTooltip.remove();
      }
      this.currentTooltip = null;
      this.target = null;
    },
    set: function(target, tooltip) {
      this.clear();
      this.currentTooltip = tooltip;
      this.target = target;
    },
    initialize: function() {
      this.currentUser = new UserProfile({
        id: 'me'
      }).load();
      this.currentUser.isInMyContacts = function(user) {
        var contacts = user.relationships();
        for (var i in contacts) {
          if (this.id == contacts[i].id) {
            return true;
          }
        }
        return false;
      };

      this.currentUser.isInMyInvitations = function(user) {
        var invitations = this.sentInvitations;
        for (var i in invitations) {
          if (invitations[i].receiverId == user.id) {
            return true;
          }
        }
        return false;
      }

      this.currentUser.onMySentInvitations = function(callback) {
        $.ajax({
          url: webContext + '/services/invitations/outbox',
          type: 'GET',
          dataType: 'json',
          cache: false,
          success: function(invitations, status, jqXHR) {
            this.sentInvitations = invitations;
            if (callback)
              callback(invitations);
          },
          error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          }
        });
      }

      this.initialized = true;
    }
  };

  var currentTarget = null;

  /**
   * Open a new WEB page into another browser window.
   */
  function openWindow(url, windowName, width, height, options) {
    var top = (screen.height - height) / 2;
    var left = (screen.width - width) / 2;
    window.open(url, windowName, "top=" + top + ",left=" + left + ",width=" + width + ",height=" + height + "," + options);
  }

  /**
   * Open the Silverpeas tchat WEB page.
   */
  function tchatWith(user) {
    openWindow(user.tchatPage, 'popupDiscussion' + user.id, '650', '460', 'menubar=no,scrollbars=no,statusbar=no');
  }

  /**
   * Returns the HTML element with which the user status information is displayed.
   */
  function connectionStatus(user) {
    if (user.connected) {
      var onlineStatus = webContext + '/util/icons/online.gif';
      var onlineStatusAlt = $.i18n.prop('connected');
    } else {
      var onlineStatus = webContext + '/util/icons/offline.gif';
      var onlineStatusAlt = $.i18n.prop('notConnected');
    }
    return $('<img>', {
      src: onlineStatus,
      alt: onlineStatusAlt
    });
  }

  /**
   * Returns the HTML element with the user interation tool (including links to tchat, to send
   * messages, to send an invitation, ...)
   */
  function interactionWith(user) {
    var disabledCss = '', interactionBox = $('<div>').addClass('userzoom-tooltip-interaction');
    if (!user.connected)
      disabledCss = ' disabled';
    if (!$.userZoom.currentUser.isInMyContacts(user) && !$.userZoom.currentUser.isInMyInvitations(user))
      interactionBox.append($('<div>').addClass('userzoom-tooltip-interaction-action').append($('<a>', {
        href: '#'
      }).addClass('userzoom-tooltip-interaction-action-invitation invitation').append($('<span>').append($.i18n.prop('invitation.send'))).invitMe(user)));
    interactionBox.append($('<a>', {
      href: user.webPage
    }).addClass('userzoom-tooltip-interaction-accessProfil').append($('<span>').append($.i18n.prop('myProfile.tab.profile')))).
            append($('<a>', {
      href: '#'
    }).addClass('userzoom-tooltip-interaction-accessNotification notification').append($('<span>').append($.i18n.prop('ToContact'))).messageMe(user)).
            append($('<a>', {
      href: '#'
    }).addClass('userzoom-tooltip-interaction-accessTchat' + disabledCss).append($('<span>').append($.i18n.prop('tchat'))).click(function() {
      if (user.connected)
        tchatWith(user);
    })).
            append($('<div>').addClass('userzoom-tooltip-arrow'));

    return interactionBox;
  }

  /**
   * Computes the relative position of the tooltip with the specified target.
   */
  function positionBetween(tooltip, target) {
    var targetPosition = target.offset(), tooltipPosition = tooltip.offset();
    var position = (targetPosition.top > tooltipPosition.top ? 'above' : 'below');
    return position + (targetPosition.left > tooltipPosition.left ? ' right' : ' left');
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
      $.i18n.properties({
        name: 'socialNetworkBundle',
        path: webContext + '/services/bundles/com/silverpeas/social/multilang/',
        language: '$$', /* by default the language of the user in the current session */
        mode: 'map'
      });
      $.userZoom.initialize();
    }

    return this.each(function() {
      var profile = user, $this = $(this);
      $this.data('userZoom', new Date());
      if (!(profile instanceof UserProfile)) {
        profile = new UserProfile(user);
      }
      if (!(profile.id && profile.fullName && profile.lastName && profile.avatar != null && profile.status != null &&
              profile.connected != null))
        profile.load(function(user) {
          render($this, user);
        });
      else
        render($this, profile);
    })
  };

  /**
   * Renders into the specified target a userZoom tooltip for the specified user.
   * The tooltip is bound to the mouse pointer movement:
   * - it is displayed when the mouse hovers above the target,
   * - it is removed when the mouse moves away the tooltip.
   */
  function render(target, user) {
    var status = connectionStatus(user);
    target.append('&nbsp;').append($('<span>').addClass('userzoom-infoConnection').append(status)).hover(function() {
      setTimeout(function() {
        if (currentTarget != target[0] || ($.userZoom.target != null && $.userZoom.target == target))
          return;
        user.loadRelationships({name: $.userZoom.currentUser.lastName, reload: true}, function() {
          $.userZoom.currentUser.onMySentInvitations(function() {
            var element = tooltip(target, user);
            $.userZoom.set(target, element);
          });
        });
      }, 1500);
    });
    $(document).mousedown(function(event) {
      if ($.userZoom.currentTooltip != null) {
        var target = $(event.target);
        if (!target.hasClass('userzoom-tooltip') && target.parents('.userzoom-tooltip').length == 0) {
          $.userZoom.clear();
        }
      }
    });
    $(document).mousemove(function(event) {
      currentTarget = event.target;
    });
  }

  /**
   * Creates into the specified target the tooltip with short information about the specified user
   * and with an interaction tool to communicate with him through Silverpeas.
   */
  function tooltip(target, user) {
    var userinfo = $('<div>').addClass('userzoom-tooltip').
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
            appendTo($(document.body)).
            position({
      of: target,
      at: 'right bottom',
      my: 'left top',
      offset: '-50 7',
      collision: 'flip'
    });
    userinfo.addClass(positionBetween(userinfo, target));
    return userinfo;
  }

})(jQuery);


/**
 * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
 * javascript plugin.
 */
jQuery(document).ready(function() {
  jQuery('.userToZoom').each(function() {
    var $this = jQuery(this);
    if ($this.data('userZoom') == null) {
      $this.userZoom({
        id: $this.attr('rel')
      });
    }
  })
});