НАПОМИНАНИЕ: $contributionTitle;format="toUpperFirstChar"$ состоится $if(reminderContributionSeveralDays)$
$if(reminderContributionStart.timeExisting)$
$reminderContributionStart.dayDate$ к $reminderContributionStart.dayTime$ к $reminderContributionEnd.dayDate$ к $reminderContributionEnd.dayTime$
$else$
$reminderContributionStart.dayDate$ к $reminderContributionEnd.dayDate$
$endif$
$else$
$if(reminderContributionStart.timeExisting)$
$reminderContributionStart.dayDate$ $reminderContributionStart.dayTime$ к $reminderContributionEnd.dayTime$
$else$
$reminderContributionStart.dayDate$
$endif$
$endif$
$if(reminderContributionZoneId)$
($reminderContributionZoneId$)
$endif$
.
