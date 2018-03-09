REMINDER: $contributionTitle;format="toUpperFirstChar"$ will be $if(reminderContributionSeveralDays)$
$if(reminderContributionStart.hourExisting)$
from $reminderContributionStart.dayDate$ at $reminderContributionStart.hour$ to $reminderContributionEnd.dayDate$ at $reminderContributionEnd.hour$
$else$
from $reminderContributionStart.dayDate$ to $reminderContributionEnd.dayDate$
$endif$
$else$
$if(reminderContributionStart.hourExisting)$
on $reminderContributionStart.dayDate$ from $reminderContributionStart.hour$ to $reminderContributionEnd.hour$
$else$
on $reminderContributionStart.dayDate$
$endif$
$endif$
$if(reminderContributionZoneId)$
 ($reminderContributionZoneId$)
$endif$
.