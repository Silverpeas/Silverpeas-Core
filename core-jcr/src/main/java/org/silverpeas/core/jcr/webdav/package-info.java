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

/**
 * It provides the classes with which the JCR can be accessed through the WebDAV protocol. The
 * WebDAV access to the documents in the JCR is provided by Silverpeas to allow their edition with
 * an office editor supporting the WebDAV protocol. For doing, the WebDAV bridge with the JCR is
 * provided by the JackRabbit WebDAV library, which is JCR implementation agnostic, and customized
 * by Silverpeas to ensure the user behind the WebDAV access has the right to access and to edit the
 * document in the JCR. To allow the edition of a document in the JCR by WebDAV with an external
 * editor, Silverpeas uses the custom web protocol mechanism for which a native program to install
 * in the host of the client is provided by Silverpeas. The WebDAV URL of document sent back to the
 * browser of the user is crafted on this custom protocol so that the browser delegates the opening
 * of the document by WebDAV to the native program on the host of the user. This native program aims
 * to look for an office editor supporting the WebDAV protocol and then to launch it with the
 * correct WebDAV URL of the document.
 * @implNote It depends on the Jackrabbit server library.
 * @author mmoquillon
 */
package org.silverpeas.core.jcr.webdav;