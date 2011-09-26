/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Provides an API for pusblishing and for receiving notifications about some topics. A topic refers
 * the nature of information that will be carried by the notifications; for example, a topic can
 * be related to the actions (add and removal) on the comments over the contents managed in Silverpeas.
 * A notification is a message sent by a publisher over a specific topic. Objects interested by such
 * messages have to subscribe for notifications about this topic; theses are
 * subscribers that will be then informed of the incoming notifications through a callback with the
 * message passed as parameter.
 * 
 * The API can also be used for posting messages to several end-points (one to many user
 * communication for example).
 * The API decouples the client from the specific messaging system (JMS, AMQP, ...) used to provide
 * the notification mechanism, so that the underlying MOM system can be changed without any impact.
 * The implementation of this API is a MOM technology-dependent and it is injected through the
 * IoC container within which the Silverpeas components are deployed.
 */
package com.silverpeas.notification;
