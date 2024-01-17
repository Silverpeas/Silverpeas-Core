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
package org.silverpeas.core.web.proxy;

import org.apache.commons.io.IOUtils;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;
import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.silverpeas.core.util.HttpUtil.*;
import static org.silverpeas.core.util.StringUtil.EMPTY;

/**
 * This servlet plays the role of a light proxy.
 * It permits to perform requests on external servers managed by the one of Silverpeas.
 * <p>
 * The rules are defined into the property file
 * <b>org.silverpeas.proxy.settings.proxy.properties</b>
 * </p>
 * <p>
 * The proxy performs only GET requests.
 * </p>
 * @author silveryocha
 */
public class ProxyServlet extends SilverpeasAuthenticatedHttpServlet {
  private static final long serialVersionUID = 5047753714615164680L;

  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.proxy.settings.proxy");

  @Override
  public void doGet(final HttpServletRequest req, final HttpServletResponse res) {
    if (!isEnabled()) {
      throwHttpForbiddenError();
    }
    final String targetUrl = getTargetUrl(req);
    final String userIdentity = ofNullable(User.getCurrentRequester())
        .map(user -> "User " + user.getId())
        .orElse("Someone");
    final Supplier<String> errorDebugSupplier = () -> format(
        "{0} tried to use proxy with URL ''{1}''", userIdentity, targetUrl);
    if (isAuthorizedTargetUrl(targetUrl)) {
      try {
        final HttpResponse<InputStream> response = client().send(toUrl(targetUrl)
            .header("Accept", WILDCARD)
            .build(), ofInputStream());
        res.setStatus(response.statusCode());
        response.headers()
            .map()
            .entrySet()
            .stream()
            .filter(e -> e.getKey().equalsIgnoreCase("content-type"))
            .forEach(e -> e.getValue().forEach(v -> res.setHeader(e.getKey(), v)));
        final long copied;
        try (final InputStream body = response.body()) {
          copied = IOUtils.copyLarge(body, res.getOutputStream());
        }
        SilverLogger.getLogger(this)
            .debug("{0} using proxy with URL ''{1}'' and got {2} bytes (status={3})", userIdentity,
                targetUrl, String.valueOf(copied), String.valueOf(response.statusCode()));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        SilverLogger.getLogger(this).debug(errorDebugSupplier);
        throwHttpNotFoundError();
      } catch (IOException | GeneralSecurityException e) {
        SilverLogger.getLogger(this).debug(errorDebugSupplier);
        throwHttpNotFoundError();
      }
    } else {
      SilverLogger.getLogger(this).warn(errorDebugSupplier.get());
      throwHttpForbiddenError();
    }
  }

  private HttpClient client() throws GeneralSecurityException {
    return settings.getBoolean("ssl.handshake", true) ?
        httpClient() :
        httpClientTrustingAnySslContext();
  }

  private String getTargetUrl(HttpServletRequest req) {
    return ofNullable(req.getPathInfo())
        .map(p -> p.substring(1))
        .map(p -> fromUri(req.getScheme() + "://" + p))
        .map(b -> {
          req.getParameterMap().forEach(b::queryParam);
          return b.build().toString();
        })
        .orElse(EMPTY);
  }

  private boolean isAuthorizedTargetUrl(final String targetUrl) {
    return ofNullable(targetUrl)
        .filter(StringUtil::isDefined)
        .stream()
        .flatMap(u -> Stream.of(settings.getString("rules", EMPTY).split(" "))
          .filter(StringUtil::isDefined)
          .map(Pattern::compile)
          .map(p -> p.matcher(u))
          .map(Matcher::matches))
          .filter(Boolean::booleanValue)
        .findFirst()
        .orElse(false);
  }

  private boolean isEnabled() {
    return settings.getBoolean("enabled", false);
  }
}
