/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.silverpeas.core.web.util.viewgenerator.html.TagUtil;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author neysseri
 */
public class ButtonSilverpeasV5 extends AbstractButton {

  public ButtonSilverpeasV5() {
    // Nothing to do.
  }

  @Override
  public String renderButtonHtml() {
    final String classes = "sp_button" + defaultStringIfNotDefined(" " + getClasses());
    final String theAction = disabled ? "#" : getAction();
    final String href = TagUtil.formatHrefFromAction(theAction);
    return "<a class=\"" + classes + "\" " + href + " >" + label + "</a>";
  }
}
