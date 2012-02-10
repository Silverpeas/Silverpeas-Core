/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.socialnetwork.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.silverpeas.socialnetwork.model.ExternalAccount;
import com.silverpeas.socialnetwork.model.SocialNetworkID;
import com.silverpeas.socialnetwork.service.SocialNetworkService;

/**
 * @author lbertin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-socialnetwork.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class SocialNetworkServiceLinkedInTest {

	@Inject
	private SocialNetworkService service = null;
	@Inject
	private DataSource dataSource;

	public SocialNetworkServiceLinkedInTest() {
	}

	@Before
	public void generalSetUp() throws Exception {
		ReplacementDataSet dataSet = new ReplacementDataSet(
				new FlatXmlDataSet(
						SocialNetworkServiceLinkedInTest.class
								.getClassLoader()
								.getResourceAsStream(
										"com/silverpeas/socialnetwork/dao/socialnetwork-dataset.xml")));
		dataSet.addReplacementObject("[NULL]", null);
		IDatabaseConnection connection = new DatabaseConnection(
				dataSource.getConnection());
		DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
	}

	@Test
	@Transactional
	public void testReadByPrimaryKey() throws Exception {
		ExternalAccount account = service.getExternalAccount(
				SocialNetworkID.LINKEDIN, "1234");
		assertThat(account.getSilverpeasUserId(), is("11"));
	}

}
