/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package org.silverpeas.core.mail;

import org.silverpeas.core.util.CollectionUtil;

import javax.mail.Message;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This {@link java.util.LinkedHashSet} implementation handles {@link MailAddress} data.
 * It permits to define several receiver addresses and following configuration:
 * <ul>
 * <li>the number of receivers to specify for a one send, {@link #DEFAULT_BATCH_SIZE} by
 * default. If the value is less than or equals to zero, then all receivers are specified in a one
 * single send</li>
 * <li>the recipient type {@link ReceiverMailAddressSet.MailRecipientType} of
 * the receivers
 * addresses. {@link ReceiverMailAddressSet.MailRecipientType#TO} by
 * default</li>
 * </ul>
 * @author Yohann Chastagnier
 */
public class ReceiverMailAddressSet extends LinkedHashSet<MailAddress> {
  private static final long serialVersionUID = 6660986678159523787L;

  /*
   * The default batch size
   */
  private static final int DEFAULT_BATCH_SIZE = 0;

  public enum MailRecipientType {
    /**
     * The "To" (primary) recipients.
     */
    TO(Message.RecipientType.TO),
    /**
     * The "Cc" (carbon copy) recipients.
     */
    CC(Message.RecipientType.CC),
    /**
     * The "Bcc" (blind carbon copy) recipients.
     */
    BCC(Message.RecipientType.BCC);

    private final Message.RecipientType type;

    MailRecipientType(final Message.RecipientType type) {
      this.type = type;
    }

    /**
     * Gets the linked {@link Message.RecipientType}.
     * @return the linked {@link Message.RecipientType}.
     */
    public Message.RecipientType getTechnicalType() {
      return type;
    }
  }

  // The default recipient type
  private MailRecipientType recipientType = MailRecipientType.TO;

  // The default batch size
  private int receiversBatchSizeForOneSend = DEFAULT_BATCH_SIZE;

  /**
   * See {@link ReceiverMailAddressSet#with(java.util.Collection)} details.
   */
  public static ReceiverMailAddressSet with(MailAddress... mailAddresses) {
    return with(CollectionUtil.asList(mailAddresses));
  }

  /**
   * Adds several {@link MailAddress} instances.
   * @param mailAddresses the {@link MailAddress} instances.
   * @return the set instance
   */
  public static ReceiverMailAddressSet with(Collection<MailAddress> mailAddresses) {
    ReceiverMailAddressSet list = new ReceiverMailAddressSet();
    if (mailAddresses != null) {
      list.addAll(mailAddresses);
    }
    return list;
  }

  /**
   * Adds several {@link MailAddress} instances.
   * @param mailRecipientType the recipient type to take into account.
   * @return the set instance
   */
  public static ReceiverMailAddressSet ofRecipientType(MailRecipientType mailRecipientType) {
    return new ReceiverMailAddressSet().withRecipientType(mailRecipientType);
  }

  /**
   * Hidden constructor.
   */
  private ReceiverMailAddressSet() {
  }

  /**
   * Gets the recipient type.
   * @return the recipient type.
   */
  public MailRecipientType getRecipientType() {
    return recipientType;
  }

  /**
   * Sets a recipient type. If null, {@link ReceiverMailAddressSet
   * .MailRecipientType#TO}
   * is taken in
   * account by default
   * @param recipientType the new recipient type to take into account.
   * @return the set instance
   */
  public ReceiverMailAddressSet withRecipientType(final MailRecipientType recipientType) {
    this.recipientType = recipientType;
    return this;
  }

  /**
   * Gets the number of receivers that must be specified for one send.
   * @return the number of receivers that must be specified for one send.
   */
  int getReceiversBatchSizeForOneSend() {
    return receiversBatchSizeForOneSend;
  }

  /**
   * Sets the new number of receivers that must be specified for one send.
   * If the value is less than or equals to zero, then all receivers will be specified in a one
   * single send.
   * @param receiversBatchSizeForOneSend the new number of receivers that must be specified for one
   * send.
   * @return the set instance
   */
  public ReceiverMailAddressSet withReceiversBatchSizeOf(final int receiversBatchSizeForOneSend) {
    this.receiversBatchSizeForOneSend =
        receiversBatchSizeForOneSend >= 0 ? receiversBatchSizeForOneSend : DEFAULT_BATCH_SIZE;
    return this;
  }

  /**
   * Gets the list of receiver batches according to the defined number of receivers per send batch.
   * @return the batched receiver list.
   */
  public List<ReceiverMailAddressSet> getBatchedReceiversList() {
    if (size() <= getReceiversBatchSizeForOneSend() || getReceiversBatchSizeForOneSend() == 0) {
      return Collections.singletonList(this);
    }
    List<ReceiverMailAddressSet> batchedReceiverList =
        new ArrayList<>((size() / getReceiversBatchSizeForOneSend()) + 1);

    ReceiverMailAddressSet current = null;
    for (MailAddress mailAddress : this) {
      if (current == null || current.size() == getReceiversBatchSizeForOneSend()) {
        current = new ReceiverMailAddressSet().withRecipientType(getRecipientType())
            .withReceiversBatchSizeOf(getReceiversBatchSizeForOneSend());
        batchedReceiverList.add(current);
      }
      current.add(mailAddress);
    }
    return batchedReceiverList;
  }

  /**
   * Gets the list of emails each one separated by a comma.
   * @return the list of emails each one separated by a comma.
   */
  public String getEmailsSeparatedByComma() {
    String comma = ",";
    StringBuilder sb = new StringBuilder(30 * size());
    for (MailAddress mailAddress : this) {
      if (sb.length() > 0) {
        sb.append(comma);
      }
      sb.append(mailAddress.getEmail());
    }
    return sb.toString();
  }
}
