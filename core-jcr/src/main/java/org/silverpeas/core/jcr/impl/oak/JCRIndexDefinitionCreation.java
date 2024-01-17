/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package org.silverpeas.core.jcr.impl.oak;

import org.apache.jackrabbit.oak.plugins.index.IndexUtils;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeStore;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.jcr.SilverpeasRepository;
import org.silverpeas.core.jcr.util.SilverpeasJCRIndexation;
import org.silverpeas.core.jcr.util.SilverpeasProperty;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Technical
@Service
@Singleton
public class JCRIndexDefinitionCreation implements SilverpeasJCRIndexation {

  private static final String JCR_INDEX = "/silverpeas-oak-index.properties";

  @Inject
  private SilverpeasRepository repository;

  @Override
  public void initialize() {
    if (repository instanceof OakRepository) {
      SilverLogger.getLogger(this).info("Initialize indexation in JCR...");
      NodeBuilder indexRoot = getIndexRoot();
      Properties indexDefs = loadIndexDefinitions();
      indexDefs.forEach((key, value) -> {
        String name = (String) key;
        String properties = (String) value;
        String[] np = Arrays.stream(properties.split(" ")).map(String::trim).toArray(String[]::new);
        if (!indexRoot.hasChildNode(name)) {
          String prop = np.length > 1 ? "properties " : "property ";
          SilverLogger.getLogger(this).info("Create index " + name + " on " + prop +
              String.join(" and ", np));
          IndexUtils.createIndexDefinition(indexRoot, name, false, false, Arrays.asList(np),
              List.of(SilverpeasProperty.SLV_SIMPLE_DOCUMENT));
        } else {
          SilverLogger.getLogger(this).info("Index " + name + " already created");
        }
      });
    }
  }

  private NodeBuilder getIndexRoot() {
    OakRepository oakRepository = (OakRepository) repository;
    NodeStore nodeStore = oakRepository.getNodeStore();
    NodeBuilder root = nodeStore.getRoot().builder();
    return IndexUtils.getOrCreateOakIndex(root);
  }

  private Properties loadIndexDefinitions() {
    InputStream indexDefsInput = getClass().getResourceAsStream(JCR_INDEX);
    Properties indexDefs = new Properties();
    try {
      indexDefs.load(indexDefsInput);
    } catch (IOException e) {
      throw new SilverpeasRuntimeException(e);
    }
    return indexDefs;
  }
}
