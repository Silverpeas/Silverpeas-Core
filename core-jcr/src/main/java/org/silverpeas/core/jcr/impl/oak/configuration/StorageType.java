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

package org.silverpeas.core.jcr.impl.oak.configuration;

import java.util.Arrays;

/**
 * The type of storage backend to store data for the JCR in Oak.
 * @author mmoquillon
 */
public enum StorageType {
  /**
   * The data are stored in memory and then will be lost once the application is down. This is
   * useful for tests, and it shouldn't be used in production.
   */
  MEMORY_NODE_STORE("memory"),
  /**
   * The data are stored into the filesystem for maximal performance. This is sufficient for
   * standalone system.
   */
  SEGMENT_NODE_STORE("segment"),
  /**
   * The data are stored into a document-based, distributed, database for maximal scalability. This
   * storage is mandatory for distributed systems. Currently, Oak supports three subtypes of such
   * database:
   * <ul>
   *   <li>a true document-based database: MongoDB,</li>
   *   <li>any relational databases,</li>
   *   <li>in memory for testing purpose (only for testing purpose. This isn't actually supported
   *   by this Silverpeas Oak wrapper.</li>
   * </ul>
   */
  DOCUMENT_NODE_STORE("document"),
  /**
   * The data is stored into several node storages. Those storages can be both a document and a
   * segment one. For instance, this is not supported by Silverpeas.
   */
  COMPOSITE_NODE_STORE("composite");

  private final String value;

  /**
   * Gets from the specified configuration value of the storage type the {@link StorageType}
   * instance matching it.
   * @param confValue the value of the storage type configuration parameter.
   * @return the {@link StorageType} instance matching the given value or null if no such storage
   * type exists.
   */
  public static StorageType fromValue(final String confValue) {
    return Arrays.stream(values())
        .filter(s -> s.getValue().equals(confValue))
        .findFirst().orElse(null);
  }

  StorageType(final String confValue) {
    this.value = confValue;
  }

  /**
   * Gets the value of the storage type configuration parameter as it should be defined in the
   * configuration file of the Oak repository.
   * @return the configuration value of this storage type.
   */
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
