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
package org.silverpeas.notification.message;

import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.ResourceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.cache.service.CacheServiceFactory;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


/**
 * User: Yohann Chastagnier
 * Date: 08/11/13
 */
public class MessageManagerTest {

  @Before
  public void setup() {
    MessageManager.initialize();
  }

  @After
  public void tearDown() {
    MessageManager.clear(MessageManager.getRegistredKey());
    MessageManager.destroy();
  }

  /**
   * Initialize the manager in order to be used everywhere in treatments.
   */
  public void initialize() {
    String registredKey =
        CacheServiceFactory.getApplicationCacheService().add(new MessageContainer());
    CacheServiceFactory.getRequestCacheService().put(MessageManager.class, registredKey);
    assertThat(getMessageContainer().getMessages(), emptyIterable());
  }

  /**
   * Clear out the thread cache the registred key referenced.
   */
  public void destroy() {
    CacheServiceFactory.getRequestCacheService().remove(MessageManager.class);
  }

  @Test
  public void getLanguage() {
    assertThat(MessageManager.getLanguage(), is(I18NHelper.defaultLanguage));
    String otherLanguage = "otherLanguage";
    MessageManager.setLanguage(otherLanguage);
    assertThat(I18NHelper.defaultLanguage, not(is(otherLanguage)));
    assertThat(MessageManager.getLanguage(), is(otherLanguage));
  }

  @Test
  public void getResourceLocator() {
    ResourceLocator locator =
        MessageManager.getResourceLocator("org.silverpeas.util.multilang.i18n");
    ResourceLocator utilLocator =
        MessageManager.getResourceLocator("org.silverpeas.util.multilang.util");
    MessageManager.setLanguage("otherLanguage");
    ResourceLocator locatorOtherLanguage =
        MessageManager.getResourceLocator("org.silverpeas.util.multilang.i18n");
    ResourceLocator utilLocatorOtherLanguage =
        MessageManager.getResourceLocator("org.silverpeas.util.multilang.util");

    assertThat(locator, notNullValue());
    assertThat(locatorOtherLanguage, notNullValue());
    assertThat(utilLocator, notNullValue());
    assertThat(utilLocatorOtherLanguage, notNullValue());

    assertThat(locator, not(sameInstance(locatorOtherLanguage)));
    assertThat(locator, not(sameInstance(utilLocator)));
    assertThat(locator, not(sameInstance(utilLocatorOtherLanguage)));
    assertThat(locatorOtherLanguage, not(sameInstance(utilLocator)));
    assertThat(locatorOtherLanguage, not(sameInstance(utilLocatorOtherLanguage)));
    assertThat(utilLocator, not(sameInstance(utilLocatorOtherLanguage)));

    MessageManager.setLanguage(I18NHelper.defaultLanguage);
    assertThat(locator,
        sameInstance(MessageManager.getResourceLocator("org.silverpeas.util.multilang.i18n")));
    assertThat(utilLocator,
        sameInstance(MessageManager.getResourceLocator("org.silverpeas.util.multilang.util")));
    MessageManager.setLanguage("otherLanguage");
    assertThat(locatorOtherLanguage,
        sameInstance(MessageManager.getResourceLocator("org.silverpeas.util.multilang.i18n")));
    assertThat(utilLocatorOtherLanguage,
        sameInstance(MessageManager.getResourceLocator("org.silverpeas.util.multilang.util")));


    MessageManager.setLanguage("en");
    ResourceLocator locatorEn =
        MessageManager.getResourceLocator("org.silverpeas.util.multilang.i18n");

    assertThat(locator.getString("language_fr"), is("Fran\u00e7ais"));
    assertThat(locatorEn.getString("language_fr"), is("French"));
  }

  @Test
  public void getResourceLocatorVerifyAfterTrash() {
    getResourceLocator();
    MessageManager.destroy();
    assertThat(MessageManager.getResourceLocator("org.silverpeas.util.multilang.i18n"),
        nullValue());
    assertThat(MessageManager.getResourceLocator("org.silverpeas.util.multilang.util"),
        nullValue());
    MessageManager.setLanguage("otherLanguage");
    assertThat(MessageManager.getResourceLocator("org.silverpeas.util.multilang.i18n"),
        nullValue());
    assertThat(MessageManager.getResourceLocator("org.silverpeas.util.multilang.util"),
        nullValue());
  }

