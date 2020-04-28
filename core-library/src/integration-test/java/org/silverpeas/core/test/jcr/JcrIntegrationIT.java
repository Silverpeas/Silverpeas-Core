/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.test.jcr;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.silverpeas.core.contribution.attachment.process.SimpleDocumentDummyHandledFileConverter;
import org.silverpeas.core.contribution.attachment.repository.JcrContext;
import org.silverpeas.core.contribution.attachment.util.AttachmentSettings;
import org.silverpeas.core.test.extention.SettingBundleStub;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.ServiceProvider;

/**
 * @author Yohann Chastagnier
 */
public abstract class JcrIntegrationIT {

  public static final String DATABASE_CREATION_SCRIPT =
      "org/silverpeas/core/admin/create_space_components_database.sql";
  public static final String DATASET_SCRIPT =
      "org/silverpeas/core/admin/test-spaces_and_components-dataset.sql";
  protected SettingBundleStub attachmentSettings;
  public MavenTargetDirectoryRule mavenTargetDirectory = new MavenTargetDirectoryRule(this);

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("/" + DATABASE_CREATION_SCRIPT)
      .loadInitialDataSetFrom("/" + DATASET_SCRIPT);

  private ExpectedException expectedException = ExpectedException.none();
  private JcrContext jcrContext = new JcrContext();

  @Rule
  public MavenTargetDirectoryRule getMavenTargetDirectory() {
    return mavenTargetDirectory;
  }

  @Rule
  public ExpectedException getExpectedException() {
    return expectedException;
  }

  @Rule
  public JcrContext getJcr() {
    return jcrContext;
  }

  @Before
  public void setup() throws Exception {
    ServiceProvider.getService(SimpleDocumentDummyHandledFileConverter.class).init();
    attachmentSettings = new SettingBundleStub(AttachmentSettings.class, "settings");
    attachmentSettings.beforeEach(null);
  }

  @After
  public void clean() throws Exception {
    attachmentSettings.afterEach(null);
  }
}
