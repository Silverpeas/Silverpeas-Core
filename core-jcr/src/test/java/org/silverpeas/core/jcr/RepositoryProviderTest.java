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

package org.silverpeas.core.jcr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.jcr.impl.RepositorySettings;
import org.silverpeas.core.jcr.impl.ResourcesCloser;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.SystemProperty;
import org.silverpeas.core.test.unit.extention.TestManagedBeans;
import org.silverpeas.core.test.unit.extention.TestedBean;
import org.silverpeas.core.util.lang.SystemWrapper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.silverpeas.core.jcr.RepositoryProviderTest.JCR_HOME;
import static org.silverpeas.core.jcr.RepositoryProviderTest.OAK_CONFIG;

/**
 * Test a {@link javax.jcr.Repository} instance is well obtained by using a
 * {@link RepositoryProvider} instance and the provided object is an instance of the
 * {@link SilverpeasRepository} class.
 * @author mmoquillon
 */
@EnableSilverTestEnv
@SystemProperty(key = RepositorySettings.JCR_HOME, value = JCR_HOME)
@SystemProperty(key = RepositorySettings.JCR_CONF, value = OAK_CONFIG)
@TestManagedBeans({ResourcesCloser.class})
class RepositoryProviderTest {

  public static final String JCR_HOME = "target/";
  public static final String OAK_CONFIG = "classpath:/silverpeas-oak.properties";

  @SuppressWarnings("unused")
  @TestedBean
  private RepositoryProvider provider;

  @Test
  @DisplayName("The JCR properties should be found from the system")
  void checkSystemPropertiesForJCRAreCorrectlySet() {
    SystemWrapper systemWrapper = SystemWrapper.get();
    assertThat(systemWrapper.getProperty(RepositorySettings.JCR_HOME), is(JCR_HOME));
    assertThat(systemWrapper.getProperty(RepositorySettings.JCR_CONF), is(OAK_CONFIG));
  }

  @Test
  @DisplayName("The repository provider should provide an instance of SilverpeasRepository")
  void provideARepository() {
    SilverpeasRepository repository = provider.getRepository();
    assertThat(repository, notNullValue());
  }
}