  @Test
  public void addError() {
    MessageManager.addError("errorMessage");
    assertThat(getMessageContainer().getMessages(), hasSize(1));
    Message test = getMessageContainer().getMessages().iterator().next();
    assertThat(test.getType(), is(MessageType.error));
    assertThat(test.getContent(), is("errorMessage"));
    assertThat(test.getDisplayLiveTime(), is(0L));
  }

  @Test
  public void addWarning() {
    MessageManager.addWarning("warningMessage");
    assertThat(getMessageContainer().getMessages(), hasSize(1));
    Message test = getMessageContainer().getMessages().iterator().next();
    assertThat(test.getType(), is(MessageType.warning));
    assertThat(test.getContent(), is("warningMessage"));
    assertThat(test.getDisplayLiveTime(), is(0L));
  }

  @Test
  public void addSuccess() {
    MessageManager.addSuccess("successMessage");
    assertThat(getMessageContainer().getMessages(), hasSize(1));
    Message test = getMessageContainer().getMessages().iterator().next();
    assertThat(test.getType(), is(MessageType.success));
    assertThat(test.getContent(), is("successMessage"));
    assertThat(test.getDisplayLiveTime(), is(5000L));
  }

  @Test
  public void addInfo() {
    MessageManager.addInfo("infosMessage");
    assertThat(getMessageContainer().getMessages(), hasSize(1));
    Message test = getMessageContainer().getMessages().iterator().next();
    assertThat(test.getType(), is(MessageType.info));
    assertThat(test.getContent(), is("infosMessage"));
    assertThat(test.getDisplayLiveTime(), is(5000L));
  }

  @Test
  public void addSeveralMessages() {
    MessageManager.addError("errorMessage");
    MessageManager.addWarning("warningMessage");
    MessageManager.addSuccess("successMessage");
    MessageManager.addInfo("infosMessage");
    assertThat(getMessageContainer().getMessages(), hasSize(4));
  }

  @Test
  public void addListener() {
    final List<Object> counterLanguage = new LinkedList<Object>();
    final List<Object> counterBefore = new LinkedList<Object>();
    final List<Object> counterAfter = new LinkedList<Object>();
    MessageManager.addListener(new MessageListener() {
      @Override
      public void beforeGetLanguage(final MessageContainer container) {
        counterLanguage.add(new Object());
      }

      @Override
      public void beforeAddMessage(final MessageContainer container, final Message message) {
        counterBefore.add(new Object());
      }

      @Override
      public void afterMessageAdded(final MessageContainer container, final Message message) {
        counterAfter.add(new Object());
      }
    });
    MessageManager.addInfo("infosMessage");
    assertThat(counterLanguage, hasSize(0));
    assertThat(counterBefore, hasSize(1));
    assertThat(counterAfter, hasSize(1));
    MessageManager.addInfo("infosMessage");
    MessageManager.addInfo("infosMessage");
    assertThat(counterLanguage, hasSize(0));
    assertThat(counterBefore, hasSize(3));
    assertThat(counterAfter, hasSize(3));
    // When several messages are identical, only one is kept.
    assertThat(getMessageContainer().getMessages(), hasSize(1));
    MessageManager.addInfo("otherInfosMessage");
    assertThat(counterLanguage, hasSize(0));
    assertThat(counterBefore, hasSize(4));
    assertThat(counterAfter, hasSize(4));
    // When several messages are identical, only one is kept.
    assertThat(getMessageContainer().getMessages(), hasSize(2));

    MessageManager.getLanguage();
    assertThat(counterLanguage, hasSize(1));
  }

  private MessageContainer getMessageContainer() {
    return MessageManager.getMessageContainer(MessageManager.getRegistredKey());
  }
}
