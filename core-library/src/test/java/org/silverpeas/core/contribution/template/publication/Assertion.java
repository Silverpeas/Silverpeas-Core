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

package org.silverpeas.core.contribution.template.publication;

import org.silverpeas.core.contribution.content.form.FormException;
import java.util.Arrays;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.junit.Assert;

/**
 * Module gathering all of the assertion operation on the objects handled in publication template.
 */
public final class Assertion {

  public static void assertEquals(final RecordTemplate expected, final RecordTemplate actual) {
    Assert.assertEquals(Arrays.asList(expected.getFieldNames()),
            Arrays.asList(actual.getFieldNames()));
    try {
      Assert.assertEquals(Arrays.asList(expected.getFieldTemplates()),
              Arrays.asList(actual.getFieldTemplates()));
    } catch (FormException ex) {
      Assert.fail(ex.getMessage());
    }
  }


}
