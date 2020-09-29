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

package org.silverpeas.core.test;

import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.chat.servers.DefaultChatServer;

/**
 * The build of a war archive for the chat service.
 * @author mmoquillon
 */
public class WarBuilder4Chat extends BasicWarBuilder {

  /**
   * Constructs a war builder for the specified test class. It will load all the resources in the
   * same packages of the specified test class.
   * @param classOfTest the class of the test for which a war archive will be build.
   */
  protected <U> WarBuilder4Chat(final Class<U> classOfTest) {
    super(classOfTest);
  }

  /**
   * Constructs an instance of the war archive builder for the specified test class.
   * All the dependencies and resources required by the Chat service are automatically set.
   * @param test the test class for which a war will be built. Any resources located in the same
   * package of the test will be loaded into the war.
   * @param <T> the type of the test.
   * @return a builder of the war archive with the Chat service embedded within it.
   */
  public static <T> WarBuilder4Chat onWarForTestClass(Class<T> test) {
    return (WarBuilder4Chat) new WarBuilder4Chat(test)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core")
        //.createMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud")
        .testFocusedOn(war -> {
          war.addPackages(false, "org.silverpeas.core.chat")
              .addClasses(ChatServer.class, DefaultChatServer.class)
              .addAsResource("org/silverpeas/chat/settings/chat.properties");
        });
  }
}
  