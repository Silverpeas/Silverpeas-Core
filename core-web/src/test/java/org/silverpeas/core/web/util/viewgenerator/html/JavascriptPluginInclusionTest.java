/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

package org.silverpeas.core.web.util.viewgenerator.html;

import org.apache.ecs.ElementContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.SettingBundleStub;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.JQUERY_JS;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.JQUERY_MIGRATE_DEV_JS;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.JQUERY_MIGRATE_MIN_JS;
import static org.silverpeas.core.web.util.viewgenerator.html.JavascriptPluginInclusion.JQUERY_UI_JS;

/**
 * Unit tests on the inclusion of jQuery, jQuery Migrate and jQuery UI in the pages of Silverpeas.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class JavascriptPluginInclusionTest {

  @RegisterExtension
  SettingBundleStub generalSettings = new SettingBundleStub("org.silverpeas.general")
      .put("web.resource.css.get.minified.enabled", "false");

  @BeforeEach
  void setUp() {
    URLUtil.setSilverpeasVersion("6.5");
    // the scripts are emitted only once per thread (and hence per request)
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @Test
  void includeJQueryWhenMinificationEnabledServesSilentMigrateBuild() {
    generalSettings.put("web.resource.js.get.minified.enabled", "true");

    String html = JavascriptPluginInclusion.includeJQuery(new ElementContainer()).toString();

    assertThat(html, containsString(withoutExtension(JQUERY_JS)));
    assertThat(html, containsString("jQuery.migrateMute = true;"));
    assertThat(html, containsString(withoutExtension(JQUERY_MIGRATE_MIN_JS)));
    assertThat(html, containsString(withoutExtension(JQUERY_UI_JS)));
    assertThat(html, containsString("ui-lightness/jquery-ui.min"));
  }

  @Test
  void includeJQueryWhenMinificationDisabledServesVerboseMigrateBuild() {
    generalSettings.put("web.resource.js.get.minified.enabled", "false");

    String html = JavascriptPluginInclusion.includeJQuery(new ElementContainer()).toString();

    assertThat(html, containsString(withoutExtension(JQUERY_JS)));
    assertThat(html, containsString("jQuery.migrateMute = false;"));
    assertThat(html, containsString(withoutExtension(JQUERY_MIGRATE_DEV_JS)));
    assertThat(html, not(containsString(withoutExtension(JQUERY_MIGRATE_MIN_JS))));
    assertThat(html, containsString(withoutExtension(JQUERY_UI_JS)));
  }

  @Test
  void includePdfViewerDeclaresItsLocaleAsAResourceAndNotAsAStylesheet() {
    generalSettings.put("web.resource.js.get.minified.enabled", "true");

    String html = JavascriptPluginInclusion.includePdfViewer(new ElementContainer()).toString();

    assertThat(html, containsString("rel='resource'"));
    assertThat(html, containsString("type='application/l10n'"));
    assertThat(html, containsString("/media/jsp/pdf/locale/locale"));
    assertThat(html, not(containsString("rel='stylesheet' href='/silverpeas/media/jsp/pdf/locale")));
  }

  /**
   * The calendar management template of AngularJS uses the progress message: the script has to be
   * already in the page, otherwise jQuery fetches it synchronously when the template is inserted.
   * Both the calendar plugin and the progress message tag include it, so it is emitted only once.
   */
  @Test
  void includeProgressMessageEmitsItsScriptOnceInAPage() {
    generalSettings.put("web.resource.js.get.minified.enabled", "true");

    String html = JavascriptPluginInclusion.includeProgressMessage(new ElementContainer()).toString();
    String again = JavascriptPluginInclusion.includeProgressMessage(new ElementContainer()).toString();

    assertThat(html, containsString("src='/silverpeas/util/javaScript/progressMessage"));
    assertThat(again, is(emptyString()));
  }

  /**
   * The cache-busting fingerprint is inserted before the extension of the resource name, so only
   * the part before the extension can be matched.
   */
  private static String withoutExtension(String scriptName) {
    return scriptName.substring(0, scriptName.lastIndexOf('.'));
  }
}
