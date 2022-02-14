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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.driver.googledriver;

import com.google.api.client.json.GenericJson;
import com.google.api.services.directory.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.domain.driver.googledriver.GoogleUserFilter.UserFilterException;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.silverpeas.core.admin.domain.driver.googledriver.GoogleUserBuilder.aUser;
import static org.silverpeas.core.util.CollectionUtil.asList;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class GoogleUserFilterTest {
  private static final List<User> allGoogleUsers = new ArrayList<>();
  private static final User USER_A = aUser("A", "/").withEmail("a@a.a", "work").build();
  private static final User USER_B = aUser("B", "/SIEGE/DRH/ARH").build();
  private static final User USER_C = aUser("C", "/").withEmail("c@c.c", "").build();
  private static final User USER_D = aUser("D", "/").build();
  private static final User USER_E = aUser("E", "/SIEGE/DRH").build();
  private static final User USER_F = aUser("F", "/").build();
  private static final User USER_G = aUser("G", "/SIEGE/EXCLUSION").withCustomEmail("g@g.g", "work").suspended().build();
  private static final User USER_H = aUser("H", "/").withEmail("hh@hh.hh", "work").withCustomEmail("h@h.h", "perso").build();
  private static final User USER_I = aUser("I", "/DSI/AMOA").build();
  private static final User USER_J = aUser("J", "/DSI/ATOA").suspended().build();
  private static final User USER_K = aUser("K", "/").withCustomSchemas("FLAGS", "isTata", "True").withCustomSchemas("FLAGS", "isToto", "true").build();

  @BeforeAll
  static void setup() {
    allGoogleUsers.add(USER_A);
    allGoogleUsers.add(USER_B);
    allGoogleUsers.add(USER_C);
    allGoogleUsers.add(USER_D);
    allGoogleUsers.add(USER_E);
    allGoogleUsers.add(USER_F);
    allGoogleUsers.add(USER_G);
    allGoogleUsers.add(USER_H);
    allGoogleUsers.add(USER_I);
    allGoogleUsers.add(USER_J);
    allGoogleUsers.add(USER_K);
  }

  @Test
  void simpleFilter() {
    assertResult(new GoogleUserFilter<>(allGoogleUsers, ""), allGoogleUsers);
  }

  @Test
  void simpleOuFilter() {
    assertResult(new GoogleUserFilter<>(allGoogleUsers, "orgUnitPath=/SIEGE/EXCLUSION"), singletonList(USER_G));
  }

  @Test
  void groundRuleNotCorrect() {
    final SilverpeasRuntimeException e = assertThrows(SilverpeasRuntimeException.class,
        () -> new GoogleUserFilter<>(allGoogleUsers, "orgUnitPath /SIEGE/EXCLUSION")
            .apply());
    assertThat(e.getMessage(), is("ground rule 'orgUnitPath /SIEGE/EXCLUSION' is not correct !"));
  }

  @Test
  void twoEqualOuFilterWithOrOperator() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "|(orgUnitPath=/SIEGE/EXCLUSION)(orgUnitPath=/SIEGE/DRH)"),
        asList(USER_G, USER_E));
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, " | (orgUnitPath = /SIEGE/EXCLUSION ) ( orgUnitPath   =   /SIEGE/DRH)"),
        asList(USER_G, USER_E));
  }

  @Test
  void oneEqualOneStartWithOuFilterWithOrOperator() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "|(orgUnitPath=/SIEGE/EXCLUSION)(orgUnitPath=/SIEGE/DRH%)"),
        asList(USER_G, USER_B, USER_E));
  }

  @Test
  void oneContainsOneEndWithOuFilterWithOrOperator() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "|(orgUnitPath=%EXCLUSION%)(orgUnitPath=%/DRH)"),
        asList(USER_G, USER_E));
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "|(orgUnitPath  =   %EXCLUSION%)(orgUnitPath  =   %/DRH)"),
        asList(USER_G, USER_E));
  }

  @Test
  void oneContainsOneEndWithOuFilterWithAndOperator() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "&(orgUnitPath=%EXCLUSION%)(orgUnitPath=%/DRH)"),
        emptyList());
  }

  @Test
  void dataFromArrayFilter() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "emails[&(address=g@g.g)(customType=work)]"),
        asList(USER_G));
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "emails[!(&(address=g@g.g)(customType=work))]"),
        asList(USER_A, USER_B, USER_C, USER_D, USER_E, USER_F, USER_H, USER_I, USER_J, USER_K));
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "!(emails[!(&(address=g@g.g)(customType=work))])"),
        asList(USER_G));
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "&(orgUnitPath=/SIeGE%)(emails[!(&(address=g@g.g)(customType=work))])"),
        asList(USER_B, USER_E));
  }

  @Test
  void customSchemasFilter() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "customSchemas.FLAGS.isToto=true"),
        asList(USER_K));
  }

  @Test
  void oneContainsOneNotEndWithOuFilterWithOrOperator() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "|(orgUnitPath=%EXCLUSion%)(!(orgUnitPath=%/DrH))"),
        asList(USER_G, USER_A, USER_B, USER_C, USER_D, USER_F, USER_H, USER_I, USER_J, USER_K));
  }

  @Test
  void nameFilter() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "|(name.familyName=fn_b)(name.givenName=GN_J)"),
        asList(USER_B, USER_J));
  }

  @Test
  void wrongPathFilter() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "|(name.famName=FN_B)(name.givenName=GN_J)"),
        asList(USER_J));
  }

  @Test
  void strangeOperatorOrCharacterButRightFilter() {
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "name.famName+=FN_B"),
        emptyList());
    assertResult(
        new GoogleUserFilter<>(allGoogleUsers, "name.famName += FN_B"),
        emptyList());
  }

  @Test
  void notValidPathFilterError() {
    final UserFilterException e = assertThrows(UserFilterException.class,
        () -> new GoogleUserFilter<>(allGoogleUsers, "|(name.famName.value=FN_B)(name.givenName=GN_J)")
            .apply());
    assertThat(e.getType(), is(GoogleUserFilter.ERROR.NOT_VALID_PATH_PART));
    assertThat(e.getMessage(),
        is("path part 'famName' of 'name.famName.value' in clause 'name.famName.value=FN_B' is not valid"));
    assertThat(e.getCriterion(), is("name.famName.value=FN_B"));
    assertThat(e.getPath(), is("name.famName.value"));
    assertThat(e.getPathPart(), is("famName"));
  }

  @Test
  void finalPathFilterError() {
    final UserFilterException e = assertThrows(UserFilterException.class,
        () -> new GoogleUserFilter<>(allGoogleUsers,
            "|(name.familyName.value.value=FN_B)(name.givenName=GN_J)").apply());
    assertThat(e.getType(), is(GoogleUserFilter.ERROR.FINAL_VALUE_PATH_PART));
    assertThat(e.getMessage(),
        is("path part 'familyName' of 'name.familyName.value.value' in clause 'name.familyName" +
            ".value.value=FN_B' represents a final value"));
    assertThat(e.getCriterion(), is("name.familyName.value.value=FN_B"));
    assertThat(e.getPath(), is("name.familyName.value.value"));
    assertThat(e.getPathPart(), is("familyName"));
  }

  private void assertResult(final GoogleUserFilter filter, final List<User> expected) {
    assertThat(filter.apply().stream().map(Object::toString).collect(Collectors.joining("#")),
        is(expected.stream().map(GenericJson::toString).collect(Collectors.joining("#"))));
  }
}