/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.chat;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Setting properties of the chat service. It loads the
 * <code>SILVERPEAS_HOME/properties/org/silverpeas/chat/settings/chat.properties</code> and provides
 * all the settings required by the Silverpeas Chat to work correctly.
 * @author mmoquillon
 */
@Bean
@Singleton
public class ChatSettings {

  private final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.chat.settings.chat");
  private final String xmppBaseUrl;

  /**
   * Gets an instance of the {@link ChatSettings} class.
   * @return a {@link ChatSettings} instance.
   */
  public static ChatSettings get() {
    return ServiceProvider.getSingleton(ChatSettings.class);
  }

  /**
   * Constructs a new object wrapping all the settings on the Silverpeas chat service.
   */
  public ChatSettings() {
    final String xmppServerUrl = settings.getString("chat.servers.xmpp", "").trim();
    if (xmppServerUrl.endsWith("/")) {
      xmppBaseUrl = xmppServerUrl.substring(0, xmppServerUrl.length() - 1);
    } else {
      xmppBaseUrl = xmppServerUrl;
    }
  }

  /**
   * Gets the URL at which the chat server is listening.
   * @return the chat server's URL.
   */
  public String getChatServerUrl() {
    return xmppBaseUrl;
  }

  /**
   * Gets the fully qualified hostname or the IP address of the ICE server listening for audio/video
   * communications.
   * @return the hostname or the IP address of the ICE server.
   */
  public String getICEServer() {
    return settings.getString("chat.servers.ice", "");
  }

  /**
   * Gets the fully qualified URL of the REST API of the XMPP server with which Silverpeas can use
   * to create and to remove XMPP users accounts.
   * @return the URL of the REST API published by the remote XMPP server.
   */
  public String getRestApiUrl() {
    final String rest = settings.getString("chat.xmpp.rest", "").trim();
    if (!rest.isEmpty()) {
      if (rest.startsWith("/")) {
        return xmppBaseUrl + rest;
      } else {
        return xmppBaseUrl + "/" + rest;
      }
    }
    return "";
  }

  /**
   * Gets the fully qualified URL of the BOSH service of the XMPP server which wich Silverpeas can
   * use to establish XMPP communications through the web.
   * @return the URL of the BOSH service provided by the remote XMPP server.
   */
  public String getBOSHServiceUrl() {
    final String bosh = settings.getString("chat.xmpp.httpBind", "").trim();
    if (!bosh.isEmpty()) {
      if (bosh.startsWith("/")) {
        return xmppBaseUrl + bosh;
      } else {
        return xmppBaseUrl + "/" + bosh;
      }
    }
    return "";
  }

  /**
   * Gets the base URL of the HTTP file transfer service of the XMPP server.
   * @return the URL of the file transfer service used by the XMPP server to transfer files between
   * users.
   */
  public String getFileTransferServiceUrl() {
    return settings.getString("chat.xmpp.httpUpload", xmppBaseUrl).trim();
  }

  /**
   * Gets the authorization token to use when communicating the REST API of the XMPP server.
   * @return the authorization token of the XMPP server's REST API.
   */
  public String getRestApiAuthToken() {
    return settings.getString("chat.xmpp.restKey", "");
  }

  /**
   * Gets the XMPP domain mapped with the specified Silverpeas domain. If the given domain isn't
   * mapped with any XMPP domain, then returns an empty string.
   * @param silverpeasDomainId the unique identifier of a domain in Silverpeas. Can be null or
   * empty in that case an empty string is returned.
   * @return the XMPP domain mapped with the specified Silverpeas domain. If no XMPP domain mapping
   * is defined for the given domain then returns an empty string.
   */
  public String getExplicitMappedXmppDomain(final String silverpeasDomainId) {
    String xmppDomain = "";
    if (StringUtil.isDefined(silverpeasDomainId)) {
      xmppDomain = settings.getString("chat.xmpp.domain." + silverpeasDomainId, "").trim();
    }
    return xmppDomain;
  }

  /**
   * Gets the XMPP domain to use for any Silverpeas domains that have no mapping rule to a XMPP
   * domain. If no such default XMPP domain is defined, then the Silverpeas domains with no mapping
   * rule won't be mapped to a XMPP domain and hence the users of those domains won't have an XMPP
   * account; the chat won't be enabled for them.
   * @return
   */
  public String getDefaultXmppDomain() {
    return settings.getString("chat.xmpp.domain.default", "").trim();
  }

  /**
   * Gets the XMPP domain in the chat server that is mapped to the specified Silverpeas domain. If
   * there is no explicit XMPP domain mapped to the given Silverpeas domain, then uses the default
   * XMPP domain set in the chat service settings. If no such default XMPP domain is set, then
   * returns an empty string.
   * @param silverpeasDomainId the unique identifier of a user domain in Silverpeas.
   * @return the XMPP domain that is explicitly or by default mapped to the given user domain in
   * Silverpeas. If no mapping exists with the given domain in the chat service settings, then
   * returns an empty string.
   */
  public String getMappedXmppDomain(final String silverpeasDomainId) {
    String xmppDomain = getExplicitMappedXmppDomain(silverpeasDomainId).trim();
    if (xmppDomain.isEmpty()) {
      return getDefaultXmppDomain();
    }
    return xmppDomain;
  }

  /**
   * Gets all the user groups in Silverpeas that are allowed to use the chat service, id est the
   * groups for which the chat is enabled. If no groups of users is defined, then an empty string
   * is returned and the chat is enabled for all the groups in Silverpeas (default behaviour).
   * @return an array of group identifiers or an empty array if all the groups are allowed.
   */
  public List<String> getAllowedUserGroups() {
    String groups = settings.getString("chat.xmpp.domain.groups", "");
    return groups.trim().isEmpty() ? Collections.emptyList() :
        Stream.of(groups.split(",")).map(String::trim).collect(Collectors.toList());
  }

  /**
   * Is the chat service enabled in Silverpeas?
   * @return true if the chat service is explicitly enabled and if the chat service properties are
   * set.
   */
  public boolean isChatEnabled() {
    boolean enabled = settings.getBoolean("chat.enable");
    final String rest = settings.getString("chat.xmpp.rest", "");
    final String bosh = settings.getString("chat.xmpp.httpBind", "");
    return enabled && !rest.isEmpty() && !bosh.isEmpty();
  }
}
  