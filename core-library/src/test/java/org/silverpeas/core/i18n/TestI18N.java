/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.i18n;

/**
 * @author mmoquillon
 */
public class TestI18N extends BeanTranslation {
  private static final long serialVersionUID = 353607407808930532L;

  private String value;
  private BeanTranslationTest.ObjectClonable objectClonable;
  private BeanTranslationTest.ObjectNotClonable objectNotClonable;

  protected TestI18N() {
    // for copy
  }

  public TestI18N(final TestI18N translation) {
    super(translation);
  }

  public TestI18N(String lang, String name, String description, String value,
      BeanTranslationTest.ObjectClonable objectClonable,
      BeanTranslationTest.ObjectNotClonable objectNotClonable) {
    super(lang, name, description);
    this.value = value;
    this.objectClonable = objectClonable;
    this.objectNotClonable = objectNotClonable;
  }

  public String getValue() {
    return value;
  }

  public BeanTranslationTest.ObjectClonable getObjectClonable() {
    return objectClonable;
  }

  public BeanTranslationTest.ObjectNotClonable getObjectNotClonable() {
    return objectNotClonable;
  }
}
  