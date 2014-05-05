/*
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

/**
 * Populate all identity card in page.
 */
jQuery(document).ready(function() {
	jQuery('.user-card').each(function(index, value) {
		$.ajax({
			type : "GET",
			url : '/silverpeas/services/profile/users/' + $(value).attr('rel') + '?extended=true',
			dataType : "json",
			cache : false,
			success : function(user, status, jqXHR) {
				$(value).find('.userToZoom').text(user.firstName + ' ' + user.lastName);
				jQuery.each(user, function(key, val) {
					if (key == 'avatar') {
						$(value).find('.' + key).html('');
						$('<img />').attr({'src' : val, 'alt' : 'Avatar'}).appendTo($(value).find('.' + key));
					} else if (key == 'moreData') {
						jQuery.each(val, function(keyMore, valMore) {
							$(value).find('.' + keyMore).text(valMore);
						});
					} else  {
						$(value).find('.' + key).text(val);
					}
				});
			},

			error : function(jqXHR, status) {
				// do nothing
			}
		});
	})
});