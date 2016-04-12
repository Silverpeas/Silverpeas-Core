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
package org.silverpeas.core.notification.user.builder;

import java.util.HashMap;
import java.util.Map;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.notification.user.DefaultUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.util.Link;
import org.apache.commons.lang3.StringUtils;

import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;

/**
 * @author Yohann Chastagnier
 * @param <T> the type of resource concerned by the notification.
 */
public abstract class AbstractTemplateUserNotificationBuilder<T> extends
    AbstractResourceUserNotificationBuilder<T> {

  /**
   * The silverpeas templates indexed by languages
   */
  private final Map<String, SilverpeasTemplate> templates
      = new HashMap<>();

  /**
   * Default constructor
   * @param resource
   */
  public AbstractTemplateUserNotificationBuilder(final T resource) {
    super(resource, null, null);
  }

  /**
   * Default constructor
   * @param resource
   * @param fileName
   */
  public AbstractTemplateUserNotificationBuilder(final T resource, final String fileName) {
    super(resource, null, fileName);
  }

  /**
   * Default constructor
   * @param resource
   * @param title
   * @param fileName
   */
  public AbstractTemplateUserNotificationBuilder(final T resource, final String title,
      final String fileName) {
    super(resource, title, fileName);
  }

  protected abstract String getBundleSubjectKey();

  @Override
  protected String getTitle() {
    if (StringUtils.isBlank(getBundleSubjectKey())) {
      return super.getTitle();
    }
    return getBundle().getString(getBundleSubjectKey());
  }

  /**
   * Gets the fileName of StringTemplate
   * @return
   */
  protected String getFileName() {
    // getContent() returns the fileName
    return getContent();
  }

  @Override
  protected UserNotification createNotification() {
    return new DefaultUserNotification(getTitle(), templates, getFileName());
  }

  @Override
  protected final void performBuild(final T resource) {
    templates.clear();
    perform(resource);
    SilverpeasTemplate template;
    final NotificationResourceData nRDBase = initializeNotificationResourceData();
    NotificationResourceData notificationResourceData;
    for (final String curLanguage : DisplayI18NHelper.getLanguages()) {
      //set link url and link label
      String linkUrl = getResourceURL(resource);
      String linkLabel = "";
      if (getContributionAccessLinkLabelBundleKey() != null) {
        linkLabel = getBundle(curLanguage).getString(getContributionAccessLinkLabelBundleKey());
      }
      Link link = new Link(linkUrl, linkLabel);
      getNotificationMetaData().setLink(link, curLanguage);

      template = createTemplate();
      template.setAttribute("silverpeasURL", linkUrl);
      templates.put(curLanguage, template);

      performTemplateData(curLanguage, resource, template);
      notificationResourceData = nRDBase.clone();
      performNotificationResource(curLanguage, resource, notificationResourceData);
      getNotificationMetaData().setNotificationResourceData(curLanguage, notificationResourceData);
    }
  }

  @Override
  protected final void performNotificationResource(final T resource) {
    // Nothing to do
  }

  @Override
  protected final void performNotificationResource(final T resource,
      final NotificationResourceData notificationResourceData) {
    // Nothing to do
  }

  /**
   * Creates the template bases
   * @return
   */
  protected SilverpeasTemplate createTemplate() {
    SilverpeasTemplate template;
    if (OrganizationControllerProvider.getOrganisationController()
        .isComponentExist(getComponentInstanceId()) || OrganizationControllerProvider.
        getOrganisationController()
        .isToolAvailable(getComponentInstanceId())) {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents(getTemplatePath());
    } else {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore(getTemplatePath());
    }
    return template;
  }

  /**
   * Performing notification data from a given language
   * @param resource
   */
  protected void perform(final T resource) {
    // Default : nothing to do
  }

  /**
   * Performing notification data from a given language
   * @param language
   * @param resource
   * @param template
   */
  protected abstract void performTemplateData(String language, T resource,
      SilverpeasTemplate template);

  /**
   * Builds the notification resource data container from a given language. Don't forget to fill
   * resourceId,
   * resourceType, resourceName, resourceDescription (optional), resourceLocation (optional). If
   * ResourceLocation is
   * empty , it will be filled by the NotificationManager with the given componentInstanceId of
   * NotificationMetaData
   * @param language
   * @param resource
   * @param notificationResourceData
   * @return
   */
  protected abstract void performNotificationResource(String language, T resource,
      NotificationResourceData notificationResourceData);

  /**
   * Gets the string template path
   * @return
   */
  protected abstract String getTemplatePath();

  /**
   * Gets the string bundle key for contribution access link
   * @return
   */
  protected String getContributionAccessLinkLabelBundleKey() {
    return null;
  }
}
