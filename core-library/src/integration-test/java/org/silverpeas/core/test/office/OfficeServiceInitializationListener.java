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

package org.silverpeas.core.test.office;

import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.initialization.SilverpeasServiceInitialization;
import org.silverpeas.core.test.WarBuilder4LibCore;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.function.Predicate;

/**
 * In order to initialize the services around the office document, this listener MUST be defined
 * into the web.xml of the archive built of an integration test with arquillian + Wildfly.
 * <p>
 *   At server starting, when the context of the integration is initialized, each implementation
 *   of {@link Initialization} interface that the class name contains the keyword "OfficeService"
 *   is performed.
 * </p>
 * <p>
 *   To use it easily, {@link WarBuilder4LibCore#addOfficeFeatures()} method is provided.
 * </p>
 * @author silveryocha
 */
public class OfficeServiceInitializationListener implements ServletContextListener {

  private static final Predicate<Initialization> FILTER = i -> i.getClass().getSimpleName().contains("OfficeService");

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    SilverpeasServiceInitialization.start(FILTER);
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    SilverpeasServiceInitialization.stop(FILTER);
  }
}
