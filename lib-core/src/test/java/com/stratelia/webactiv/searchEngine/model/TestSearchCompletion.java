package com.stratelia.webactiv.searchEngine.model;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSearchCompletion extends AbstractTestDao {

  @Before
  public void setUp() throws Exception {
    Properties props = new Properties();
    props.load(TestSearchCompletion.class.getClassLoader().getResourceAsStream(
        "jndi.properties"));
    String jndiPath = props.getProperty("java.naming.provider.url").substring(7);
    File file = new File(jndiPath);
    file.mkdir();
    super.setUp();
  }

  @After
  public void tearDown() throws Exception {
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
