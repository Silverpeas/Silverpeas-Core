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