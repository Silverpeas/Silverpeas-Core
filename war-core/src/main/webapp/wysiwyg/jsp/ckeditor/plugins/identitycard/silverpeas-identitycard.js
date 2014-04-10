/**
 * Copyright (C) 2000 - 2014 Silverpeas
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

jQuery(document).ready(function() {

  jQuery('.identitycard').each(function(index, value) {
	var idUser = $(this).attr('rel');
	$.ajax({
		 type: "GET",
		 url: "/silverpeas/services/profile/users/" + idUser,
		 dataType: "json",
		 cache: false,
		 success: function (user, status, jqXHR) {
			
			jQuery.each(user, function(userField, userFieldValue) {
				if (userField == 'avatar') {
					var avatar = document.createElement("img");
					$(avatar).attr('src', user.avatar);
					$('.avatar', value).append(avatar);
				} else {
					$('.' + userField, value).html(userFieldValue);	
				}
			});
		 },

		 error: function (jqXHR, status) {
		     // error handler
		     alert('error');
		 }
	});
  })
});