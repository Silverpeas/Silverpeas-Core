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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.viewer;

import jakarta.inject.Inject;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.kernel.SilverpeasException;

/**
 * A {@link ResourceViewProvider} with the capability to register itself in the
 * {@link ResourceViewProviderRegistry} at Silverpeas startup. All the {@link ResourceViewProvider}
 * concrete classes should extend this abstract class so that they will be automatically registered
 * into the {@link ResourceViewProviderRegistry} registry.
 *
 * @author mmoquillon
 */
public abstract class RegisteredResourceViewProvider implements ResourceViewProvider,
    Initialization {

  @Inject
  private ResourceViewProviderRegistry registry;

  @Override
  public void init() throws SilverpeasException {
    registry.addNewEmbedMediaProvider(this);
  }
}
  