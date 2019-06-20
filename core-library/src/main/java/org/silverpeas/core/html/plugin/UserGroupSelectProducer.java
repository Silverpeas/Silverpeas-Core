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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.html.plugin;

import org.silverpeas.core.html.SupportedWebPlugins;
import org.silverpeas.core.html.WebPlugin;

import java.util.Arrays;

/**
 * A producer of HTML data necessary to initialize the {@code UserGroupSelect} plugin on the
 * WEB browser. The plugin is available under {@link SupportedWebPlugins#LISTOFUSERSANDGROUPS}.
 * @author silveryocha
 */
public class UserGroupSelectProducer extends AbstractPluginInitializationProducer {

  public enum SelectionType {
    USER, GROUP, USER_GROUP
  }

  /**
   * Initializing the producer with the most important data.
   * @param id the identifier of the container which the plugin will fill.
   * @return the new instance of the producer.
   */
  public static UserGroupSelectProducer withContainerId(final String id) {
    return new UserGroupSelectProducer().addOption("rootContainerId", id);
  }

  public UserGroupSelectProducer withUserInputName(final String inputName) {
    return addOption("userInputName", inputName);
  }

  public UserGroupSelectProducer withGroupInputName(final String inputName) {
    return addOption("groupInputName", inputName);
  }

  public UserGroupSelectProducer selectionOf(final SelectionType selectionType) {
    return addOption("selectionType", selectionType.name());
  }

  public UserGroupSelectProducer multiple(final boolean multiple) {
    return addOption("multiple", multiple);
  }

  public UserGroupSelectProducer readOnly(final boolean readOnly) {
    return addOption("readOnly", readOnly);
  }

  public UserGroupSelectProducer hidden(final boolean hidden) {
    return addOption("hidden", hidden);
  }

  public UserGroupSelectProducer mandatory(final boolean mandatory) {
    return addOption("mandatory", mandatory);
  }

  public UserGroupSelectProducer withUserIds(final String... ids) {
    return addOption("initialUserIds", Arrays.stream(ids), false);
  }

  public UserGroupSelectProducer withGroupIds(final String... ids) {
    return addOption("initialGroupIds", Arrays.stream(ids), false);
  }

  public UserGroupSelectProducer filterOnComponentId(final String id) {
    return addOption("componentIdFilter", id);
  }

  public UserGroupSelectProducer filterOnRoles(final String... roles) {
    return addOption("roleFilter", Arrays.stream(roles), true);
  }

  public UserGroupSelectProducer withUserPanelButtonLabel(final String label) {
    return addOption("userPanelButtonLabel", label);
  }

  public UserGroupSelectProducer withRemoveButtonLabel(final String label) {
    return addOption("removeButtonLabel", label);
  }

  @Override
  protected String getDependencies() {
    return WebPlugin.get().getHtml(SupportedWebPlugins.LISTOFUSERSANDGROUPS, getUserLanguage())
        .toString();
  }
}
