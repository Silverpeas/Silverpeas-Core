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

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.kernel.annotation.Technical;

/**
 * Implementation of both the {@link CredentialsChangePolicy} and
 * {@link CredentialsChangePolicyProvider} interfaces
 * @author mmoquillon
 */
@Technical
@Provider
public class CredentialsChangePolicyImpl implements CredentialsChangePolicy,
    CredentialsChangePolicyProvider {

  private final String domainId;
  private final AuthenticationService authService = AuthenticationServiceProvider.getService();

  /**
   * For IoC.
   */
  @SuppressWarnings("unused")
  private CredentialsChangePolicyImpl() {
    domainId = null;
  }

  private CredentialsChangePolicyImpl(final String domainId) {
    this.domainId = domainId;
  }

  @Override
  public boolean canPasswordBeChanged() {
    return authService.isPasswordChangeAllowed(domainId);
  }

  @Override
  public CredentialsChangePolicy getPolicyForDomain(final String domainId) {
    return new CredentialsChangePolicyImpl(domainId);
  }
}
