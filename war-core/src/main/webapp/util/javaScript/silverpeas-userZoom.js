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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function( $ ){
  
  $.userZoom = {
    currentTooltip: null,
    initialized: false
  };
  
  function openWindow(url, windowName, width, height, options) {
    var top = (screen.height - height) / 2;
    var left = (screen.width - width) / 2;
    window.open(url, windowName, "top=" + top + ",left=" + left + ",width=" + width + ",height=" + height + "," + options);
  }
  
  function tchatWith(user) {
    openWindow(user.tchatPage, 'popupDiscussion' + user.id, '650', '460', 'menubar=no,scrollbars=no,statusbar=no');
  }
  
  function connectionStatus(user) {
    if (user.connected) {
      var onlineStatus = webContext + '/util/icons/online.gif';
      var onlineStatusAlt = $.i18n.prop('connected');
    } else {
      var onlineStatus = webContext + '/util/icons/offline.gif';
      var onlineStatusAlt = $.i18n.prop('notConnected');
    }
    return $('<img>', {src: onlineStatus, alt: onlineStatusAlt});
  }
  
  /**
   * The user presential Silverpeas plugin based on JQuery.
   * This JQuery plugin renders a tooltip with status information about the user and from which 
   * anyone can establish a communication with him. The tooltip is displayed when the mouse hovers
   * above the HTML element.
   */
  $.fn.userZoom = function( user ) {
    
    if (! this.length)
      return this;
    
    if (! $.userZoom.initialized) {
      $.i18n.properties({
        name: 'socialNetworkBundle',
        path: webContext + '/services/bundles/com/silverpeas/socialNetwork/multilang/',
        language: '$$', /* by default the language of the user in the current session */
        mode: 'map'
      });
      $.userZoom.initialized = true;
    }
    
    return this.each(function() {
      var profile = user, $this = $(this);
      $this.data('userZoom', new Date());
      if (!(profile instanceof UserProfile)) {
        profile = new UserProfile(user);
      }
      if (!(profile.id && profile.fullName && profile.avatar != null && profile.status != null &&
        profile.connected != null))
        profile.load(function(user) {
          render($this, user);
        });
      else
        render($this, profile);
    })
  };
  
  /**
   * Renders into the specified target a tooltip with information about the specified user.
   * The tooltip is bound with the mouse events on the target.
   */
  function render( target, user ) {
    var status = connectionStatus(user);
    target.append('&nbsp;').append(status.css('width', '8px')).hover(function() {
      $('.userzoom-tooltip').hide();
      var element = tooltip(target, user);
      element.show();
      $.userZoom.currentTooltip = element;
    });
    $(document).mousedown(function(event) {
      if (!$.userZoom.currentTooltip)
        return;
      var target = $(event.target);
      if (!target.hasClass('userzoom-tooltip') && target.parents('.userzoom-tooltip').length == 0) {
        $('.userzoom-tooltip').hide();
        $.userZoom.currentTooltip = null;
      }
    });
  }
  
  /**
   * Creates into the specified target the tooltip with short information about the specified user
   * and with some buttons to communicate with him through Silverpeas.
   * The tooltip is hidden.
   */
  function tooltip( target, user ) {
    var userinfo = target.parent().children('.userzoom-tooltip');
    if (userinfo.length == 0) {
      userinfo = $('<div>').addClass('userzoom-tooltip ui-widget ui-widget-content ui-helper-clearfix ui-corner-all').
      css({
        'position': 'absolute', 
        'display': 'block', 
        'z-index': '1000'
      }).
      append($('<img>', {
        src: user.avatar, 
        alt: user.fullName
      }).addClass('avatar')).
      append($('<p>').addClass('name').append(user.fullName)).
      append($('<p>').addClass('message').append(user.status)).
      append($('<p>').addClass('connection').append(connectionStatus(user))).
      append($('<a>', {href: '#'}).addClass('link invitation').append($.i18n.prop('invitation.send')).invitMe(user)).
      append($('<button>').append($.i18n.prop('myProfile.tab.profile')).click(function() {
        document.location.href = user.webPage;
      })).
      append($('<button>').append($.i18n.prop('tchat')).click(function() {
        tchatWith(user);
      })).
      appendTo(target).data('user', user.id);
    }
    return userinfo;
  }
  
})( jQuery );

$(function() {
  $('.userToZoom').each(function(i, element) {
    if ($(element).data('userZoom') == null)
      $(element).userZoom({
        id: $(element).attr('rel')
      });
  });
});
