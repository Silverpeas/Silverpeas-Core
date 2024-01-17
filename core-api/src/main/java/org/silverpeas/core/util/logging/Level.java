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
package org.silverpeas.core.util.logging;

/**
 * The different logging levels taken in charge by the Silverpeas logger.
 * <p>
 * This Level enumeration defines a set of standard logging levels that can be used to control
 * logging output. The logging levels are ordered. Enabling logging at a given level also enables
 * logging at all higher levels.
 * @author miguel
 */
public enum Level {
  DEBUG(500),
  INFO(700),
  WARNING(800),
  ERROR(900);

  private final int value;

  Level(int order) {
    this.value = order;
  }

  public int value() {
    return this.value;
  }
}
