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

/*
 * Copy or cut a space or an application into the clipboard from its administration page.
 *
 * The clipboard is refreshed by the hidden frame IdleFrame of the Silverpeas main window. The
 * administration page can be in a frame of this window or in a popup opened from it (as from the
 * result of a search in the administration); in the latter, the hidden frame is the one of the
 * window that opened the popup.
 */

function clipboardCopy(type, id) {
  _spWindow_getSilverpeasMainWindow().IdleFrame.location.href = 'copy?Type=' + type + '&Id=' + id;
}

function clipboardCut(type, id) {
  _spWindow_getSilverpeasMainWindow().IdleFrame.location.href = 'Cut?Type=' + type + '&Id=' + id;
}
