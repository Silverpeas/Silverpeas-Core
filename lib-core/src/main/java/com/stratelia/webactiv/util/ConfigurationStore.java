/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ConfigurationStore.java
 *
 * Created on 17 novembre 2000, 14:08
 */

package com.stratelia.webactiv.util;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author jpouyadou
 * @version
 */
public interface ConfigurationStore {
  /*
   * ConfigurationStore.java Created on 17 novembre 2000, 13:44
   */

  /**
   * @author jpouyadou
   * @version
   */
  public void serialize() throws FileNotFoundException, IOException;

  public void putProperty(String key, String value);

  public void put(String key, String value);

  public String getProperty(String key, String defaultValue);

  public String getProperty(String key);

  public String getString(String key);

  public String get(String key, String defaultValue);

  public String[] getAllNames();
}
