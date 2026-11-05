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

import org.junit.jupiter.api.Test;
import org.silverpeas.core.util.URLUtil;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class RichEditorPluginTest {
  @Test
  void nativePluginLoadsDependenciesInOrderAndDeduplicatesRepeatedInclusions() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
    try (var urls = mockStatic(URLUtil.class, CALLS_REAL_METHODS)) {
      urls.when(() -> URLUtil.getMinifiedWebResourceUrl(anyString())).thenAnswer(call -> call.getArgument(0));
      urls.when(() -> URLUtil.addFingerprintVersionOn(anyString())).thenAnswer(call -> call.getArgument(0));
      var plugin = new DefaultWebPlugin();
      plugin.init();
      var html = plugin.getHtml("richEditor", "fr").toString();
      assertTrue(html.contains("richEditor"));
      int previous = -1;
      for (var resource : new String[]{"richEditor", "markdown-it-15.0.3", "silverpeas-markdown",
          "silverpeas-rich-content", "tiptap-3.32.2", "silverpeas-rich-editor"}) {
        int position = html.indexOf(resource, previous + 1);
        assertTrue(position > previous, resource + " must be included after its dependencies");
        previous = position;
      }
      assertFalse(plugin.getHtml("richEditor", "fr").toString().contains("<script"));
      assertFalse(plugin.getHtml("richEditor", "fr").toString().contains("<link"));
    }
  }
}
