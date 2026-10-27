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
package org.silverpeas.core.contribution.content.form;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.ScriptInclusionRegistry;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.SettingBundleStub;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Unit tests on the scripts the forms include into the page.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class UtilTest {

  @RegisterExtension
  SettingBundleStub generalSettings = new SettingBundleStub("org.silverpeas.general")
      .put("web.resource.js.get.minified.enabled", "false")
      .put("web.resource.css.get.minified.enabled", "false");

  @BeforeEach
  void setUp() {
    URLUtil.setSilverpeasVersion("6.5");
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @Test
  void theFormsIncludeTheCheckingScriptsWhenThePageHasNotThemYet() {
    String includes = Util.getJavascriptIncludes("fr");

    assertThat(includes, containsString("/util/javaScript/checkForm."));
    assertThat(includes, containsString("/util/javaScript/dateUtils."));
  }

  /**
   * A page displaying a form can already include the checking scripts (by the withCheckFormScript
   * option of the look and feel for example): included twice, checkForm.js fails on the
   * redeclaration of its constants.
   */
  @Test
  void theFormsDoNotIncludeAgainTheScriptsAlreadyIncludedIntoThePage() {
    ScriptInclusionRegistry.register(URLUtil.getApplicationURL() + "/util/javaScript/checkForm.js");

    String includes = Util.getJavascriptIncludes("fr");

    assertThat(includes, not(containsString("/util/javaScript/checkForm.")));
    assertThat(includes, containsString("/util/javaScript/dateUtils."));
  }
}
