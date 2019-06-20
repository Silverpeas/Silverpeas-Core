/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.silverpeas.core.util.lang.SystemWrapper;

/**
 * @author silveryocha
 */
public class HttpUtil {

  private HttpUtil() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Centralizing the getting of HTTP client configured with proxy host and proxy port if any.
   * @return a {@link CloseableHttpClient} instance.
   */
  public static CloseableHttpClient httpClient() {
    final HttpClientBuilder builder = HttpClients.custom();
    final String proxyHost = SystemWrapper.get().getProperty("http.proxyHost");
    final String proxyPort = SystemWrapper.get().getProperty("http.proxyPort");
    if (StringUtil.isDefined(proxyHost) && StringUtil.isInteger(proxyPort)) {
      builder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
    }
    return builder.build();
  }
}
