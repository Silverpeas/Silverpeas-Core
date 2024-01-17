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

import com.google.api.client.util.SslUtils;
import org.silverpeas.core.util.lang.SystemWrapper;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.GeneralSecurityException;

import static java.net.http.HttpClient.Redirect.NORMAL;

/**
 * Centralizing the initializing of an {@link HttpRequest} or {@link HttpRequest.Builder} against
 * the different Silverpeas's HTTP requirements such as proxy configurations for example.
 * @author silveryocha
 */
public class HttpUtil {

  private HttpUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Centralizing the getting of HTTP client configured with proxy host and proxy port if any.
   * @return a {@link HttpClient} instance.
   */
  public static HttpClient httpClient() {
    return httpClient(null);
  }

  /**
   * Centralizing the getting of HTTP client configured with proxy host and proxy port if any and
   * with the acceptance of any SSL certificate whatever its validity.
   * @return a {@link HttpClient} instance.
   */
  public static HttpClient httpClientTrustingAnySslContext()
      throws GeneralSecurityException {
    return httpClient(SslUtils.trustAllSSLContext());
  }

  /**
   * Centralizing the getting of HTTP client configured with proxy host and proxy port if any and
   * with the acceptance of optional given SSL context.
   * @param sslContext an optional SSL context.
   * @return a {@link HttpClient} instance.
   */
  public static HttpClient httpClient(final SSLContext sslContext) {
    HttpClient.Builder builder = HttpClient.newBuilder().followRedirects(NORMAL);
    if (sslContext != null) {
      builder = builder.sslContext(sslContext);
    }
    final String proxyHost = SystemWrapper.get().getProperty("http.proxyHost");
    final String proxyPort = SystemWrapper.get().getProperty("http.proxyPort");
    if (StringUtil.isDefined(proxyHost) && StringUtil.isInteger(proxyPort)) {
      builder = builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))));
    }
    return builder.build();
  }

  /**
   * Centralizing the getting of HTTP request builder initialized with given URL.
   * @param url the URL to request.
   * @return a {@link HttpRequest.Builder} instance.
   */
  public static HttpRequest.Builder toUrl(final String url) {
    return toUri(URI.create(url));
  }

  /**
   * Centralizing the getting of HTTP request builder initialized with given URI.
   * @param uri the URI to request.
   * @return a {@link HttpRequest.Builder} instance.
   */
  public static HttpRequest.Builder toUri(final URI uri) {
    return HttpRequest.newBuilder().uri(uri);
  }
}
