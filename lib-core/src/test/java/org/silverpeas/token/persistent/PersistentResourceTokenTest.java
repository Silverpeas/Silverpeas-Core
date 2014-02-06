/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.token.persistent;

import com.stratelia.webactiv.util.exception.SilverpeasException;
import org.junit.Test;
import org.silverpeas.token.exception.TokenException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Yohann Chastagnier
 */
public class PersistentResourceTokenTest {

  @Test
  public void testValidate() {
    PersistentResourceToken token = initializeToken();
    assertValidate(token, true);

    token = initializeToken();
    token.setId(null);
    assertValidate(token, true);

    token = initializeToken();
    token.setSaveDate(null);
    assertValidate(token, true);

    token = initializeToken();
    token.setValue(null);
    assertValidate(token, true);

    token = initializeToken();
    token.setValue("");
    assertValidate(token, true);

    token = initializeToken();
    token.setResource(new MyEntityReference("42"));
    assertValidate(token, true);

    token = initializeToken();
    token.setResource(new MyEntityReference(null));
    assertValidate(token, false);

    token = initializeToken();
    token.setResource(null);
    assertValidate(token, true);
  }

  private <T extends SilverpeasException> void assertValidate(final PersistentResourceToken token,
      final boolean isValid) {
    boolean isException = false;
    try {
      token.validate();
    } catch (final TokenException qe) {
      isException = true;
    }
    assertThat(isException, is(!isValid));
  }

  private PersistentResourceToken initializeToken() {
    final PersistentResourceToken token = new PersistentResourceToken(new MyEntityReference("26"),
        "token");
    token.setId(26L);
    token.setSaveCount(2);
    token.setSaveDate(java.sql.Date.valueOf("2012-01-01"));
    return token;
  }
}
