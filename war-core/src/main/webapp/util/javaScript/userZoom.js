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
  
  var currentTooltip = null;
  
  /**
   * The user presential JQuery plugin.
   * This JQuery plugin renders a tooltip with status information about the user and from which 
   * anyone can establish a communication with him. The tooltip is displayed when the mouse hovers
   * above the HTML element.
   */
  $.fn.userZoom = function( user ) {
    
    if (! this.length)
      return this;
    
    return this.each(function() {
      var profile = user, $this = $(this);
      if (! profile instanceof UserProfile) {
        profile = new UserProfile(user)
      }
      if (profile.id && profile.fullName && profile.avatar != null && profile.status != null &&
          profile.connected != null)
        render($this, profile);
      else
        profile.load(function(userProfile) {
          render($this, userProfile);
        });
    })
  };
  
  
  
  /**
   * Renders into the specified target a tooltip with information about the specified user.
   * The tooltip is bound with the mouse events on the target.
   */
  function render( target, user ) {
    var element = tooltip(target, user);
    target.hover(function() {
      //$('.userzoom-tooltip').dialog('close');
      $('.userzoom-tooltip').hide();
      //element.dialog({stack: false});
      element.show();
      currentTooltip = element;
    });
    $(document).mousedown(function(event) {
      if (!currentTooltip)
        return;
      var target = $(event.target);
      if (!target.hasClass('userzoom-tooltip') && target.parents('.userzoom-tooltip').length == 0) {
        //$('.userzoom-tooltip').dialog('close');
        $('.userzoom-tooltip').hide();
        currentTooltip = null;
      }
    });
  }
  
  /**
   * Creates into the specified target the tooltip with short information about the specified user
   * and with some buttons to communicate with him through Silverpeas.
   * The tooltip is hidden.
   */
  function tooltip( target, user ) {
    return $('<div>').addClass('userzoom-tooltip').css('z-index', '1000').hide().
      append($('<div>').addClass('avatar').append($('<img>', {src: user.avatar, alt: user.fullName}))).
      append($('<p>').addClass('name').append(user.fullName)).
      append($('<p>').addClass('message').append(user.status)).
      append($('<p>').addClass('connection').append("connected: " + user.connected)).
      append($('<p>').append("")).
      appendTo($('<div>').addClass('info').appendTo(target)).data('user', user.id);
  }
  
})( jQuery );

//$(function() {
//	$(".userToZoom").userZoom({
//		id: this.attr('rel')
//	});
//});
