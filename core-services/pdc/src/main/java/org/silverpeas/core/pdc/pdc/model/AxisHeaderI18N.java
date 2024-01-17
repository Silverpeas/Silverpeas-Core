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
package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.i18n.BeanTranslation;

/**
 * This class contains headers of axis. And uses the persistence class for the DAO. The user can
 * access to the axis main information.
 */
public class AxisHeaderI18N extends BeanTranslation implements java.io.Serializable {

  // Class version identifier
  private static final long serialVersionUID = -1418233065462620219L;

  public AxisHeaderI18N() {
  }

  public AxisHeaderI18N(final AxisHeaderI18N translation) {
    super(translation);
  }

  public AxisHeaderI18N(String axisId, String lang, String name, String description) {
    super(lang, name, description);
    setObjectId(axisId);
  }
}