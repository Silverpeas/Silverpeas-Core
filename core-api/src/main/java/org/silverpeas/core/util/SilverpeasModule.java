/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.silverpeas.core.annotation.Module;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Silverpeas is made up of several modules, each of them defining either a service or an
 * application. A module is uniquely identified by a simple name. Each module in Silverpeas
 * provides a set of interfaces that can be used by others modules to realize their
 * responsibilities; codes in modules shouldn't use the classes in others modules but the
 * interfaces.
 * @author miguel
 */
public class SilverpeasModule {

  private static final Pattern pattern = Pattern.compile(
      "(org\\.silverpeas|com\\.silverpeas|com\\.stratelia\\.webactiv|com\\.stratelia" +
          "\\.silverpeas)\\.(\\w+).*");
  /* TODO replace the pattern above by the one below once the package renaming done and don't forget to remove all the Peas-like logger modules
  "(org\\.silverpeas\\.core\\.web|org\\.silverpeas\\.web|org\\.silverpeas\\.core\\.components|" +
        "org\\.silverpeas\\.core|org\\.silverpeas)\\.(\\w+).*")
   */

  /**
   * Gets the name of the module to which the specified object belongs. The module name is first
   * seek from the @{code Module} annotation at the package of the object. If no such annotation
   * is found, then it is computed from the package name itself.
   * @param anObject an object.
   * @return the module name to which this object belongs to.
   */
  public static String getModuleName(Object anObject) {
    String module;
    Package p = (anObject instanceof Class ? ((Class) anObject).getPackage() :
        anObject.getClass().getPackage());
    Module m = p.getAnnotation(Module.class);
    if (m == null) {
      Matcher matcher = pattern.matcher(p.getName());
      if (matcher.matches()) {
        module = matcher.group(2);
      } else {
        module = p.getName();
      }
    } else {
      module = m.value();
    }
    return module;
  }
}
