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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain;

import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * Register of all domain types available into Silverpeas.
 * <p>
 *   By default, {@link DomainType#LDAP} and {@link DomainType#SQL} are already registered.
 * </p>
 */
@Singleton
public class DomainTypeRegistry {

  private final Set<DomainType> registry = new HashSet<>();

  public static DomainTypeRegistry get() {
    return ServiceProvider.getService(DomainTypeRegistry.class);
  }

  @PostConstruct
  protected void setupDefaults() {
    registry.add(DomainType.LDAP);
    registry.add(DomainType.SQL);
  }

  /**
   * Hidden constructor.
   */
  private DomainTypeRegistry() {
  }

  /**
   * Adds a domain type into registry.
   * @param type the domain type.
   */
  public void add(final DomainType type) {
    registry.add(type);
  }

  /**
   * Indicates if a domain type is registered.
   * @param type a domain type.
   * @return true if registered, false otherwise.
   */
  public boolean exists(final DomainType type) {
    return registry.contains(type);
  }
}
