/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.ldap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure an OpenDJ server to be embedded in a test.
 * 
 * @author ehugonnet
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CreateLdapServer {

  /**
   * The configurtion ldif file.
   * @return configurtion ldif file.
   */
  String ldifConfig() default "config.ldif";

  /**
   * The directory containing the OpenDJ configuration and schemas.
   * @return directory containing the OpenDJ configuration and schemas.
   */
  String serverHome() default "opendj";

  /**
   * The ldif file to be loaded into the server.
   * @return ldif file to be loaded into the server.
   */
  String ldifFile();
  
  
  /**
   * The backendID of the backend where the ldif will be loaded.
   * @return backendId of the backend where the ldif will be loaded.
   */
  String backendID() default "silverpeas";
  
  /**
   * The backendID of the backend where the ldif will be loaded.
   * @return backendId of the backend where the ldif will be loaded.
   */
  String baseDN() default "dc=silverpeas,dc=org";
}
