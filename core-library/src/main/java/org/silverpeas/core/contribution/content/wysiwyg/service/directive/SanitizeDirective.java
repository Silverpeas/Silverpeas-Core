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
package org.silverpeas.core.contribution.content.wysiwyg.service.directive;

import org.owasp.html.PolicyFactory;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygContentTransformerDirective;
import org.silverpeas.core.util.StringUtil;

import static org.owasp.html.Sanitizers.*;

/**
 * Sanitize the WYSIWYG content in order to keep only:
 * <ul>
 *   <li>safe formatting</li>
 *   <li>safe blocks</li>
 *   <li>safe images</li>
 *   <li>safe links</li>
 *   <li>safe tables</li>
 *   <li>safe styles</li>
 * </ul>
 * @author silveryocha
 */
public class SanitizeDirective implements WysiwygContentTransformerDirective {

  private static final PolicyFactory POLICY_FACTORY =
      FORMATTING.and(BLOCKS).and(LINKS).and(STYLES).and(TABLES).and(IMAGES);

  @Override
  public String execute(final String wysiwygContent) {
    if (wysiwygContent == null) {
      return StringUtil.EMPTY;
    }
    return POLICY_FACTORY.sanitize(wysiwygContent);
  }
}