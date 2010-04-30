/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.searchEngine.model;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class TestSearchCompletion extends AbstractTestDao {

  @Before
  @Override
  public void setUp() throws Exception {
    Properties props = new Properties();
    props.load(TestSearchCompletion.class.getClassLoader().getResourceAsStream(
        "jndi.properties"));
    String jndiPath = props.getProperty("java.naming.provider.url").substring(7);
    File file = new File(jndiPath);
    file.mkdir();
    super.setUp();
  }


  @Test
  public final void testGetSuggestions() {
    SearchCompletion completion = new SearchCompletion();
    Set<String> set = completion.getSuggestions("inte");

    assertEquals(set.size(), 3);

    int i = 0;
    for (String keyword : set) {
      if (i == 0) {
        assertEquals("interface", keyword);
      }
      if (i == 1) {
        assertEquals("internet", keyword);
      }
      if (i == 2) {
        assertEquals("interpolation", keyword);
      }
      i++;
    }

  }

  @Override
  protected String getDatasetFileName() {
    return "autocompletion-dataset.xml";
  }

}
