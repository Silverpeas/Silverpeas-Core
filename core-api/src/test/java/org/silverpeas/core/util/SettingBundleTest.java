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
package org.silverpeas.core.util;

import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasBundleList;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

/**
 * @author Yohann Chastagnier
 */
public class SettingBundleTest {

  private SettingBundle settingBundle =
      ResourceLocator.getSettingBundle("org.silverpeas.jobDomainPeas.settings.usersCSVFormat");

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Test
  public void getStringListOnNameSuffixShouldReturn6Values() {
    List<String> values = settingBundle.getStringList("User", "Name");
    assertThat(values, contains("Nom", "Prenom", "Login", "Email", "", "MotDePasse"));
  }

  @Test
  public void getStringListOnNameSuffixShouldReturn0ValueOnNoMaxAnd6ValuesWhenMaxIsSpecified() {
    SilverpeasBundleList values = settingBundle.getStringList("User", "Unknown");
    assertThat(values, empty());
    values = settingBundle.getStringList("User", "Unknown", 6);
    assertThat(values, contains("", "", "", "", "", ""));
  }

  @Test
  public void getStringListOnNameSuffixShouldReturn3ValuesAsRequested() {
    SilverpeasBundleList values = settingBundle.getStringList("User", "Name", 3);
    assertThat(values, contains("Nom", "Prenom", "Login"));
  }

  @Test
  public void getStringListOnNameSuffixShouldReturnEmptyArray() {
    SilverpeasBundleList values = settingBundle.getStringList("User_", "Name");
    assertThat(values, empty());
  }

  @Test
  public void getStringListOnNameSuffixShouldAlsoReturnEmptyArray() {
    SilverpeasBundleList values = settingBundle.getStringList("User", "Unknown");
    assertThat(values, empty());
  }

  @Test
  public void getStringListOnNameSuffixShouldAgainReturnEmptyArray() {
    SilverpeasBundleList values = settingBundle.getStringList("User", ".Name");
    assertThat(values, empty());
  }
}