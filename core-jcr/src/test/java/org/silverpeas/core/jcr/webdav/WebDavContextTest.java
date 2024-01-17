/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.jcr.webdav;

import org.apache.jackrabbit.webdav.util.EncodeUtil;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheAccessor;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.test.unit.UnitTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test the creation and the use of a WebDAV context defining access to a document in the JCR by
 * WebDAV for a given user.
 * @author Yohann Chastagnier
 */
@UnitTest
class WebDavContextTest {

  private static final String AUTH_TOKEN = "tokenWith16Chars";
  private static final String FILENAME_WITH_SPECIAL_CHARS =
      "toto&é'()@ç+^i=¨*µ%ù!§:;.,?&&&?{}][`.doc";
  public static final String WEBDAV_DOCUMENT = "/webdav/document/";

  private final CacheAccessor cacheAccessor = CacheAccessorProvider.getApplicationCacheAccessor();

  @BeforeEach
  public void setup() {
    cacheAccessor.getCache().clear();
    cacheAccessor.getCache().put(WebDavContext.WEBDAV_JCR_URL_SUFFIX + "dummy", "dummy");
  }

  @AfterEach
  public void tearDown() {
    assertThat(
        cacheAccessor.getCache().get(WebDavContext.WEBDAV_JCR_URL_SUFFIX + "dummy", String.class),
        is("dummy"));
    cacheAccessor.getCache().clear();
  }

  @Test
  @DisplayName("Create a WebDAV context should put data in the cache and that cache should be " +
      "emptied once the context cleared")
  void shouldClearTheCache() {
    SimpleCache cache = cacheAccessor.getCache();
    assertThat(cache.get(WebDavContext.WEBDAV_JCR_URL_SUFFIX + AUTH_TOKEN), nullValue());

    WebDavContext context =
        WebDavContext.createWebDavContext(AUTH_TOKEN, WEBDAV_DOCUMENT + FILENAME_WITH_SPECIAL_CHARS
        );
    assertThat(context, notNullValue());

    assertThat(cache.get(WebDavContext.WEBDAV_JCR_URL_SUFFIX + AUTH_TOKEN, String.class),
        is("document/" + EncodeUtil.escape(FILENAME_WITH_SPECIAL_CHARS)));

    WebDavContext.clearWebDavContext(AUTH_TOKEN);

    assertThat(cache.get(WebDavContext.WEBDAV_JCR_URL_SUFFIX + AUTH_TOKEN), nullValue());
  }

  @Test
  @DisplayName("Create a WebDAV context should generate a well-encoded WebDAV URL of the document" +
      " in the JCR")
  void shouldBeWellEncoded() {
    String expectedWebdavUrl =
        "/webdav/" + AUTH_TOKEN + "/" + EncodeUtil.escape(FILENAME_WITH_SPECIAL_CHARS);

    WebDavContext context =
        WebDavContext.createWebDavContext(AUTH_TOKEN,
            WEBDAV_DOCUMENT + FILENAME_WITH_SPECIAL_CHARS);
    MatcherAssert.assertThat(context.getDocumentURL(),
        is(WEBDAV_DOCUMENT + FILENAME_WITH_SPECIAL_CHARS));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(expectedWebdavUrl));
  }

  @Test
  @DisplayName("The URL of the document should be correctly decoded from its tiny WebDAV URL")
  void shouldBeWellDecoded() {
    String webdavUrl =
        WebDavContext.createWebDavContext(AUTH_TOKEN, WEBDAV_DOCUMENT + FILENAME_WITH_SPECIAL_CHARS)
            .getWebDavUrl();

    WebDavContext context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(),
        is(WEBDAV_DOCUMENT + EncodeUtil.escape(FILENAME_WITH_SPECIAL_CHARS)));
    assertThat(EncodeUtil.unescape(context.getDocumentURL()),
        is(WEBDAV_DOCUMENT + FILENAME_WITH_SPECIAL_CHARS));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));
  }

  @ParameterizedTest
  @DisplayName("All the parts of a path in the URL of the document should be correctly decoded " +
      "from the tiny WebDAV URL")
  @ValueSource(strings = {"/webdav/document/a", "/webdav/document",
      "/webdav/document/dummyFileName"})
  void parentPathShouldBeWellDecoded(final String documentURL) {
    String webdavUrl =
        WebDavContext.createWebDavContext(AUTH_TOKEN, documentURL).getWebDavUrl();

    WebDavContext context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(), is(documentURL));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));
  }

  @Test
  @DisplayName(
      "The directory path in the URL of the document should be correctly decoded from the " +
          "tiny WebDAV URL")
  void directoryPathShouldBeWellDecoded() {
    String webdavUrl =
        WebDavContext.createWebDavContext(AUTH_TOKEN, "/repo/webdav/filename.xlsx").getWebDavUrl();

    WebDavContext context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(), is("/repo/webdav/filename.xlsx"));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));

    webdavUrl = "/repo/webdav/" + AUTH_TOKEN;
    context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(), is("/repo/webdav"));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));

    webdavUrl = "/repo/webdav/" + AUTH_TOKEN + "/";
    context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(), is("/repo/webdav/"));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));
  }

  @Test
  @DisplayName("An invalid WebDav context is got when the tiny URL has no token encoded in it")
  void noDecodingWhenNoToken() {
    String webdavUrl = "/repo/webdav/without/token/filename.xlsx";

    WebDavContext context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(),
        is("/repo/webdav/without/token/filename.xlsx"));
    MatcherAssert.assertThat(context.getToken(), is(emptyString()));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));

    webdavUrl = "/repo/webdav/without/token";
    context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(), is("/repo/webdav/without/token"));
    MatcherAssert.assertThat(context.getToken(), is(emptyString()));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));

    webdavUrl = "/repo/webdav/without/token/";
    context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert
        .assertThat(context.getDocumentURL(), is("/repo/webdav/without/token/"));
    MatcherAssert.assertThat(context.getToken(), is(emptyString()));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));
  }

  @Test
  @DisplayName("A WebDav context is well got even if the tiny WebDav URL contains several tokens")
  void decodingWithSuccessWhenItExistsSeveralWebdavTokens() {
    String webdavUrl = WebDavContext.createWebDavContext(AUTH_TOKEN, "/repo/webdav/a/b/webdav.xlsx")
        .getWebDavUrl();

    WebDavContext context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert
        .assertThat(context.getDocumentURL(), is("/repo/webdav/a/b/webdav.xlsx"));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));

    webdavUrl = "/repo/webdav/" + AUTH_TOKEN;
    context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(), is("/repo/webdav/a/b"));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));

    webdavUrl = "/repo/webdav/" + AUTH_TOKEN + "/";
    context = WebDavContext.getWebDavContext(webdavUrl);
    MatcherAssert.assertThat(context.getDocumentURL(), is("/repo/webdav/a/b/"));
    MatcherAssert.assertThat(context.getToken(), is(AUTH_TOKEN));
    MatcherAssert.assertThat(context.getWebDavUrl(), is(webdavUrl));
  }
}