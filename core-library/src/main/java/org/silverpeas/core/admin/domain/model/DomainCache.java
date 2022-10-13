/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.model;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache with all the Silverpeas domains being used.
 */
@Technical
@Bean
@Singleton
public class DomainCache {

  private ConcurrentMap<String, Domain> map = new ConcurrentHashMap<>();

  protected DomainCache() {}

  /**
   * Clears the cache.
   */
  public synchronized void clearCache() {
    map.clear();
  }

  public Optional<Domain> getDomain(String id) {
    return Optional.ofNullable(map.get(id));
  }

  /**
   * Sets the following domains in the cache after clearing it.
   * @param domains the domains to set in the cache. They replace all the domains present in the
   * cache.
   */
  public void setDomains(List<Domain> domains) {
    clearCache();
    for (Domain domain : domains) {
      addDomain(domain);
    }
  }

  /**
   * Adds the specified domain into the cache if and only if there is no yet a domain with the
   * same unique identifier.
   * @param domain the domain to add.
   * @return either the added domain or the domain with the same identifier in the cache.
   */
  public Domain addDomain(Domain domain) {
    Objects.requireNonNull(domain);
    return map.putIfAbsent(domain.getId(), domain);
  }

  /**
   * Removes from the cache the domain with the specified identifier.
   * @param id the unique identifier of a domain.
   */
  public void removeDomain(String id) {
    map.remove(id);
  }

}
