/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.permalinks.repository;

import javax.inject.Inject;

import org.silverpeas.permalinks.model.DocumentPermalink;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
@ContextConfiguration(locations = {"classpath:/spring-permalinks-embbed-datasource.xml",
  "classpath:/spring-permalinks.xml"})
@Transactional
@DirtiesContext
public class DocumentPermalinkRepositoryTest {

  @Inject
  DocumentPermalinkRepository repository;

  public DocumentPermalinkRepositoryTest() {
  }

  @Test
  public void testFindById() {
    DocumentPermalink permalink = repository.findOne(1);
    assertThat(permalink, is(notNullValue()));
    assertThat(permalink.getId(), is(1));
    assertThat(permalink.getUuid(), is("ilovesilverpeas"));
  }

}
