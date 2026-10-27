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
package org.silverpeas.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the registry of the scripts included into the page being rendered.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class ScriptInclusionRegistryTest {

  private static final String SCRIPT = "/silverpeas/util/javaScript/checkForm.js";

  @BeforeEach
  void clearTheRequestCache() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @Test
  void aScriptIsIncludedOnlyAtItsFirstRegistrationInAPage() {
    assertThat(ScriptInclusionRegistry.register(SCRIPT), is(true));
    assertThat(ScriptInclusionRegistry.register(SCRIPT), is(false));
    assertThat(ScriptInclusionRegistry.register("/silverpeas/util/javaScript/dateUtils.js"),
        is(true));
  }

  @Test
  void aScriptIsIncludedAgainInTheNextPage() {
    assertThat(ScriptInclusionRegistry.register(SCRIPT), is(true));
    clearTheRequestCache();
    assertThat(ScriptInclusionRegistry.register(SCRIPT), is(true));
  }
}
