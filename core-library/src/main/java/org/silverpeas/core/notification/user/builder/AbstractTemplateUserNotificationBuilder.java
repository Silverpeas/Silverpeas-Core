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
package org.silverpeas.core.notification.user.builder;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.notification.user.DefaultUserNotification;
import org.silverpeas.core.notification.user.FallbackToCoreTemplatePathBehavior;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasStringTemplateUtil;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.Pair;

import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.silverpeas.core.date.TemporalConverter.asLocalDate;
import static org.silverpeas.core.date.TemporalFormatter.toLocalized;
import static org.silverpeas.core.date.TemporalFormatter.toLocalizedTime;

/**
 * @author Yohann Chastagnier
 * @param <T> the type of resource concerned by the notification.
 */
public abstract class AbstractTemplateUserNotificationBuilder<T> extends
    AbstractResourceUserNotificationBuilder<T> {

  /**
   * The property in the settings from which the subject of the notification will be set. This
   * key is to set a custom subject peculiar to a given component. If no such property exists or
   * if this property isn't valued, then the default notification subject will be taken (it is
   * defined by the property GML.st.notification.subject).
   */
  protected static final String CUSTOM_NOTIFICATION_SUBJECT = "custom.st.notification.subject";
  protected static final String DEFAULT_NOTIFICATION_SUBJECT = "GML.st.notification.subject";
  private final Map<String, SilverpeasTemplate> templates = new HashMap<>();
  private Pair<Boolean, String> rootTemplatePath;

  /**
   * Default constructor
   * @param resource the resource which is the object of the notification.
   */
  public AbstractTemplateUserNotificationBuilder(final T resource) {
    super(resource);
  }

  /**
   * The name of the property in the bundle returned by the {@link #getBundle()} method and that
   * specifies a custom subject for the notifications built by this builder. By Default the
   * custom subject is defined by the property <code>custom.st.notification.subject</code> in the
   * bundle returned by the {@link #getBundle()} method. So this method doesn't require to be
   * overridden unless to give a different property name; for example, in case there is a different
   * subject for several kinds of notifications in a given Silverpeas component (and hence several
   * notification builders).
   * @return the name of the property in the {@link #getBundle()} bundle that specifies the subject
   * to use in the notifications built by this builder.
   */
  protected String getBundleSubjectKey() {
    return CUSTOM_NOTIFICATION_SUBJECT;
  }

  /**
   * The title is by default defined by the property <code>GML.st.notification.subject</code> in
   * the Silverpeas's general localization bundle. The property is valued by a StringTemplate
   * pattern, so that information about the resource concerned by the notification can be passed.
   * <p>
   * It can be overridden by specifying a another property
   * in the bundle returned by {@link #getBundle()} and under the name given by
   * {@link #getBundleSubjectKey()}. By this way, each component in Silverpeas has a way to
   * customize the title of the notifications for the resources handled by itself.
   * </p>
   * <p>
   *  This method delegates its call to the {@link #getTitle(String)} method with
   *  {@link I18NHelper#defaultLanguage} as locale. So, to specify a custom implementation of this
   *  method, please override instead the {@link #getTitle(String)} method.
   * </p>
   * @return the title of the notification. By default, the title is specify globally for all
   * notifications by the <code>GML.st.notification.subject</code> property.
   */
  @Override
  protected final String getTitle() {
    return getTitle(I18NHelper.defaultLanguage);
  }

  /**
   * Gets the title of the notification to build explicitly in the specified language from the
   * bundle returned by the {@link #getBundle()} method. This method can be overridden to specify
   * another implementation.
   * @see #getTitle()
   * @param language the ISO-631 code of a language.
   * @return the title of the notification. By default, the title is specify globally for all
   * notifications by the <code>GML.st.notification.subject</code> property.
   */
  protected String getTitle(final String language) {
    final String subjectKey = getBundleSubjectKey();
    final String subject;
    if (StringUtils.isBlank(subjectKey) || !getBundle().containsKey(subjectKey)) {
      subject = getBundle(language).getString(DEFAULT_NOTIFICATION_SUBJECT);
    } else {
      subject = getBundle(language).getString(subjectKey);
    }
    return subject;
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
    NotificationResourceData resourceData;
    for (final String curLanguage : DisplayI18NHelper.getLanguages()) {
      //set link url and link label
      final String linkUrl;
      String linkLabel = "";
      if (getAction() == NotifAction.DELETE) {
        getNotificationMetaData().setLink(Link.EMPTY_LINK, curLanguage);
        linkUrl = null;
      } else {
        linkUrl = getResourceURL(resource);
        if (getContributionAccessLinkLabelBundleKey() != null) {
          linkLabel = getBundle(curLanguage).getString(getContributionAccessLinkLabelBundleKey());
        }
        final Link link = new Link(linkUrl, linkLabel);
        getNotificationMetaData().setLink(link, curLanguage);
      }

      template = createTemplate();
      template.setAttribute("silverpeasURL", linkUrl);
      template.setAttribute("resource", resource);
      templates.put(curLanguage, template);

      performTemplateData(curLanguage, resource, template);
      resourceData = nRDBase.clone();
      resourceData.setCurrentLanguage(curLanguage);
      resourceData.setLinkLabel(linkLabel);
      performNotificationResource(curLanguage, resource, resourceData);
      getNotificationMetaData().setNotificationResourceData(curLanguage, resourceData);
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
        instance
            .map(i -> i.getName() + "/" + templatePath.get())
            .filter(p -> SilverpeasStringTemplateUtil.isComponentTemplateExist(p, getTemplateFileName()))
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
        hour = toLocalizedTime(temporal, language);
      }
      return hour;
    }

    /**
     * Gets the time data if the temporal supports such a chronology unit.
     * @return a string.
     */
    public String getZonedDayTime() {
      String hour = "";
      if (isTimeExisting()) {
        hour = toLocalizedTime(temporal, zoneIdReference, language);
      }
      return hour;
    }
  }
}
