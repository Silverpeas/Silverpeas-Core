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

package org.silverpeas.core.admin.component;

import org.silverpeas.core.admin.component.model.PasteDetail;

/**
 * <p>
 * Capability of an application to paste the resources it manages in a given location (another
 * application instance or another space, ...).
 * </p>
 * <p>
 * By default, all application instances in Silverpeas can be copied or moved to another location
 * (another space). Nevertheless, by default the resources that are managed in this application
 * instance aren't also processed by the pasting. In order to paste also the resources
 * managed by the copied or moved application instances, the Silverpeas application has to provide
 * a class that implements this interface and that is qualified by the @Named annotation with
 * as value the name of the application (the first letter in lower case) plus the term
 * ResourcePasting; for example, for the Kmelia application the name should be
 * <code>kmeliaResourcePasting</code>.
 * </p>
 */
public interface ApplicationResourcePasting {

  /**
   * The suffix of the name any bean implementing this interface should have.
   */
  static final String NAME_SUFFIX = "ResourcePasting";

  /**
   * Pastes all the resources of component instance referred by the pasteDetail information to
   * a targeted location also referred by the specified pasteDetail data.
   * @param pasteDetail the pasting information.
   * @throws RuntimeException if an error occurs during the pasting process.
   */
  void paste(PasteDetail pasteDetail) throws RuntimeException;

}
