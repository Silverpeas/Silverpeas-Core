/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.glossary;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ehugonnet
 */
public class HighlightGlossaryTermsTest {

  public HighlightGlossaryTermsTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of highlight method, of class HighlightGlossaryTerms.
   */
  @Test
  public void testHighlightWithLink() {
    String term = "Bart Simpson";
    String publication = "<a href=\"www.simpson.com\" id=\"Bart Simpson\">Bart Simpson</a>";
    String definition = "Skatter";
    String className = "cool";
    boolean onlyFirst = true;
    HighlightGlossaryTerms instance = new HighlightGlossaryTerms();
    String expResult = "<a href=\"www.simpson.com\" id=\"Bart Simpson\">Bart Simpson</a>";
    String result = instance.highlight(term, publication, definition, className, onlyFirst);
    assertEquals(expResult, result);
  }


  @Test
  public void testHighlightAll() {
    String term = "Bart Simpson";
    String publication = "<p>Bart Simpson is the cooleast sk8ter in the world. Miss Krabappel doesn't see le good part in Bart Simpson.</p>";
    String definition = "Skatter";
    String className = "cool";
    boolean onlyFirst = false;
    HighlightGlossaryTerms instance = new HighlightGlossaryTerms();
    String expResult = "<p><a href=\"#\" class=\"cool\" title=\"Skatter\">Bart Simpson</a> is the cooleast sk8ter in the world. Miss Krabappel doesn't see le good part in <a href=\"#\" class=\"cool\" title=\"Skatter\">Bart Simpson</a>.</p>";
    String result = instance.highlight(term, publication, definition, className, onlyFirst);
    assertEquals(expResult, result);
  }

   @Test
  public void testHighlightFirst() {
    String term = "Bart Simpson";
    String publication = "<p>Bart Simpson is the cooleast sk8ter in the world. Miss Krabappel doesn't see le good part in Bart Simpson.</p>";
    String definition = "Skatter";
    String className = "cool";
    boolean onlyFirst = true;
    HighlightGlossaryTerms instance = new HighlightGlossaryTerms();
    String expResult = "<p><a href=\"#\" class=\"cool\" title=\"Skatter\">Bart Simpson</a> is the cooleast sk8ter in the world. Miss Krabappel doesn't see le good part in Bart Simpson.</p>";
    String result = instance.highlight(term, publication, definition, className, onlyFirst);
    assertEquals(expResult, result);
  }


  @Test
  public void testHighlightWithLinkInParagraph() {
    String term = "Bart Simpson";
    String publication = "<p>Bart Simpson is the cooleast sk8ter in the world. Miss Krabappel doesn't see le good part in <a href=\"www.simpson.com\" id=\"Bart Simpson\">Bart Simpson</a>.</p>";
    String definition = "Skatter";
    String className = "cool";
    boolean onlyFirst = false;
    HighlightGlossaryTerms instance = new HighlightGlossaryTerms();
    String expResult = "<p><a href=\"#\" class=\"cool\" title=\"Skatter\">Bart Simpson</a> is the cooleast sk8ter in the world. Miss Krabappel doesn't see le good part in <a href=\"www.simpson.com\" id=\"Bart Simpson\">Bart Simpson</a>.</p>";
    String result = instance.highlight(term, publication, definition, className, onlyFirst);
    assertEquals(expResult, result);
  }

  @Test
  public void testHighlightWithComments() {
    String term = "Bart Simpson";
    String publication = "<p>Bart Simpson is the cooleast sk8ter in the world.<!-- I love Bart Simpson --> Miss Krabappel doesn't see le good part in Bart Simpson.</p>";
    String definition = "Skatter";
    String className = "cool";
    boolean onlyFirst = false;
    HighlightGlossaryTerms instance = new HighlightGlossaryTerms();
    String expResult = "<p><a href=\"#\" class=\"cool\" title=\"Skatter\">Bart Simpson</a> is the cooleast sk8ter in the world.<!-- I love Bart Simpson --> Miss Krabappel doesn't see le good part in <a href=\"#\" class=\"cool\" title=\"Skatter\">Bart Simpson</a>.</p>";
    String result = instance.highlight(term, publication, definition, className, onlyFirst);
    assertEquals(expResult, result);
  }
}
