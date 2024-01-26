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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.impl;

import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.util.SystemWrapper;

/**
 * The parameters the different {@link javax.jcr.RepositoryFactory} have to understand and satisfy
 * when creating a {@link javax.jcr.Repository} object through which Silverpeas access the JCR.
 * @author mmoquillon
 */
public class RepositorySettings {

  /**
   * The absolute path of the home directory of the JCR. The directory into which the data could be
   * persisted and some JCR implementation-specific configuration files are located.
   */
  public static final String JCR_HOME = "jcr.home";

  /**
   * The absolute path of the JCR configuration file required to create a
   * {@link javax.jcr.Repository} instance. This file is either specific to the underlying JCR
   * implementation or it is specific to Silverpeas to customize the repository creation.
   */
  public static final String JCR_CONF = "jcr.conf";

  /**
   * Gets the absolute path of the JCR home directory. The JCR home directory is the location in
   * which is defined the repository to use in Silverpeas. It should be defined in the system
   * properties to be retrieved. If no such property is found, then a
   * {@link SilverpeasRuntimeException} exception is thrown.
   * @return the absolute path of the JCR home directory.
   */
  public String getJCRHomeDirectory() {
    String jcrHomePath = SystemWrapper.getInstance().getProperty(JCR_HOME);
    if (StringUtil.isNotDefined(jcrHomePath)) {
      throw new SilverpeasRuntimeException("No such system properties defined: " + JCR_HOME);
    }
    return jcrHomePath;
  }

  /**
   * Gets the absolute path of the JCR configuration file required to bootstrap or to open access to
   * the repository. The configuration file can be either specific to the underlying implementation
   * or specific to Silverpeas whether this one performs itself the creation of the
   * {@link javax.jcr.Repository} instance. If no such property is found, then a
   * {@link SilverpeasRuntimeException} exception is thrown.
   * @return the absolute path of the JCR configuration file to use to create a
   * {@link javax.jcr.Repository} instance.
   */
  public String getJCRConfigurationFile() {
    String jcrConf = SystemWrapper.getInstance().getProperty(JCR_CONF);
    if (StringUtil.isNotDefined(jcrConf)) {
      throw new SilverpeasRuntimeException("No such system properties defined: " + JCR_CONF);
    }
    return jcrConf;
  }
}
