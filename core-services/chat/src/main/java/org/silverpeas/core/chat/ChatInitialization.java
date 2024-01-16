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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.chat;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.util.security.SecuritySettings;

/**
 * Initializes the resources required by the Chat to work correctly.
 * @author mmoquillon
 */
@Service
public class ChatInitialization implements Initialization {

  @Override
  public void init() throws Exception {
    final ChatSettings chatSettings = ChatServer.getChatSettings();
    if (chatSettings.isChatEnabled()) {
      final SecuritySettings.Registration registration = SecuritySettings.registration();
      final String xmppService = chatSettings.getChatServerUrl();
      if (!xmppService.isEmpty()) {
        registration.registerDefaultSourceInCSP(xmppService);
        registration.registerDomainInCORS(xmppService);
      }
      final String fileTransferService = chatSettings.getFileTransferServiceUrl();
      if (!fileTransferService.equals(xmppService)) {
        registration.registerDefaultSourceInCSP(fileTransferService);
        registration.registerDomainInCORS(fileTransferService);
      }
    }
  }
}
  