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

package org.silverpeas.core.util.html;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;


public class TestHtmlCleaner {

  @Test
  public void testBreadCrumbAsString() throws Exception {
    String html =
        "<div id=\"breadScrumb\"><a href=\"javascript:goSpace('WA1')\" class=\"space\" id=\"spaceWA1\">test</a> &gt; <a href=\"javascript:goSpace('WA4')\" class=\"space\" id=\"spaceWA4\">Archives</a> > <a href=\"javascript:goSpace('WA5')\" class=\"space\" id=\"spaceWA5\">Tous les composants</a> > <a href=\"Main\" class=\"component\" id=\"gallery74\">Galerie</a> > <a href=\"ViewAlbum?Id=68\" class=\"element\" id=\"68\">Mon Album</a></div>";

    String expectedResult = "test > Archives > Tous les composants > Galerie > Mon Album";

    HtmlCleaner cleaner = new HtmlCleaner();
    String result = cleaner.cleanHtmlFragment(html);
    Assert.assertEquals(expectedResult, result);
  }

}
