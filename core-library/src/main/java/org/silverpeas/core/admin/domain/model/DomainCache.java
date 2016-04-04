/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.domain.model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.silverpeas.core.admin.domain.model.Domain;

public class DomainCache {

  private static ConcurrentMap<String, Domain> map =
      new ConcurrentHashMap<String, Domain>();

  public synchronized static void clearCache() {
    map.clear();
  }

  public static Domain getDomain(String id) {
    return map.get(id);
  }

  public static void setDomains(List<Domain> domains) {
    clearCache();
    for (Domain domain : domains) {
      addDomain(domain);
    }
  }

  public static void addDomain(Domain domain) {
    map.putIfAbsent(domain.getId(), domain);
  }

  public static void removeDomain(String id) {
    map.remove(id);
  }

}
