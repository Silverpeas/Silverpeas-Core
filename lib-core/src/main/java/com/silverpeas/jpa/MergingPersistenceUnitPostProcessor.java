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

package com.silverpeas.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

/**
 * Processor of persistence units dedicated to merge all JPA settings defining the same persistence
 * unit.
 */
public class MergingPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {
  private Map<String, List<String>> puiClasses = new HashMap<String, List<String>>();

  @Override
  public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
    List<String> classes = puiClasses.get(pui.getPersistenceUnitName());
    if (classes == null) {
      classes = new ArrayList<String>();
      puiClasses.put(pui.getPersistenceUnitName(), classes);
    }
    pui.getManagedClassNames().addAll(classes);
    classes.addAll(pui.getManagedClassNames());
  }
}
