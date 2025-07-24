/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
package org.silverpeas.core.test.jcr;

import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.initialization.SilverpeasServiceInitialization;
import org.silverpeas.core.jcr.util.SilverpeasJCRIndexation;
import org.silverpeas.core.jcr.util.SilverpeasJCRSchemaRegister;
import org.silverpeas.kernel.util.SystemWrapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.function.Predicate;

/**
 * In order to initialize the JCR that can be used in integration tests, this listener MUST be
 * defined into the web.xml of the archive built of an integration test with arquillian + Wildfly.
 * <p>
 * At server starting, when the context of the integration is initialized, all the tasks required to initialize
 * the JCR for the integration tests are performed. The requirement is the dependencies on the JCR have to be
 * satisfied.
 * </p>
 * <p>
 * By default, this listener is added onto the war archive used in the integration tests by the
 * {@link org.silverpeas.core.test.BasicWarBuilder} object.
 * </p>
 * @author silveryocha
 */
public class SilverpeasJcrInitialization implements ServletContextListener {

  private static final String JCR_HOME = "target/jcr";
  private static final String JCR_CONFIG = "classpath:/silverpeas-oak.properties";

  private static final Predicate<Initialization> FILTER =
      i -> i.getClass().getSimpleName().contains("SilverpeasJCRSchemaRegister");

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    SystemWrapper systemWrapper = SystemWrapper.getInstance();
    systemWrapper.setProperty("jcr.home", JCR_HOME);
    systemWrapper.setProperty("jcr.conf", JCR_CONFIG);

    SilverpeasJCRSchemaRegister register = new SilverpeasJCRSchemaRegister();
    register.register();

    SilverpeasJCRIndexation indexation = SilverpeasJCRIndexation.get();
    indexation.initialize();
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    SilverpeasServiceInitialization.stop(FILTER);
  }
}
