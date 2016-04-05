/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.comment.service;

import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.comment.test.WarBuilder4Comment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.ForeignPK;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the deletion of all comments belonging to a component instance.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ComponentInstanceDeletionIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT = "/org/silverpeas/core/comment/create-database.sql";
  private static final String DATASET_SCRIPT = "/org/silverpeas/core/comment/comment-dataset.sql";

  private static final String COMPONENT_INSTANCE_ID = "instanceId10";

  @Inject
  public DefaultCommentService service;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Comment
        .onWarForTestClass(ComponentInstanceDeletionIntegrationTest.class)
        .build();
  }

  @Test
  public void allCommentsShouldBeDeletedWithAnExistingComponentInstance() {
    List<Comment> commentsToDelete = getAllCommentsFor(COMPONENT_INSTANCE_ID);
    List<Comment> otherComments = getAllCommentsFor("instanceId20");
    assertThat(commentsToDelete.isEmpty(), is(false));
    assertThat(otherComments.isEmpty(), is(false));

    service.delete(COMPONENT_INSTANCE_ID);

    commentsToDelete = getAllCommentsFor(COMPONENT_INSTANCE_ID);
    List<Comment> otherCommentsAfter = getAllCommentsFor("instanceId20");
    assertThat(commentsToDelete.isEmpty(), is(true));
    assertThat(otherCommentsAfter.size(), is(otherComments.size()));
  }

  @Test
  public void nothingIsDoneForANonExistingComponentInstance() {
    List<Comment> comments = getAllCommentsFor("toto");
    assertThat(comments.isEmpty(), is(true));

    service.delete("toto");
  }

  private List<Comment> getAllCommentsFor(String instanceId) {
    return service.getAllCommentsOnPublication(null, new ForeignPK(null, instanceId));
  }
}
