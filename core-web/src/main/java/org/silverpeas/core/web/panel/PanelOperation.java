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

package org.silverpeas.core.web.panel;

public class PanelOperation {
  // Not the Id (like GML.xxx) but the language-dependant string (like "Supprimer les fichiers")
  public String m_helpString = "";
  // Not the Id (like GI.xxx) but the language-dependant string (like "theicons/ico1.ico")
  public String m_icon = "";
  // The Command parameter sent to the URL
  public String m_command = "";
  // The confirmation message
  public String m_confirmation = "";

  public PanelOperation(String helpString, String icon, String command) {
    m_helpString = helpString;
    m_icon = icon;
    m_command = command;
  }

  public PanelOperation(String helpString, String icon, String command,
      String confirmation) {
    this(helpString, icon, command);
    m_confirmation = confirmation;
  }
}
