/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.pdc.tree.model;

import org.silverpeas.core.i18n.BeanTranslation;
import org.silverpeas.core.pdc.pdc.model.AxisHeaderI18N;

public class TreeNodeI18N extends BeanTranslation implements java.io.Serializable {

  private static final long serialVersionUID = 7977604222849839444L;

  public TreeNodeI18N() {
  }

  public TreeNodeI18N(String nodeId, String lang, String name, String description) {
    super(lang, name, description);
    setObjectId(nodeId);
  }

  public TreeNodeI18N(final TreeNodeI18N translation) {
    super(translation);
  }

  /**
   * Constructs a {@link TreeNodeI18N} representation of the specified {@link AxisHeaderI18N}
   * instance. They are two different presentations of the same bean translation and as such share
   * the same identifier.
   * @param otherTranslation an {@link AxisHeaderI18N} instance.
   */
  public TreeNodeI18N(final AxisHeaderI18N otherTranslation) {
    super(otherTranslation);
  }
}
