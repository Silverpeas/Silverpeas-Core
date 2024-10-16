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
package org.silverpeas.core.process.annotation;

import org.silverpeas.core.util.annotation.ClassAnnotationUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Registering <code>DummyHandledFileConverter</code> instances. Registred
 * <code>DummyHandledFileConverter</code> are used by <code>SimulationActionProcess</code>.
 * @author Yohann Chastagnier
 * @see SimulationActionProcess
 */
public class DummyHandledFileConverterRegistration {

  /**
   * Converters
   */
  private static final Map<Class<? extends SimulationElement>, DummyHandledFileConverter>
      converters = new HashMap<>();

  /**
   * Gets the class of the element that has to be converted to a dummy handled file.
   * @param converter a converter.
   * @return the type of the source element handled by the converter.
   */
  private static Class<? extends SimulationElement> getSourceElementType(
      DummyHandledFileConverter converter) {
    return ClassAnnotationUtil
        .searchParameterizedTypeFrom(SimulationElement.class, converter.getClass());
  }

  /**
   * Register
   * @param converter a converter instance.
   */
  public static synchronized void register(final DummyHandledFileConverter converter) {
    converters.put(getSourceElementType(converter), converter);
  }

  /**
   * Unregister
   * @param converter a converter instance.
   */
  public static synchronized void unregister(final DummyHandledFileConverter converter) {
    converters.remove(getSourceElementType(converter));
  }

  /**
   * @return a converter for a type
   */
  @SuppressWarnings("unchecked")
  static <E extends SimulationElement<?>> DummyHandledFileConverter<E> getConverter(
      Class<E> sourceElementType) {
    return converters.get(sourceElementType);
  }
}
