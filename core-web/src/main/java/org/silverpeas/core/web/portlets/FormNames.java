/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.web.portlets;

public interface FormNames {

  /*
   * Markup element name for the "url" value in the portlet's edit mode.
   */
  public static final String TEXTBOX_NB_ITEMS = "textboxNbItems";

  /*
   * Markup element name for the "maxAge" value in the portlet's edit mode.
   */
  public static final String TEXTBOX_MAX_AGE = "maxAge";

  /*
   * Markup element name for the "finished" button in the portlet's edit mode.
   */
  public static final String SUBMIT_FINISHED = "submitFinished";

  /*
   * Markup element name for the "cancel" button in the portlet's edit mode.
   */
  public static final String SUBMIT_CANCEL = "submitCancel";

  /*
   * Error message name when the "TEXTBOX_URL" value is null.
   */
  public static final String ERROR_BAD_VALUE = "errorBadValue";
}
