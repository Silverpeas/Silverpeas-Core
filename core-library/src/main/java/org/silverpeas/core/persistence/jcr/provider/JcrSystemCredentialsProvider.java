/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.jcr.provider;

import javax.jcr.SimpleCredentials;

/**
 * A provider of the credentials for the JCR system user in Silverpeas. These credentials are used
 * by the implementation of the Silverpeas JCR API to open a connection with the underlying JCR
 * repository. A user wishing connecting himself the repository has to provide himself a simple
 * credentials with the login and the associated password with which he uses to sign in Silverpeas.
 * @author mmoquillon
 */
public class JcrSystemCredentialsProvider {

  private static final String ID = "jcr-system@domain0";

  /**
   * Gets the credentials of the JCR system user in Silverpeas.
   * @return the simple credentials corresponding to the JCR system user.
   */
  public static final SimpleCredentials getJcrSystemCredentials() {
    return new SimpleCredentials(ID, new char[0]);
  }
}
