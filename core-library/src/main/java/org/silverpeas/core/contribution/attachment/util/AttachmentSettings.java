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
package org.silverpeas.core.contribution.attachment.util;

import org.silverpeas.core.admin.component.model.SilverpeasComponent;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Handled the settings around the attachments.
 * @author Yohann Chastagnier
 */
public class AttachmentSettings {

  public static final int DEFAULT_REORDER_START = 1;
  public static final int YOUNGEST_TO_OLDEST_MANUAL_REORDER_START = 200000;
  public static final int YOUNGEST_TO_OLDEST_MANUAL_REORDER_THRESHOLD = 100000;

  private static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.Attachment");

  private AttachmentSettings() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Gets the delay in percent after which an alert MUST be sent to the owner in order to remind
   * it to release the reserved file.
   * @return an integer representing a percentage, -1 means no value.
   */
  public static int getDelayInPercentAfterWhichReservedFileAlertMustBeSent() {
    return settings.getInteger("DelayReservedFile", -1);
  }

  /**
   * Indicates the order the methods in charge of returning list of documents must apply.
   * @return false to list from oldest to youngest, true to list from the youngest to the oldest.
   */
  public static boolean listFromYoungestToOldestAdd() {
    final int order = settings.getInteger("attachment.list.order", 1);
    return order < 0;
  }

  /**
   * Indicates if metadata of a file, if any, can be used to fill data (title & description) of an
   * attachment. (defined in properties by attachment.data.fromMetadata)
   * @return true if they must be used, false otherwise.
   */
  public static boolean isUseFileMetadataForAttachmentDataEnabled() {
    return settings.getBoolean("attachment.data.fromMetadata", false);
  }

  /**
   * Gets the stream of component name for which the the displaying as content is enabled.
   * @return {@link Stream} of component name as {@link String}.
   */
  public static Stream<String> displayableAsContentComponentNames() {
    return Stream
        .of(settings.getString("attachmentsAsContent.component.names", StringUtil.EMPTY).split("[ ,;]"))
        .map(String::trim);
  }

  /**
   * Gets the default value of the JCR flag which indicates if a document is displayable as content.
   * @return true of displayable as content, false otherwise.
   */
  public static boolean defaultValueOfDisplayableAsContentBehavior() {
    return settings.getBoolean("attachmentsAsContent.default.value", true);
  }

  /**
   * Indicates if the displaying as content is enabled for a component instance represented by
   * the given identifier.
   * @param componentInstanceId identifier of a component instance.
   * @return true if activated, false otherwise.
   */
  public static boolean isDisplayableAsContentForComponentInstanceId(
      final String componentInstanceId) {
    return displayableAsContentComponentNames()
        .map(c -> {
          final Optional<SilverpeasComponent> component = SilverpeasComponent.getByInstanceId(componentInstanceId);
          return component.isPresent() && component.get().getName().equalsIgnoreCase(c.trim());
        })
        .filter(b -> b)
        .findFirst()
        .orElse(false);
  }

  /**
   * Gets the default value of the JCR flag which indicates if a document is editable simultaneously.
   * @return true if editable simultaneously, false otherwise.
   */
  public static boolean defaultValueOfEditableSimultaneously() {
    return settings.getBoolean("attachment.onlineEditing.simultaneously.default", true);
  }
}
