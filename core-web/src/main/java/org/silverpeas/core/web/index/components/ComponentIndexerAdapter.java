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
package org.silverpeas.core.web.index.components;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;

import javax.inject.Singleton;

import static org.silverpeas.kernel.logging.SilverLogger.getLogger;

/**
 * @author ehugonnet
 */
@Singleton
public class ComponentIndexerAdapter implements ComponentIndexation {

  @Override
  public void index(SilverpeasComponentInstance componentInst) {
    getLogger(this).debug("No indexer for component instance {0}", componentInst.getId());
  }

  @Override
  public void index(final SilverpeasComponentInstance componentInst, final boolean deleteAllBefore) {
    index(componentInst);
  }
}
