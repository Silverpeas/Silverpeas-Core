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

package org.silverpeas.core.webapi.admin.scim;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.psu.swe.scim.server.exception.InvalidProviderException;
import edu.psu.swe.scim.server.exception.UnableToRetrieveExtensionsException;
import edu.psu.swe.scim.server.provider.ProviderRegistry;
import edu.psu.swe.scim.spec.resources.ScimGroup;
import edu.psu.swe.scim.spec.resources.ScimUser;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Some initializations that have to be done just after the server has started.
 * @author silveryocha
 */
@Service
public class ScimConfigurator implements Initialization {

  @Inject
  private ProviderRegistry providerRegistry;

  @Inject
  private Instance<ScimUserAdminService> scimUserAdminProvider;

  @Inject
  private Instance<ScimGroupAdminService> scimGroupAdminServices;

  @Override
  public void init() {
    try {
      providerRegistry.registerProvider(ScimUser.class, scimUserAdminProvider);
      providerRegistry.registerProvider(ScimGroup.class, scimGroupAdminServices);
    } catch (InvalidProviderException | JsonProcessingException |
             UnableToRetrieveExtensionsException e) {
      throw new SilverpeasRuntimeException(e.getMessage(), e);
    }
  }
}
