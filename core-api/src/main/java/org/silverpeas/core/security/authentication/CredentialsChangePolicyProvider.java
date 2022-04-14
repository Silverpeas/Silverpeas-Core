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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.security.authentication;

import org.silverpeas.core.util.ServiceProvider;

/**
 * A provider of a {@link org.silverpeas.core.security.authentication.CredentialsChangePolicy}
 * instances for each authentication domain.
 * @author mmoquillon
 */
public interface CredentialsChangePolicyProvider {

  /**
   * Gets an instance of this provider.
   * @return an instance of this provider.
   */
  static CredentialsChangePolicyProvider get() {
    return ServiceProvider.getSingleton(CredentialsChangePolicyProvider.class);
  }

  /**
   * Gets the policy as defined for the specified authentication domain.
   * @param domainId the unique identifier of an authentication domain.
   * @return a {@link CredentialsChangePolicy} instance.
   */
  CredentialsChangePolicy getPolicyForDomain(final String domainId);
}
