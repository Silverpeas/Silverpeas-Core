/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.admin.domain.driver.sqldriver;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.repository.SQLDomainRepository;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.LibCoreWarBuilder;
import org.silverpeas.core.test.integration.DataSetTest;
import org.silverpeas.core.test.integration.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.util.SystemWrapper;

import jakarta.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.getFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.insertInto;
import static org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery.select;

/**
 * Integration tests of the {@link SQLDriver} on the storage of a SQL domain created by
 * Silverpeas, that is to say with integrity constraints between its tables.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class SQLDriverIT extends DataSetTest {

  private static final String DOMAIN_NAME = "4TestDrv";
  private static final String USER_TABLE = "Domain" + DOMAIN_NAME + "_User";
  private static final String GROUP_TABLE = "Domain" + DOMAIN_NAME + "_Group";
  private static final String GROUP_USER_TABLE = "Domain" + DOMAIN_NAME + "_Group_User_Rel";

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Inject
  private SQLDomainRepository repository;

  private File silverpeasHome;
  private SQLDriver driver;

  @Override
  protected Operation getDbSetupInitializations() {
    return Operations.sql("DROP TABLE IF EXISTS " + GROUP_USER_TABLE,
        "DROP TABLE IF EXISTS " + GROUP_TABLE, "DROP TABLE IF EXISTS " + USER_TABLE);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return LibCoreWarBuilder.onFullWarForTestClass(SQLDriverIT.class)
        .build();
  }

  @Before
  public void createDomainStorage() throws Exception {
    silverpeasHome = getFile(mavenTargetDirectoryRule.getBuildDirFile(), "SILVERPEAS_HOME");
    SystemWrapper.getInstance().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());

    File domainProperties = new File(FileRepositoryManager.getDomainPropertiesPath(DOMAIN_NAME));
    Files.createDirectories(domainProperties.getParentFile().toPath());
    Files.writeString(domainProperties.toPath(), String.join("\n",
        "property.Number = 0",
        "database.SQLUserTableName = " + USER_TABLE,
        "database.SQLGroupTableName = " + GROUP_TABLE,
        "database.SQLUserGroupTableName = " + GROUP_USER_TABLE, ""));

    Domain domain = new Domain();
    domain.setName(DOMAIN_NAME);
    Transaction.performInOne(() -> {
      repository.createDomainStorage(domain);
      return null;
    });

    driver = new SQLDriver();
    driver.initFromProperties(
        ResourceLocator.getSettingBundle("org.silverpeas.domains.domain" + DOMAIN_NAME));
  }

  @After
  public void unsetSilverpeasHome() {
    deleteQuietly(new File(FileRepositoryManager.getDomainPropertiesPath(DOMAIN_NAME)));
    deleteQuietly(silverpeasHome);
  }

  @Test
  public void deleteAGroupDeletesAlsoItsSubgroupsAndTheirMemberships() throws Exception {
    execute(insertInto(USER_TABLE).withInsertParam("id", 1).withInsertParam("login", "fnaudin")
        );
    execute(insertInto(GROUP_TABLE).withInsertParam("id", 1).withInsertParam("name", "Tontons")
        );
    execute(insertInto(GROUP_TABLE).withInsertParam("id", 2).withInsertParam("superGroupId", 1)
        .withInsertParam("name", "Flingueurs"));
    execute(insertInto(GROUP_TABLE).withInsertParam("id", 3).withInsertParam("superGroupId", 2)
        .withInsertParam("name", "Volfoni"));
    execute(insertInto(GROUP_TABLE).withInsertParam("id", 4).withInsertParam("name", "Barbouzes")
        );
    execute(insertInto(GROUP_USER_TABLE).withInsertParam("groupId", 1).withInsertParam("userId", 1)
        );
    execute(insertInto(GROUP_USER_TABLE).withInsertParam("groupId", 3).withInsertParam("userId", 1)
        );
    execute(insertInto(GROUP_USER_TABLE).withInsertParam("groupId", 4).withInsertParam("userId", 1)
        );

    Transaction.performInOne(() -> {
      driver.deleteGroup("1");
      return null;
    });

    assertThat(idsIn(GROUP_TABLE, "id"), contains(4));
    assertThat(idsIn(GROUP_USER_TABLE, "groupId"), contains(4));
    assertThat(idsIn(USER_TABLE, "id"), contains(1));
  }

  @Test
  public void deleteAUserDeletesAlsoItsMemberships() throws Exception {
    execute(insertInto(USER_TABLE).withInsertParam("id", 1).withInsertParam("login", "fnaudin")
        );
    execute(insertInto(GROUP_TABLE).withInsertParam("id", 1).withInsertParam("name", "Tontons")
        );
    execute(insertInto(GROUP_USER_TABLE).withInsertParam("groupId", 1).withInsertParam("userId", 1)
        );

    Transaction.performInOne(() -> {
      driver.deleteUser("1");
      return null;
    });

    assertThat(idsIn(USER_TABLE, "id"), empty());
    assertThat(idsIn(GROUP_USER_TABLE, "userId"), empty());
    assertThat(idsIn(GROUP_TABLE, "id"), contains(1));
  }

  private static List<Integer> idsIn(final String table, final String column)
      throws SQLException {
    return select(column).from(table).orderBy(column).execute(r -> r.getInt(1));
  }

  private static void execute(final JdbcSqlQuery modification) {
    Transaction.performInOne(modification::execute);
  }
}
