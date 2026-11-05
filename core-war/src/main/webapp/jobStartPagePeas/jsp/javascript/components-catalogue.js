/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* Native administration catalogue: keep categories and descriptions, disable full applications. */
document.addEventListener('DOMContentLoaded', () => {
  jQuery('.linkSee').on('click', function(event) {
    event.preventDefault();
    jQuery('.applicationsType ul').hide();
    jQuery('.linkSee').removeClass('select');
    jQuery(this).addClass('select').parents('.applicationsType').children('ul').show();
  });
  jQuery('.application a').hover(function() {
    if (jQuery(this).children('.application-capacity').length) { return; }
    jQuery(this).children('.applicationDescription').show();
    jQuery(this).children('h3').hide();
  }, function() {
    if (jQuery(this).children('.application-capacity').length) { return; }
    jQuery(this).children('h3').show();
    jQuery(this).children('.applicationDescription').hide();
  });
  jQuery('.linkSee span').each(function() {
    const title = jQuery(this).text().trim();
    jQuery(this).text(title.substring(3));
    jQuery(this).parents('.applicationsType').addClass('type_' + title.substring(0, 2));
  });
  jQuery('.type_01 ul').show();
  jQuery('.type_01 .linkSee').addClass('select');
  document.addEventListener('click', event => {
    if (event.target.closest('.application a[aria-disabled="true"]')) { event.preventDefault(); }
  });
});
