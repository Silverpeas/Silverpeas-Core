/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.XmlSettingBundle;
import org.silverpeas.core.util.lang.SystemWrapper;

import java.util.MissingResourceException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Unit tests on the getting of SilverpeasBundle instances.
 * @author miguel
 */
public class ResourceLocatorTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectory = new MavenTargetDirectoryRule(this);

  @Before
  public void initEnvVariables() {
    SystemWrapper.get()
        .getenv()
        .put("SILVERPEAS_HOME", mavenTargetDirectory.getResourceTestDirFile().getPath());
  }

  @Test
  public void useGeneralSettingShouldSucceed() {
    SettingBundle bundle = ResourceLocator.getGeneralSettingBundle();
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.getString("webdav.respository"), is("repository"));
  }

  @Test
  public void useAGivenSettingBundleShouldSucceed() {
    SettingBundle bundle = ResourceLocator.getSettingBundle(
        "org.silverpeas.authentication.settings.authenticationSettings");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.getBoolean("loginAnswerCrypted"), is(false));
  }

  @Test
  public void getAnExistingOrNonExistingSettingBundleShouldSucceed() {
    SettingBundle bundle = ResourceLocator.getSettingBundle("org.silverpeas.tartempion.toto");
    assertThat(bundle, is(notNullValue()));
  }

  @Test
  public void testExistenceOfANonExistingSettingBundleShouldReturnFalse() {
    SettingBundle bundle = ResourceLocator.getSettingBundle("org.silverpeas.tartempion.toto");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.exists(), is(false));
  }

  @Test
  public void testExistenceOfAnExistingSettingBundleShouldReturnTrue() {
    SettingBundle bundle = ResourceLocator.getSettingBundle(
        "org.silverpeas.authentication.settings.authenticationSettings");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.exists(), is(true));
  }

  @Test(expected = MissingResourceException.class)
  public void useANonExistingSettingBundleShouldFail() {
    SettingBundle bundle = ResourceLocator.getSettingBundle("org.silverpeas.tartempion.toto");
    assertThat(bundle, is(notNullValue()));
    bundle.getString("toto");
  }

  @Test(expected = MissingResourceException.class)
  public void getAMissingPropertyInAnExistingSettingBundleShouldFail() {
    SettingBundle bundle = ResourceLocator.getSettingBundle(
        "org.silverpeas.authentication.settings.authenticationSettings");
    assertThat(bundle, is(notNullValue()));
    bundle.getString("toto");
  }

  @Test
  public void useGeneralLocalizationBundleShouldSucceed() {
    LocalizationBundle beforeBundleEn = ResourceLocator.getGeneralLocalizationBundle("en");
    LocalizationBundle beforeBundleFr = ResourceLocator.getGeneralLocalizationBundle("fr");
    LocalizationBundle bundleEn = ResourceLocator.getGeneralLocalizationBundle("en");
    LocalizationBundle bundleFr = ResourceLocator.getGeneralLocalizationBundle("fr");

    assertThat(bundleEn, sameInstance(beforeBundleEn));
    assertThat(bundleFr, sameInstance(beforeBundleFr));

    assertThat(bundleEn, is(notNullValue()));
    assertThat(bundleEn.getString("GML.cancel"), is("Cancel"));
    assertThat(bundleEn.getString("GML.cancel"), is("Cancel"));
    assertThat(bundleFr, is(notNullValue()));
    assertThat(bundleFr.getString("GML.cancel"), is("Annuler"));
    assertThat(bundleFr.getString("GML.cancel"), is("Annuler"));
  }

  @Test
  public void useAGivenLocalizationBundleShouldSucceed() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication", "en");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.getString("authentication.logon.loginMissing"),
        is("Please enter your login"));
  }

  @Test
  public void useAGivenLocalizationBundleWithoutSpecifiedLocaleShouldSucceed() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.getString("authentication.logon.loginMissing"),
        is("Veuillez renseigner votre login"));
  }

  @Test
  public void useAGivenLocalizationBundleForAMissingLocaleShouldLoadRootBundle() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication", "pl");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.getString("authentication.logon.loginMissing"),
        is("Veuillez renseigner votre login"));
  }

  @Test
  public void getAnExistingOrNonExistingLocalizationBundleShouldSucceed() {
    LocalizationBundle bundle =
        ResourceLocator.getLocalizationBundle("org.silverpeas.tartempion.multilang.toto");
    assertThat(bundle, is(notNullValue()));
  }

  @Test
  public void testExistenceOfANonexistingLocalizationBundleShouldReturnFalse() {
    LocalizationBundle bundle =
        ResourceLocator.getLocalizationBundle("org.silverpeas.tartempion.multilang.toto");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.exists(), is(false));
  }

  @Test
  public void testExistenceOfAnExistingLocalizationBundleShouldReturnTrue() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.exists(), is(true));
  }

  @Test
  public void testExistenceOfAnExistingLocalizationBundleForAMissingLocaleShouldReturnTrue() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication", "pl");
    // pl doesn't exist, then it is the root bundle that is loaded
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.exists(), is(true));
  }

  @Test(expected = MissingResourceException.class)
  public void useANonExistingLocalizationBundleShouldFail() {
    LocalizationBundle bundle =
        ResourceLocator.getLocalizationBundle("org.silverpeas.tartempion.multilang.toto");
    assertThat(bundle, is(notNullValue()));
    bundle.getString("toto");
  }

  @Test(expected = MissingResourceException.class)
  public void getAMissingPropertyInAnExistingLocalizationBundleShouldFail() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication");
    assertThat(bundle, is(notNullValue()));
    bundle.getString("toto");
  }

  @Test(expected = MissingResourceException.class)
  public void getAMissingPropertyInAnExistingLocalizationBundleForANonExistingLocaleShouldFail() {
    LocalizationBundle bundle = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.authentication.multilang.authentication", "pl");
    assertThat(bundle, is(notNullValue()));
    bundle.getString("toto");
  }

  @Test(expected = ClassCastException.class)
  public void getASettingBundleForALocalizedBundleShouldFail() {
    LocalizationBundle bundle =
        ResourceLocator.getLocalizationBundle("org.silverpeas.titi.grominet");
    assertThat(bundle, is(notNullValue()));
    ResourceLocator.getSettingBundle("org.silverpeas.titi.grominet");
  }

  @Test
  public void useAGivenXmlSettingBundleShouldSucceed() {
    XmlSettingBundle bundle =
        ResourceLocator.getXmlSettingBundle("org.silverpeas.util.settings.dbDriverSettings");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.getString("DBDriver-configuration.Drivers-Definition.BD_Silverpeas-configuration.DriverName"),
        is(notNullValue()));
  }

  @Test
  public void getAnExistingOrNonExistingXmlSettingBundleShouldSucceed() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle("org.silverpeas.tartempion.xmlconf");
    assertThat(bundle, is(notNullValue()));
  }

  @Test
  public void testExistenceOfANonExistingXmlSettingBundleShouldReturnFalse() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle("org.silverpeas.tartempion.xmlconf");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.exists(), is(false));
  }

  @Test
  public void testExistenceOfAnExistingXmlSettingBundleShouldReturnTrue() {
    XmlSettingBundle bundle =
        ResourceLocator.getXmlSettingBundle("org.silverpeas.util.settings.dbDriverSettings");
    assertThat(bundle, is(notNullValue()));
    assertThat(bundle.exists(), is(true));
  }

  @Test(expected = MissingResourceException.class)
  public void useANonExistingXmlSettingBundleShouldFail() {
    XmlSettingBundle bundle = ResourceLocator.getXmlSettingBundle("org.silverpeas.tartempion.xmlconf");
    assertThat(bundle, is(notNullValue()));
    bundle.getString("toto");
  }

  @Test(expected = MissingResourceException.class)
  public void getAMissingPropertyInAnXmlExistingSettingBundleShouldFail() {
    XmlSettingBundle bundle =
        ResourceLocator.getXmlSettingBundle("org.silverpeas.util.settings.dbDriverSettings");
    assertThat(bundle, is(notNullValue()));
    bundle.getString("DBDriver-configuration.Toto");
  }

  @Test(expected = ClassCastException.class)
  public void getASettingBundleForAXmlBundleShouldFail() {
    XmlSettingBundle bundle =
        ResourceLocator.getXmlSettingBundle("org.silverpeas.titi.tutu");
    assertThat(bundle, is(notNullValue()));
    ResourceLocator.getSettingBundle("org.silverpeas.titi.tutu");
  }
}
