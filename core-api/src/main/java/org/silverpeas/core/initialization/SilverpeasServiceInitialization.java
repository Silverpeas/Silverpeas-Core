/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.initialization;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.enterprise.inject.Any;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This class provides the method to initialize all the services that implement the
 * {@code org.silverpeas.core.initialization.Initialization} interface when the application is starting.
 * @author mmoquillon
 */
public class SilverpeasServiceInitialization {

  private SilverpeasServiceInitialization() {
  }

  @SafeVarargs
  public static void start(final Predicate<Initialization>... filters) {
    SilverLogger logger = SilverLogger.getLogger("silverpeas");
    logger.info("Silverpeas Services Initialization...");
    getAllInitializations(filters).forEach(i -> {
      String simpleClassName = i.getClass().getSimpleName();
      try {
        logger.info(" -> {0} initialization...", simpleClassName);
        i.init();
        logger.info("    {0} initialization done.", simpleClassName);
      } catch (Exception e) {
        logger.error("    {0} initialization failure!", i.getClass().getName());
        throw new SilverpeasRuntimeException(e.getMessage(), e);
      }
    });
  }

  @SafeVarargs
  public static void stop(final Predicate<Initialization>... filters) {
    SilverLogger logger = SilverLogger.getLogger("silverpeas");
    logger.info("Silverpeas Services Release...");
    getAllInitializations(filters).forEach(i -> {
      try {
        i.release();
      } catch (Exception ex) {
        logger.warn(ex.getMessage());
      }
    });
  }

  @SafeVarargs
  private static Stream<Initialization> getAllInitializations(final Predicate<Initialization>... filters) {
    return ServiceProvider.getAllServices(Initialization.class, Any.Literal.INSTANCE)
        .stream()
        .filter(Stream.of(filters).reduce(Predicate::and).orElse(x -> true))
        .sorted(Comparator.comparing(Initialization::getPriority));
  }
}
