/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.mail.engine;

import org.silverpeas.core.mail.MailToSend;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * A thread MailSenderThread in the background a batch of mail sending. All the public methods
 * are static, so only one thread runs and processes the mail sending.<br/>
 * When it get no more mail to send, the thread ends a new one will be instantiated on the next
 * mail sending request.<br/>
 * Priority is given to synchronous mail sending request.
 */
public class MailSenderTask extends AbstractRequestTask<MailProcessContext> {

  /**
   * The requests are stored in a shared list of Requests. In order to guarantee serial access, all
   * access will be synchronized on this list. Furthermore this list is used to synchronize the
   * providers and the consumers of the list.<br/>
   * When the list is empty, then the thread is killed. It will be instantiated again on the next
   * mail to send.
   */
  private static final List<Request<MailProcessContext>> requestList = new ArrayList<>();

  /**
   * All the requests are processed by a single background thread. This thread is built and started
   * by the start method.
   */
  private static boolean running = false;
  private static Semaphore orderedOneByOneSemaphore = new Semaphore(1, true);

  /**
   * The constructor is private : only one MailSenderThread will be created to process all the
   * request.
   */
  private MailSenderTask() {
  }

  /**
   * Builds and starts the thread which will process all the requests. This method is synchronized
   * on the requests queue in order to guarantee that only one MailSenderThread is running.
   */
  private static void startIfNotAlreadyDone() {
    if (!running) {
      running = true;
      ManagedThreadPool.invoke(new MailSenderTask());
    }
  }

  /**
   * Add a mail to send.
   * @param mailToSend a mail to send.
   */
  public static void addMailToSend(MailToSend mailToSend) {
    AddMailToSendRequest addMailToSendRequest = new AddMailToSendRequest(mailToSend);
    if (mailToSend.isAsynchronous()) {
      synchronized (requestList) {
        requestList.add(addMailToSendRequest);
        startIfNotAlreadyDone();
      }
    } else {
      // The sending is performed synchronously
      try {
        addMailToSendRequest.process(new MailProcessContext(orderedOneByOneSemaphore));
      } catch (Exception e) {
        SilverLogger.getLogger(MailSenderTask.class).error(e.getLocalizedMessage(), e);
      }
    }
  }

  @Override
  protected List<Request<MailProcessContext>> getRequestList() {
    return requestList;
  }

  @Override
  protected void taskIsEnding() {
    running = false;
  }

  @Override
  protected MailProcessContext getProcessContext() {
    return new MailProcessContext(orderedOneByOneSemaphore);
  }
}

class MailProcessContext implements AbstractRequestTask.ProcessContext {
  private final Semaphore semaphore;

  MailProcessContext(final Semaphore semaphore) {
    this.semaphore = semaphore;
  }

  Semaphore getSemaphore() {
    return semaphore;
  }
}

/**
 * Permits to add a request of adding a mail to send.
 */
class AddMailToSendRequest implements AbstractRequestTask.Request<MailProcessContext> {
  private final MailToSend mailToSend;

  /**
   * Constructor declaration
   * @param mailToSend the mail to send.
   */
  AddMailToSendRequest(MailToSend mailToSend) {
    this.mailToSend = mailToSend;
  }

  /**
   * As {@link MailSenderTask} can send a mail synchronously or asynchronously, this method is
   * synchronized to ensure that one send is performed at a same time laps.
   * @param context the context of the request processing.
   */
  @Override
  public void process(final MailProcessContext context) throws InterruptedException {
    try {
      context.getSemaphore().acquire();
      MailSenderProvider.get().send(mailToSend);
    } finally {
      context.getSemaphore().release();
    }
  }
}