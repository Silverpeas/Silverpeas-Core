/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.admin.domain.driver.googledriver;

import com.google.api.services.directory.model.User;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.domain.driver.googledriver.GoogleEntitySimpleAttributePathResolver.AttributePathDecoder;
import org.silverpeas.core.admin.domain.driver.googledriver.GoogleEntitySimpleAttributePathResolver.SimpleAttributePathDecoder;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.admin.domain.driver.googledriver.GoogleEntitySimpleAttributePathResolver.decodePath;
import static org.silverpeas.core.admin.domain.driver.googledriver.GoogleEntitySimpleAttributePathResolver.resolve;
import static org.silverpeas.core.admin.domain.driver.googledriver.GoogleUserBuilder.aUser;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class GoogleEntitySimpleAttributePathResolverTest {

  private static final User USER_A = aUser("A", "/").withEmail("a@a.a", "work").build();
  private static final User USER_B = aUser("B", "/SIEGE/DRH/ARH").build();
  private static final String ORG_UNIT_PATH_ATTRIBUTE_PATH = "orgUnitPath";
  private static final String EMAILS_ADDRESS_ATTRIBUTE_PATH = "emails.address";

  @Test
  void orgUnitPathAttributePathDecoding() {
    final AttributePathDecoder attributePathDecoder = decodePath(ORG_UNIT_PATH_ATTRIBUTE_PATH);
    assertThat(attributePathDecoder, instanceOf(SimpleAttributePathDecoder.class));
    assertThat(attributePathDecoder.path, is(ORG_UNIT_PATH_ATTRIBUTE_PATH));
    assertThat(attributePathDecoder.explodedPath, arrayContaining(ORG_UNIT_PATH_ATTRIBUTE_PATH));
    assertThat(attributePathDecoder.match, is(true));
  }

  @Test
  void emailAttributePathDecoding() {
    final AttributePathDecoder attributePathDecoder = decodePath(EMAILS_ADDRESS_ATTRIBUTE_PATH);
    assertThat(attributePathDecoder, instanceOf(SimpleAttributePathDecoder.class));
    assertThat(attributePathDecoder.path, is(EMAILS_ADDRESS_ATTRIBUTE_PATH));
    assertThat(attributePathDecoder.explodedPath, arrayContaining("emails", "address"));
    assertThat(attributePathDecoder.match, is(true));
  }

  @Test
  void resolveOrgUnitPath() {
    final Object result = resolve(USER_A, ORG_UNIT_PATH_ATTRIBUTE_PATH);
    assertThat(result, not(nullValue()));
    assertThat(result, is("/"));
  }

  @Test
  void resolveOtherOrgUnitPath() {
    final Object result = resolve(USER_B, ORG_UNIT_PATH_ATTRIBUTE_PATH);
    assertThat(result, not(nullValue()));
    assertThat(result, is("/SIEGE/DRH/ARH"));
  }

  @SuppressWarnings("unchecked")
  @Test
  void resolveSeveralEmailAddresses() {
    final Object result = resolve(USER_A, EMAILS_ADDRESS_ATTRIBUTE_PATH);
    assertThat(result, not(nullValue()));
    assertThat(result, instanceOf(List.class));
    final List<String> resultList = (List) result;
    assertThat(resultList, contains("GN_A.FN_A@silverpeas.org", "a@a.a"));
  }

  @Test
  void resolveOneEmailAddress() {
    final Object result = resolve(USER_B, EMAILS_ADDRESS_ATTRIBUTE_PATH);
    assertThat(result, not(nullValue()));
    assertThat(result, is("GN_B.FN_B@silverpeas.org"));
  }
}