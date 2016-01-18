/**
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.silverpeas.comment.service;

import com.silverpeas.comment.model.Comment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.test.BasicWarBuilder;
import org.silverpeas.test.rule.DbSetupRule;
import org.silverpeas.util.ForeignPK;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the deletion of all comments belonging to a component instance.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ComponentInstCommentsRemoverIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT = "/com/silverpeas/comment/create-database.sql";
  private static final String DATASET_SCRIPT = "/com/silverpeas/comment/comment-dataset.sql";

  private static final String COMPONENT_INSTANCE_ID = "instanceId10";

  @Inject
  public ComponentInstCommentsRemover remover;
  @Inject
  public CommentService service;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT).loadInitialDataSetFrom(DATASET_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(ComponentInstCommentsRemover.class)
        .addMavenDependenciesWithPersistence("org.silverpeas.core:lib-core")
        .addMavenDependencies("org.apache.tika:tika-core")
        .addMavenDependencies("org.apache.tika:tika-parsers")
        .createMavenDependenciesWithPersistence("org.silverpeas.core.ejb-core:node")
        .createMavenDependencies("org.silverpeas.core.ejb-core:tagcloud")
        .createMavenDependencies("org.silverpeas.core.ejb-core:publication")
        .createMavenDependencies("org.silverpeas.core.ejb-core:clipboard")
        .testFocusedOn(war -> {
          war.addPackages(true, "com.silverpeas.comment")
              .addAsResource("com/silverpeas/comment")
              .addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST-MF");
        })
        .build();
  }

  @Test
  public void allCommentsShouldBeDeletedWithAnExistingComponentInstance() {
    List<Comment> commentsToDelete = getAllCommentsFor(COMPONENT_INSTANCE_ID);
    List<Comment> otherComments = getAllCommentsFor("instanceId20");
    assertThat(commentsToDelete.isEmpty(), is(false));
    assertThat(otherComments.isEmpty(), is(false));

    remover.delete(COMPONENT_INSTANCE_ID);

    commentsToDelete = getAllCommentsFor(COMPONENT_INSTANCE_ID);
    List<Comment> otherCommentsAfter = getAllCommentsFor("instanceId20");
    assertThat(commentsToDelete.isEmpty(), is(true));
    assertThat(otherCommentsAfter.size(), is(otherComments.size()));
  }

  @Test
  public void nothingIsDoneForANonExistingComponentInstance() {
    List<Comment> comments = getAllCommentsFor("toto");
    assertThat(comments.isEmpty(), is(true));

    remover.delete("toto");
  }

  private List<Comment> getAllCommentsFor(String instanceId) {
    return service.getAllCommentsOnPublication(null, new ForeignPK(null, instanceId));
  }
}
