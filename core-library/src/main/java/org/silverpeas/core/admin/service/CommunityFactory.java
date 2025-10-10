/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.kernel.annotation.NonNull;

/**
 * Factory of a community of users. This interface is used by Silverpeas to bootstrap a community
 * when an administrator creates a community space. The community space is just an usual space and
 * in order to be a community one, a community has to be created and then linked to the space. This
 * factory aims to create such community for a given space in Silverpeas.
 * <p>
 * Silverpeas supports two kinds of spaces: collaborative spaces and community spaces. Latter are
 * only available if an application dedicated to manage the memberships of a community is available.
 * Otherwise, the communities support is disabled and only collaborative spaces are available. The
 * constrain for such application is to provide an implementation of this interface, and that has to
 * be available in the underlying IoC subsystem.
 * </p>
 *
 * @author mmoquillon
 */
public interface CommunityFactory {

  /**
   * Creates a community for the specified Silverpeas space. This will spawn the membership
   * management application for the the specified Silverpeas space.
   *
   * @param communitySpace a Silverpeas space.
   * @return the component instance representing the application dedicated to manage the
   * created community membership.
   * @throws org.silverpeas.kernel.SilverpeasRuntimeException if the community cannot be created.
   */
  @NonNull
  ComponentInst createCommunity(@NonNull SpaceInst communitySpace);

}
