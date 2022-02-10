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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.contribution.ContributionOperationContextPropertyHandler;
import org.silverpeas.core.notification.user.client.NotificationManagerSettings;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.ServiceProvider;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getRequestCacheService;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getThreadCacheService;
import static org.silverpeas.core.util.StringUtil.*;

/**
 * This class handles the feature that permits to skip the user subscription notification sending.
 * @author Yohann Chastagnier
 */
@Technical
@Service
public class UserSubscriptionNotificationSendingHandler implements
    ContributionOperationContextPropertyHandler {

  private static final String SENDING_NOT_ENABLED_KEY =
      UserSubscriptionNotificationSendingHandler.class.getName() + "#SENDING_ENABLED";

  private static final String SENDING_NOT_ENABLED_JMS_WAY_KEY =
      UserSubscriptionNotificationSendingHandler.class.getName() + "#SENDING_ENABLED_JMS_WAY";

  public static UserSubscriptionNotificationSendingHandler get() {
    return ServiceProvider.getService(UserSubscriptionNotificationSendingHandler.class);
  }

  /**
   * Hidden constructor.
   */
  protected UserSubscriptionNotificationSendingHandler() {
  }

  /**
   * Verifies from a request if it is indicated that subscription notification sending must be
   * skipped for the current request (from request parameters or request headers).
   * @param request the current HTTP request.
   */
  @Override
  public void parseForProperty(HttpServletRequest request) {
    if (NotificationManagerSettings.isSubscriptionNotificationConfirmationEnabled()) {
      final String parameter = request.getParameter(
          UserSubscriptionNotificationBehavior
              .SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM);
      final String header = request.getHeader(
          UserSubscriptionNotificationBehavior
              .SUBSCRIPTION_NOTIFICATION_SENDING_CONFIRMATION_HTTP_PARAM);
      final Confirmation confirmation = getMergedConfirmations(parameter, header);
      getRequestCacheService().getCache().put(SENDING_NOT_ENABLED_KEY, confirmation);
    }
  }

  /**
   * Forces the context by indicating to skip the sending of subscription notifications.
   * <p>
   * This method permits to indicate a such context by bypassing an HTTP request decoding.
   * </p>
   * <p>
   * The context is also registered into a thread cache, like it is done with the HTTP request
   * decoding.
   * </p>
   */
  public void skipNotificationSend() {
    getOrCreateConfirmation().skip = true;
  }

  /**
   * Indicates if the user subscription notification sending is enabled for the current request.
   * @return true if enabled, false otherwise.
   */
  public boolean isSubscriptionNotificationEnabledForCurrentRequest() {
    final Confirmation confirmation = getConfirmation();
    return confirmation.isNotificationSendEnabled();
  }

  /**
   * Gets a user note to paste into subscription notification message from the current request.
   * @return true if enabled, false otherwise.
   */
  public String getSubscriptionNotificationUserNoteFromCurrentRequest() {
    final Confirmation confirmation = getConfirmation();
    if (confirmation.isNotificationSendEnabled()) {
      return confirmation.note;
    }
    return null;
  }

  private Confirmation getMergedConfirmations(final String parameter, final String header) {
    Confirmation fromParameters = decodeConfirmation(parameter);
    Confirmation fromHeaders = decodeConfirmation(header);
    final Confirmation mergedConfirmation = new Confirmation();
    mergedConfirmation.skip = fromParameters.skip || fromHeaders.skip;
    mergedConfirmation.note = defaultStringIfNotDefined(fromParameters.note, fromHeaders.note);
    return mergedConfirmation;
  }

  private Confirmation decodeConfirmation(final String value) {
    final String decodedValue;
    if (isDefined(value)) {
      if (value.startsWith("{")) {
        decodedValue = value;
      } else {
        decodedValue = new String(fromBase64(value));
      }
    } else {
      decodedValue = "{}";
    }
    return JSONCodec.decode(decodedValue, Confirmation.class);
  }

  private Confirmation getConfirmation() {
    Confirmation confirmation =
        getRequestCacheService().getCache().get(SENDING_NOT_ENABLED_KEY, Confirmation.class);
    if (confirmation == null) {
      confirmation = getThreadCacheService().getCache()
          .get(SENDING_NOT_ENABLED_JMS_WAY_KEY, Confirmation.class);
    }
    return confirmation == null ? new Confirmation() : confirmation;
  }

  private Confirmation getOrCreateConfirmation() {
    return getRequestCacheService().getCache()
        .computeIfAbsent(SENDING_NOT_ENABLED_KEY, Confirmation.class, Confirmation::new);
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.PROPERTY)
  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Confirmation implements Serializable {
    private static final long serialVersionUID = 3590333285305219765L;

    @XmlElement
    private boolean skip = false;

    @XmlElement
    private String note = null;

    boolean isNotificationSendEnabled() {
      return !skip;
    }
  }
}
