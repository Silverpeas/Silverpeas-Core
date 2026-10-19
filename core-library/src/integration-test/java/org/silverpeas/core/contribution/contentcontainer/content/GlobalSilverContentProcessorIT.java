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
package org.silverpeas.core.contribution.contentcontainer.content;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.util.ServiceProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Integration tests on the discovery of the processors of the contents classified on the PdC: the
 * default one has to be found among them as it is used for any components without their own
 * processor.
 */
@RunWith(Arquillian.class)
public class GlobalSilverContentProcessorIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return LibCoreWarBuilder.onFullWarForTestClass(GlobalSilverContentProcessorIT.class)
        .build();
  }

  @Test
  public void theDefaultProcessorIsAManagedBean() {
    var processors = ServiceProvider.getAllServices(GlobalSilverContentProcessor.class);
    assertThat(processors, hasItem(instanceOf(DefaultGlobalSilverContentProcessor.class)));
  }
}
