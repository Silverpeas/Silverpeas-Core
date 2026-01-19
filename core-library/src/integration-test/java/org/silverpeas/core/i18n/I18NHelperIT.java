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

package org.silverpeas.core.i18n;

import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.ui.DisplayI18NHelper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration test about the dependency resolution of the I18NHelper as the default implementation
 * of the I18n interface.
 *
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class I18NHelperIT {

  @Inject
  private I18n i18n;

  @Deployment
  public static Archive<?> createTestArchive() {
    return LibCoreWarBuilder.onWarForTestClass(I18NHelperIT.class)
        .addI18n()
        .build();
  }

  @Test
  public void checkInjection() {
    assertThat(i18n, is((notNullValue())));
  }

  @Test
  public void displayI18NHelperCanBeConstructed() {
    assertThat(DisplayI18NHelper.getDefaultLanguage(), is("fr"));
    assertThat(DisplayI18NHelper.getLanguages(), contains("fr", "en", "de"));
  }
}
  