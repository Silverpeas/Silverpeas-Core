/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.webapi.admin.tools;

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.web.look.LookHelper;

/**
 * @author Yohann Chastagnier
 */
public class ToolDelegate {

  private final String language;
  private final LookHelper lookHelper;

  /**
   * @return all tools contained in Silverpeas
   */
  public List<AbstractTool> getAllTools() {
    final List<AbstractTool> tools = new ArrayList<>();
    tools.add(new AgendaTool(language, lookHelper));
    tools.add(new TodoTool(language, lookHelper));
    tools.add(new NotificationTool(language, lookHelper));
    tools.add(new SubscriptionTool(language, lookHelper));
    tools.add(new FavoriteRequestTool(language, lookHelper));
    tools.add(new FavoriteLinkTool(language, lookHelper));
    tools.add(new FileSharingTool(language, lookHelper));
    tools.add(new WebConnectionTool(language, lookHelper));
    tools.add(new ScheduleEventTool(language, lookHelper));
    tools.add(new MyProfileTool(language, lookHelper));
    tools.add(new FeedbackTool(language, lookHelper));
    tools.add(new ClipboardTool(language, lookHelper));
    return tools;
  }

  /**
   * Easy instantiation.
   * @param language the language
   * @param lookHelper
   * @return
   */
  public static ToolDelegate getInstance(final String language, final LookHelper lookHelper) {
    return new ToolDelegate(language, lookHelper);
  }

  /**
   * Hidden constructor.
   * @param language the language
   * @param lookHelper
   */
  private ToolDelegate(final String language, final LookHelper lookHelper) {
    this.language = language;
    this.lookHelper = lookHelper;
  }
}
