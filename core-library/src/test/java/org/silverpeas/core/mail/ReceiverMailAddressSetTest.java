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

import org.junit.Test;
import org.silverpeas.core.util.StringUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ReceiverMailAddressSetTest {

  @Test
  public void emptyWithDefaultValues() {
    ReceiverMailAddressSet set = initializeSeveralForTest(0);
    assertThat(set, hasSize(0));
    assertThat(set.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(set.getReceiversBatchSizeForOneSend(), is(0));
    assertThat(set.getBatchedReceiversList(), hasSize(1));
    assertThat(set.getBatchedReceiversList().get(0), sameInstance(set));
    assertThat(set.getEmailsSeparatedByComma(), isEmptyString());
  }

  @Test
  public void oneWithCcRecipientTypeAndOtherDefaultValues() {
    ReceiverMailAddressSet set =
        initializeSeveralForTest(1).withRecipientType(ReceiverMailAddressSet.MailRecipientType.CC);
    assertThat(set, hasSize(1));
    assertThat(set.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.CC));
    assertThat(set.getReceiversBatchSizeForOneSend(), is(0));
    assertThat(set.getBatchedReceiversList(), hasSize(1));
    assertThat(set.getBatchedReceiversList().get(0), sameInstance(set));
    assertThat(set.getEmailsSeparatedByComma(), is("Email_000"));
  }

  @Test
  public void tenWithNegativeBatchValueAndOtherDefaultValues() {
    int size = 10;
    ReceiverMailAddressSet set = initializeSeveralForTest(size).withReceiversBatchSizeOf(-55);
    assertThat(set, hasSize(size));
    assertThat(set.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(set.getReceiversBatchSizeForOneSend(), is(0));
    assertThat(set.getBatchedReceiversList(), hasSize(1));
    assertThat(set.getBatchedReceiversList().get(0), sameInstance(set));
    assertThat(set.getEmailsSeparatedByComma(),
        is("Email_000,Email_001,Email_002,Email_003,Email_004,Email_005,Email_006,Email_007," +
            "Email_008,Email_009"));
  }

  @Test
  public void tenWithReceiverBatchOf10AndOtherDefaultValues() {
    int size = 10;
    ReceiverMailAddressSet set = initializeSeveralForTest(size).withReceiversBatchSizeOf(10);
    assertThat(set, hasSize(size));
    assertThat(set.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(set.getReceiversBatchSizeForOneSend(), is(10));
    assertThat(set.getBatchedReceiversList(), hasSize(1));
    assertThat(set.getBatchedReceiversList().get(0), sameInstance(set));
    assertThat(set.getEmailsSeparatedByComma(),
        is("Email_000,Email_001,Email_002,Email_003,Email_004,Email_005,Email_006,Email_007," +
            "Email_008,Email_009"));
  }

  @Test
  public void tenWithBccRecipientTypeAndReceiverBatchOf2() {
    int size = 10;
    int batch = 2;
    ReceiverMailAddressSet set = initializeSeveralForTest(size)
        .withRecipientType(ReceiverMailAddressSet.MailRecipientType.BCC)
        .withReceiversBatchSizeOf(batch);
    assertThat(set, hasSize(size));
    assertThat(set.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(set.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(set.getBatchedReceiversList(), hasSize(5));

    ReceiverMailAddressSet currentBatch = set.getBatchedReceiversList().get(0);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_000,Email_001"));

    currentBatch = set.getBatchedReceiversList().get(1);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_002,Email_003"));

    currentBatch = set.getBatchedReceiversList().get(2);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_004,Email_005"));

    currentBatch = set.getBatchedReceiversList().get(3);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_006,Email_007"));

    currentBatch = set.getBatchedReceiversList().get(4);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_008,Email_009"));
  }

  @Test
  public void fiveWithReceiverBatchOf1AndOtherDefaultValues() {
    int size = 5;
    int batch = 1;
    ReceiverMailAddressSet set = initializeSeveralForTest(size).withReceiversBatchSizeOf(batch);
    assertThat(set, hasSize(size));
    assertThat(set.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(set.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(set.getBatchedReceiversList(), hasSize(5));

    ReceiverMailAddressSet currentBatch = set.getBatchedReceiversList().get(0);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_000"));

    currentBatch = set.getBatchedReceiversList().get(1);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_001"));

    currentBatch = set.getBatchedReceiversList().get(2);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_002"));

    currentBatch = set.getBatchedReceiversList().get(3);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_003"));

    currentBatch = set.getBatchedReceiversList().get(4);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.TO));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_004"));
  }

  @Test
  public void tenWithBccRecipientTypeAndReceiverBatchOf3() {
    int size = 10;
    int batch = 3;
    ReceiverMailAddressSet set = initializeSeveralForTest(size)
        .withRecipientType(ReceiverMailAddressSet.MailRecipientType.BCC)
        .withReceiversBatchSizeOf(batch);
    assertThat(set, hasSize(size));
    assertThat(set.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(set.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(set.getBatchedReceiversList(), hasSize(4));

    ReceiverMailAddressSet currentBatch = set.getBatchedReceiversList().get(0);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_000,Email_001,Email_002"));

    currentBatch = set.getBatchedReceiversList().get(1);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_003,Email_004,Email_005"));

    currentBatch = set.getBatchedReceiversList().get(2);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(batch));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_006,Email_007,Email_008"));

    currentBatch = set.getBatchedReceiversList().get(3);
    assertThat(currentBatch.getRecipientType(), is(ReceiverMailAddressSet.MailRecipientType.BCC));
    assertThat(currentBatch.getReceiversBatchSizeForOneSend(), is(batch));
    assertThat(currentBatch.getBatchedReceiversList(), hasSize(1));
    assertThat(currentBatch.getBatchedReceiversList().get(0), sameInstance(currentBatch));
    assertThat(currentBatch, not(sameInstance(set)));
    assertThat(currentBatch, hasSize(1));
    assertThat(currentBatch.getEmailsSeparatedByComma(), is("Email_009"));
  }

  private ReceiverMailAddressSet initializeSeveralForTest(int size) {
    ReceiverMailAddressSet set = ReceiverMailAddressSet.with();
    for (int i = 0; i < size; i++) {
      set.add(MailAddress.eMail("Email_" + StringUtil.leftPad(String.valueOf(i), 3, "0")));
    }
    return set;
  }
}