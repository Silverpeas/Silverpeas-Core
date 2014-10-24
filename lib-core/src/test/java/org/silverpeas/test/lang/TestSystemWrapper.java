/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.test.lang;

import org.silverpeas.util.lang.DefaultSystemWrapper;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * Default implementation that is nothing more than a delegate of {@link System} class.
 * @author Yohann Chastagnier
 */
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class TestSystemWrapper extends DefaultSystemWrapper{

  private Map<String, String> env = null;

  @Override
  public String getenv(final String name) {
    return getenv().get(name);
  }

  @Override
  public Map<String, String> getenv() {
    if (env == null) {
      env = new HashMap<>();
      env.putAll(System.getenv());
      env.put("SILVERPEAS_HOME", "");
    }
    return env;
  }
}
