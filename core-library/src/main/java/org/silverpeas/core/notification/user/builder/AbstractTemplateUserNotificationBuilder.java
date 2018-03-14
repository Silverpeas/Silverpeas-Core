/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
package org.silverpeas.core.notification.user.builder;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.notification.user.DefaultUserNotification;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasStringTemplateUtil;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.Pair;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.core.date.TemporalConverter.asLocalDate;
import static org.silverpeas.core.date.TemporalFormatter.toLocalized;

/**
 * @author Yohann Chastagnier
 * @param <T> the type of resource concerned by the notification.
 */
public abstract class AbstractTemplateUserNotificationBuilder<T> extends
    AbstractResourceUserNotificationBuilder<T> {

  private final Map<String, SilverpeasTemplate> templates = new HashMap<>();
  private Pair<Boolean, String> rootTemplatePath;

  /**
   * Default constructor
   * @param resource the resource which is the object of the notification.
   */
  public AbstractTemplateUserNotificationBuilder(final T resource) {
    super(resource);
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
   * @return the StringTemplate filename
   */
  protected abstract String getTemplateFileName();

  @Override
  protected UserNotification createNotification() {
    return new DefaultUserNotification(getTitle(), templates, getTemplateFileName());
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
   * @return a {@link Pair} instance which indicates first the StringTemplate repository as a
   * boolean, true if it is the components one, false if it is the core one. It indicates secondly
   * the template path into the root as a String.
   */
  private Pair<Boolean, String> getRootTemplatePath() {
    if (rootTemplatePath == null) {
      final Mutable<Boolean> componentRoot = Mutable.of(false);
      final Mutable<String> templatePath = Mutable.of(getTemplatePath());
      Optional<SilverpeasComponentInstance> instance =
          SilverpeasComponentInstance.getById(getComponentInstanceId());
      if (this instanceof FallbackToCoreTemplatePathBehavior) {
        instance.map(i -> i.getName() + "/" + templatePath.get()).filter(
            p -> SilverpeasStringTemplateUtil.isComponentTemplateExist(p, getTemplateFileName()))
            .ifPresent(p -> {
              componentRoot.set(true);
              templatePath.set(p);
            });
      } else {
        componentRoot.set(instance.isPresent() || OrganizationControllerProvider.
            getOrganisationController().isToolAvailable(getComponentInstanceId()));
      }
      rootTemplatePath = Pair.of(componentRoot.get(), templatePath.get());
    }
    return rootTemplatePath;
  }

  private SilverpeasTemplate createTemplate() {
    final Boolean fromComponent = getRootTemplatePath().getFirst();
    final String templatePath = getRootTemplatePath().getSecond();
    if (fromComponent) {
      return SilverpeasTemplateFactory.createSilverpeasTemplateOnComponents(templatePath);
    } else {
      return SilverpeasTemplateFactory.createSilverpeasTemplateOnCore(templatePath);
    }
  }

  protected void perform(final T resource) {
    // Default : nothing to do
  }

  protected abstract void performTemplateData(String language, T resource,
      SilverpeasTemplate template);

  /**
   * Builds the notification resource data container from a given language. Don't forget to fill
   * resourceId, resourceType, resourceName, resourceDescription (optional), resourceLocation
   * (optional). If ResourceLocation is empty , it will be filled by the NotificationManager with
   * the given componentInstanceId of NotificationMetaData
   * @param language the language in ISO-639-2
   * @param resource the resource concerned by the notification
   * @param notificationResourceData data about the notification
   */
  protected abstract void performNotificationResource(String language, T resource,
      NotificationResourceData notificationResourceData);

  /**
   * Gets the string template path
   * @return the StringTemplate file path
   */
  protected abstract String getTemplatePath();

  /**
   * Gets the string bundle key for contribution access link
   * @return the string bundle key.
   */
  protected String getContributionAccessLinkLabelBundleKey() {
    return null;
  }

  /**
   * Handles the date formats into notification building context.
   */
  public class NotificationTemporal {
    final Temporal temporal;
    final ZoneId zoneIdReference;
    final String language;

    public NotificationTemporal(final Temporal temporal, final ZoneId zoneIdReference,
        final String language) {
      this.temporal = temporal;
      this.zoneIdReference = zoneIdReference;
      this.language = language;
    }

    /**
     * Indicates if the date exist.
     * @return true if exists, false otherwise.
     */
    public boolean isDateExisting() {
      return temporal != null && temporal.isSupported(ChronoUnit.DAYS);
    }

    /**
     * Gets the date of the day.
     * @return a string.
     */
    public String getDayDate() {
      return toLocalized(asLocalDate(temporal), language);
    }

    /**
     * Gets the date with time (if the temporal has time data otherwise only date is returned).
     * @return a string.
     */
    public String getDate() {
      return toLocalized(temporal, language);
    }

    /**
     * Gets the date with hour data if the temporal has hour data.<br/>
     * If the zone id is not the same of the platform, the zone id is also filled.
     * @return a string.
     */
    public String getFullDate() {
      return toLocalized(temporal, zoneIdReference, language);
    }

    /**
     * Indicates if the underlying temporal has time data.
     * @return true if the time exists in the temporal, false otherwise.
     */
    public boolean isTimeExisting() {
      return temporal.isSupported(ChronoUnit.HOURS);
    }

    /**
     * Gets the time data if the temporal supports such a chronology unit.
     * @return a string.
     */
    public String getDayTime() {
      String hour = "";
      if (isTimeExisting()) {
        hour = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.forLanguageTag(language))
            .format(temporal);
      }
      return hour;
    }
  }
}
