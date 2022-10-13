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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.media.streaming;

import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.media.streaming.StreamingProvider;
import org.silverpeas.core.media.streaming.StreamingProvidersRegistry;

import javax.ws.rs.WebApplicationException;
import java.net.http.HttpResponse;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.silverpeas.core.util.HttpUtil.httpClient;
import static org.silverpeas.core.util.HttpUtil.toUrl;
import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * @author silveryocha
 */
public class StreamingRequester {

  private StreamingRequester() {
    // hidden constructor.
  }

  /**
   * Gets OEMBED data as JSON string.<br>
   * WARNING: performances can be altered when called from a list treatments as it performs an
   * HTTP request.
   * @return a JSON structure as string that represents oembed data.
   */
  protected static String getJsonOembedAsString(String homepageUrl) {
    return StreamingProvidersRegistry.get().getOembedUrl(homepageUrl).map(u -> u.replace("http:", "https:")).map(oembedUrl -> {
      try {
        final HttpResponse<String> response = httpClient().send(toUrl(oembedUrl)
            .header("Accept", APPLICATION_JSON)
            .build(), ofString());
        if (response.statusCode() != OK.getStatusCode()) {
          throw new WebApplicationException(response.statusCode());
        }
        String jsonResponse = response.body();
        for (StreamingProvider provider : StreamingProvidersRegistry.get().getAll()) {
          jsonResponse = jsonResponse.replaceAll("(?i)" + provider.getName(), provider.getName());
        }
        return jsonResponse;
      } catch (WebApplicationException wae) {
        SilverLogger.getLogger(StreamingRequester.class)
            .error("{0} -> HTTP ERROR {1}", oembedUrl, wae.getMessage());
        throw wae;
      } catch (Exception e) {
        SilverLogger.getLogger(StreamingRequester.class).error("{0} -> {1}", oembedUrl, e.getMessage());
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        throw new WebApplicationException(e);
      }
    }).orElse(EMPTY);
  }
}
