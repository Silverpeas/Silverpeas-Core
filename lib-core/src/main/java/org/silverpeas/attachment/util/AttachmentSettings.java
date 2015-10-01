/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.attachment.util;

import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.ResourceLocator;

import javax.inject.Inject;

/**
 * The aim of this class is to expose at the rest of application the settings around attachments.
 * Its implementation allows to manage the attachment parameters easily into unit tests. It assures
 * also less problems around setting regressions.
 * @author: Yohann Chastagnier
 */
public class AttachmentSettings {

  private static final AttachmentSettings instance = new AttachmentSettings();

  /**
   * Indicates if obsolete wysiwyg content must be ignored.
   * This method returns always false if {@link #getObsoleteWysiwygContentIgnoredMark()} returns
   * a not defined value.
   * False by default (attachment.wysiwyg.content.inspection.obsolete.ignore).
   * @return true if wysiwyg content must be ignored, false otherwise.
   */
  public static boolean isObsoleteWysiwygContentIgnored() {
    return getInstance().getSettingBundle()
        .getBoolean("attachment.wysiwyg.content.inspection.obsolete.ignore", false) &&
        StringUtil.isDefined(getObsoleteWysiwygContentIgnoredMark());
  }

  /**
   * Gets the strict mark to fill at start of a wysiwyg file to mark it as ignored.
   * If the mark exists into the file, but not at beginning, it is not taken into account.
   * (defined in properties by attachment.wysiwyg.content.inspection.obsolete.ignore.mark)
   * @return the mark if defined, empty string otherwise.
   */
  public static String getObsoleteWysiwygContentIgnoredMark() {
    return getInstance().getSettingBundle()
        .getString("attachment.wysiwyg.content.inspection.obsolete.ignore.mark", "");
  }

  /**
   * Gets the provider.
   * @return
   */
  private SettingBundle getSettingBundle() {
    return ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.Attachment");
  }

  /**
   * @return a AttachmentSettings instance.
   */
  public static AttachmentSettings getInstance() {
    return instance;
  }

}
