/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.test;

import javax.annotation.Resource;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.sql.DataSource;
import java.util.Set;

/**
 * A convenient provider of the data source used in the integration tests.
 * @author mmoquillon
 */
public class DataSourceProvider {

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;

  private static DataSourceProvider getInstance() {
    BeanManager beanManager = CDI.current().getBeanManager();
    Bean<DataSourceProvider> bean =
        beanManager.resolve((Set) beanManager.getBeans(DataSourceProvider.class));
    if (bean == null) {
      throw new IllegalStateException(
          "Cannot find an instance of type " + DataSourceProvider.class.getName());
    }
    CreationalContext<DataSourceProvider> ctx = beanManager.createCreationalContext(bean);
    return (DataSourceProvider) beanManager.getReference(bean, DataSourceProvider.class, ctx);
  }

  @Produces
  public static final DataSource getDataSource() {
    return getInstance().dataSource;
  }
}
