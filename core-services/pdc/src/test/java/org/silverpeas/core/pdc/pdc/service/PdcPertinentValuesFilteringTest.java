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
package org.silverpeas.core.pdc.pdc.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.pdc.classification.ObjectValuePair;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.tree.model.TreeNode;
import org.silverpeas.core.pdc.tree.service.TreeService;
import org.silverpeas.core.persistence.jdbc.ConnectionPool;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the filtering of the values of an axis of the PdC by the contents classified on
 * them within some component instances: only the values on which at least one content is
 * classified (directly or through one of their daughters) are kept, with their number of
 * contents. This filtering backs the PdC filter of the search engine and of the "latest
 * publications" of a space home page.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PdcPertinentValuesFilteringTest {

  private static final String AXIS_ID = "5";
  private static final int TREE_ID = 12;
  private static final String USER_ID = "3";
  private static final List<String> INSTANCE_IDS = List.of("almanach3", "kmelia42");

  private final TreeService treeService = mock(TreeService.class);
  private final PdcClassifyManager pdcClassifyManager = mock(PdcClassifyManager.class);
  private final SearchContext searchContext = new SearchContext(USER_ID);
  private GlobalPdcManager pdcManager;

  @BeforeEach
  void setUpThePdcManager(@TestManagedMock ConnectionPool connectionPool,
      @TestManagedMock PublicationAccessControl publicationAccessControl) throws Exception {
    // the header of the axis is got from the data source: the PdC manager is spied to do without
    pdcManager = spy(new GlobalPdcManager());
    FieldUtils.writeField(pdcManager, "treeService", treeService, true);
    FieldUtils.writeField(pdcManager, "pdcClassifyManager", pdcClassifyManager, true);
    final AxisHeader axisHeader = new AxisHeader();
    axisHeader.setRootId(TREE_ID);
    doReturn(axisHeader).when(pdcManager).getAxisHeader(AXIS_ID, false);
    setUpTheValuesOfTheAxis();
  }

  /**
   * The axis is made up of its root, of two first-level values and of a daughter of the second
   * one: /0/, /0/1/, /0/2/ and /0/2/3/.
   */
  private void setUpTheValuesOfTheAxis() throws Exception {
    final TreeNode root = aTreeNode("0", "-1", "/", 0);
    final List<TreeNode> tree = List.of(root,
        aTreeNode("1", "0", "/0/", 1),
        aTreeNode("2", "0", "/0/", 1),
        aTreeNode("3", "2", "/0/2/", 2));
    when(treeService.getRoot(any(), eq(String.valueOf(TREE_ID)))).thenReturn(root);
    when(treeService.getTree(any(), eq(String.valueOf(TREE_ID)))).thenReturn(tree);
  }

  private void classifyContentsOn(final ObjectValuePair... contents) throws Exception {
    when(pdcClassifyManager.getObjectValuePairs(searchContext, Integer.parseInt(AXIS_ID),
        INSTANCE_IDS)).thenReturn(List.of(contents));
  }

  @Test
  void onlyTheValuesWithContentsAreKeptWithTheirNumberOfContents() throws Exception {
    classifyContentsOn(new ObjectValuePair("42", "/0/2/3/", "almanach3"),
        new ObjectValuePair("43", "/0/2/3/", "almanach3"));

    final List<Value> values = pdcManager.getPertinentDaughterValuesByInstanceIds(searchContext,
        AXIS_ID, "0", INSTANCE_IDS);

    assertThat(values.stream().map(Value::getFullPath).toList(),
        contains("/0/", "/0/2/", "/0/2/3/"));
    assertThat(values.stream().map(Value::getNbObjects).toList(), contains(2, 2, 2));
  }

  @Test
  void theDaughtersOfAValueWithoutContentsAreSkipped() throws Exception {
    classifyContentsOn(new ObjectValuePair("42", "/0/1/", "almanach3"));

    final List<Value> values = pdcManager.getPertinentDaughterValuesByInstanceIds(searchContext,
        AXIS_ID, "0", INSTANCE_IDS);

    assertThat(values.stream().map(Value::getFullPath).toList(), contains("/0/", "/0/1/"));
    assertThat(values.stream().map(Value::getNbObjects).toList(), contains(1, 1));
  }

  @Test
  void noValueIsKeptWhenNoContentIsClassifiedOnTheAxis() throws Exception {
    classifyContentsOn();

    final List<Value> values = pdcManager.getFirstLevelAxisValuesByInstanceIds(searchContext,
        AXIS_ID, INSTANCE_IDS);

    assertThat(values, is(empty()));
  }

  private static TreeNode aTreeNode(final String id, final String fatherId, final String path,
      final int level) {
    final TreeNode node = new TreeNode(id, String.valueOf(TREE_ID));
    node.setFatherId(fatherId);
    node.setPath(path);
    node.setLevelNumber(level);
    return node;
  }
}
