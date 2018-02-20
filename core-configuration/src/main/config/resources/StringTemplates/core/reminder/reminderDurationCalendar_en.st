REMINDER: $contributionTitle;format="toUpperFirstChar"$ will be $if(reminderContributionSeveralDays)$
$if(reminderContributionStart.hourExisting)$
from the $reminderContributionStart.dayDate$ at $reminderContributionStart.hour$ to the $reminderContributionEnd.dayDate$ at $reminderContributionEnd.hour$
$else$
from the $reminderContributionStart.dayDate$ to the $reminderContributionEnd.dayDate$
$endif$
$else$
$if(reminderContributionStart.hourExisting)$
on the $reminderContributionStart.dayDate$ from $reminderContributionStart.hour$ to $reminderContributionEnd.hour$
$else$
on the $reminderContributionStart.dayDate$
$endif$
$endif$
$if(reminderContributionZoneId)$
 ($reminderContributionZoneId$)
$endif$
.