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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication.password.rule;

import org.junit.jupiter.api.AfterEach;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.ResourceLocator;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;

/**
 * User: Yohann Chastagnier
 * Date: 08/01/13
 */
@EnableSilverTestEnv
public abstract class AbstractPasswordRuleTest<T extends PasswordRule> {
  protected final static int NB_LOOP = 1000;

  @AfterEach
  public void afterTest() {
    AbstractPasswordRule.settings =
        ResourceLocator.getSettingBundle("org.silverpeas.password.settings.password");
  }

  protected void setDefinedSettings() {
    AbstractPasswordRule.settings =
        ResourceLocator.getSettingBundle("org.silverpeas.password.settings.passwordDefined");
  }

  protected void setDefinedMoreThanOneSettings() {
    AbstractPasswordRule.settings =
        ResourceLocator.getSettingBundle("org.silverpeas.password.settings.passwordMoreThanOneDefined");
  }

  protected void setCombinationDefinedMoreThanOneSettings() {
    AbstractPasswordRule.settings =
        ResourceLocator.getSettingBundle("org.silverpeas.password.settings.passwordCombinationDefined");
  }

  protected void setNotDefinedSettings() {
    AbstractPasswordRule.settings =
        ResourceLocator.getSettingBundle("org.silverpeas.password.settings.passwordNotDefined");
  }

  protected void setBadDefinedSettings() {
    AbstractPasswordRule.settings =
        ResourceLocator.getSettingBundle("org.silverpeas.password.settings.passwordBadDefined");
  }

  protected void setNotRequiredSettings() {
    AbstractPasswordRule.settings =
        ResourceLocator.getSettingBundle("org.silverpeas.password.settings.passwordNotRequired");
  }

  public abstract void testDefinedPropertyValues();

  public abstract void testDefinedMoreThanOnePropertyValues();

  public abstract void testCombinationDefinedMoreThanOnePropertyValues();

  public abstract void testNotDefinedPropertyValues();

  public abstract void testBadDefinedPropertyValues();

  public abstract void testNotRequiredPropertyValues();

  protected T newRuleInstanceForTest() {
    try {
      Constructor<T> constructor = getRuleClass().getConstructor();
      return constructor.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Class<T> getRuleClass() {
    final ParameterizedType paramType = (ParameterizedType) getClass().getGenericSuperclass();
    return (Class<T>) paramType.getActualTypeArguments()[0];
  }
}
