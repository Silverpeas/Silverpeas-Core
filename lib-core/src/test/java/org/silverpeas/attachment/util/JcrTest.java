/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment.util;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.silverpeas.attachment.repository.DocumentRepository;
import org.silverpeas.util.Charsets;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * This class handle a JCR test with using a String context.
 * @author: Yohann Chastagnier
 */
public abstract class JcrTest {

  private ClassPathXmlApplicationContext appContext;

  private DocumentRepository documentRepository = new DocumentRepository();

  public ClassPathXmlApplicationContext getAppContext() {
    return appContext;
  }

  public void setAppContext(final ClassPathXmlApplicationContext appContext) {
    this.appContext = appContext;
  }

  public DocumentRepository getDocumentRepository() {
    return documentRepository;
  }

  public JackrabbitRepository getRepository() {
    return ((JackrabbitRepository) getAppContext().getBean(BasicDaoFactory.JRC_REPOSITORY));
  }

  public abstract void run() throws Exception;


  /**
   * Execute the test with its context.
   * @throws Exception
   */
  public void execute() throws Exception {
    setAppContext(new ClassPathXmlApplicationContext("/spring-pure-memory-jcr.xml"));
    Reader reader = new InputStreamReader(
        JcrTest.class.getClassLoader().getResourceAsStream("silverpeas-jcr.txt"), Charsets.UTF_8);
    File file = new File(FileRepositoryManager.getAbsolutePath(""));
    FileUtils.deleteQuietly(file);
    try {
      try {
        SilverpeasRegister.registerNodeTypes(reader);
      } finally {
        IOUtils.closeQuietly(reader);
      }
      run();
    } finally {
      file = new File(FileRepositoryManager.getAbsolutePath(""));
      FileUtils.deleteQuietly(file);
      getRepository().shutdown();
      getAppContext().close();
    }
  }
}
