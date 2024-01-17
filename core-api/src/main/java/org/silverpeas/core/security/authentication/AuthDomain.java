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

/**
 * An authentication domain of users and groups of users in Silverpeas. Such a domain is actually a
 * directory of users and of groups of users providing its ow authentication server. In Silverpeas,
 * the users and the groups of users can be defined in different directories (LDAP service is a good
 * example of such directories) and each of them has their own authentication server to which the
 * authentication can be delegated by Silverpeas. By reifying this concept, Silverpeas is able to
 * mix in several users sources, both internal and external, to add a new one with less impact, and
 * to take in charge transparently the authentication by delegating it by using a protocol specific
 * to the targeted authentication server.
 * @author mmoquillon
 */
public interface AuthDomain {

  String SILVERPEAS_DOMAIN_ID = "0";

  /**
   * Gets the unique identifier of the domain in Silverpeas. By convention, a default domain is
   * defined for the users specifically created in Silverpeas itself: this domain has for identifier
   * 0.
   * @return the unique identifier of the domain in Silverpeas.
   */
  String getId();

  /**
   * Gets the name of this authentication domain.
   * @return its name.
   */
  String getName();

  /**
   * Gets the policy of this domain about the possibility of a user to change one of its
   * credentials.
   * @return a {@link CredentialsChangePolicy} instance.
   */
  default CredentialsChangePolicy getCredentialsChangePolicy() {
    return CredentialsChangePolicyProvider.get().getPolicyForDomain(getId());
  }

}
