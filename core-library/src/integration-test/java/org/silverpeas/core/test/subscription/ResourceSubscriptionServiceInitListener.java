/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.subscription;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.silverpeas.core.subscription.service.AbstractResourceSubscriptionService;
import org.silverpeas.kernel.SilverpeasException;
import org.silverpeas.kernel.logging.SilverLogger;

/**
 * In order to register all the available subscription services, this listener has to be added as a
 * web listener into the archive built for integration tests with arquillian + Wildfly.
 *
 * @author mmoquillon
 */
public class ResourceSubscriptionServiceInitListener implements ServletContextListener {

  @Inject
  private Instance<AbstractResourceSubscriptionService> subscriptionServices;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    subscriptionServices.forEach(AbstractResourceSubscriptionService::init);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    subscriptionServices.forEach(s -> {
      try {
        s.release();
      } catch (SilverpeasException e) {
        SilverLogger.getLogger(this).error(e);
      }
    });
  }
}
  