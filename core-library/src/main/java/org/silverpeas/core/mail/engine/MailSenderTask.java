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

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.mail.MailToSend;
import org.silverpeas.core.thread.ManagedThreadPool;

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
public class MailSenderTask implements Runnable {

  /**
   * The requests are stored in a shared list of Requests. In order to guarantee serial access, all
   * access will be synchronized on this list. Furthermore this list is used to synchronize the
   * providers and the consumers of the list.<br/>
   * When the list is empty, then the thread is killed. It will be instantiated again on the next
   * mail to send.
   */
  private static final List<Request> requestList = new ArrayList<>();

  /**
   * All the requests are processed by a single background thread. This thread is built and started
   * by the start method.
   */
  private static boolean running = false;
  private static Semaphore orderedOneByOneSemaphore = new Semaphore(1, true);

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
   * @param mailToSend
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
        addMailToSendRequest.process(orderedOneByOneSemaphore);
      } catch (Exception e) {
        e.printStackTrace();
        SilverTrace
            .error("mailSenderEngine", "MailSenderThread", "mailSenderEngine.UNEXPECTED_ERROR", e);
      }
    }
  }

  /**
   * The constructor is private : only one MailSenderThread will be created to process all the
   * request.
   */
  private MailSenderTask() {
  }

  /**
   * Process all the requests. This method should be private but is already declared public in the
   * base class Thread.
   */
  @Override
  public void run() {


    Request currentRequest = nextRequest();

    // The loop condition must be verified on a private attribute of run method (not on the static
    // running attribute) in order to avoid concurrent access.
    while (currentRequest != null) {

        /*
         * Each request is processed out of the synchronized block so the others threads (which put
         * the requests) will not be blocked.
         */
      try {
        currentRequest.process(orderedOneByOneSemaphore);
      } catch (Exception e) {
        e.printStackTrace();
        SilverTrace
            .error("mailSenderEngine", "MailSenderThread", "mailSenderEngine.UNEXPECTED_ERROR", e);
      }

      // Getting the next request if any.
      currentRequest = nextRequest();
    }


  }

  /**
   * Gets the next request.
   * @return the next request.
   */
  private Request nextRequest() {
    synchronized (requestList) {
      final Request nextRequest;
      if (!requestList.isEmpty()) {

        nextRequest = requestList.remove(0);
      } else {
        nextRequest = null;
        running = false;
      }
      return nextRequest;
    }
  }
}

/**
 * Each request must define a method called process which will process the request with a given
 * MailSendingManager.
 */
interface Request {
  public void process(final Semaphore orderedOneByOneSemaphore) throws InterruptedException;
}

/**
 * Permits to add a request of adding a mail to send.
 */
class AddMailToSendRequest implements Request {
  private final MailToSend mailToSend;

  /**
   * Constructor declaration
   * @param mailToSend
   */
  public AddMailToSendRequest(MailToSend mailToSend) {
    this.mailToSend = mailToSend;
  }

  /**
   * As {@link MailSenderTask} can send a mail synchronously or asynchronously, this method is
   * synchronized to ensure that one send is performed at a same time laps.
   * @param orderedOneByOneSemaphore
   */
  @Override
  public void process(final Semaphore orderedOneByOneSemaphore) throws InterruptedException {
    try {
      orderedOneByOneSemaphore.acquire();
      MailSenderProvider.get().send(mailToSend);
    } finally {
      orderedOneByOneSemaphore.release();
    }
  }
}