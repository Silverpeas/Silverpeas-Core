/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.core.jcr.impl.oak;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.jcr.impl.RepositorySettings;
import org.silverpeas.core.test.UnitTest;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test the Oak repository factory can make a repository by using the Oak framework and with the
 * Silverpeas settings to control the construction of that repository. For the test, only a memory
 * repository creation is asked with the Silverpeas settings.
 * @author mmoquillon
 */
@UnitTest
class OakRepositoryFactoryTest {

  private static final String JCR_HOME = "target/";
  private static final String OAK_CONFIG = "classpath:/silverpeas-oak.properties";

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  @DisplayName("A repository can be made by the Oak repository factory from a configuration file" +
      " and by using Oak")
  void getARepositoryFromTheFactory() throws RepositoryException {
    Map parameters = new HashMap();
    parameters.put(RepositorySettings.JCR_HOME, JCR_HOME);
    parameters.put(RepositorySettings.JCR_CONF, OAK_CONFIG);

    OakRepositoryFactory factory = new OakRepositoryFactory();
    Repository repository = factory.getRepository(parameters);
    assertThat(repository, notNullValue());
    assertThat(repository, is(instanceOf(OakRepository.class)));
  }
}