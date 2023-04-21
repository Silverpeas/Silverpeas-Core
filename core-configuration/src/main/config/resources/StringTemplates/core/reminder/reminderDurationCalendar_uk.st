НАГАДУВАННЯ: $contributionTitle;format="toUpperFirstChar"$ відбудеться $if(reminderContributionSeveralDays)$
$if(reminderContributionStart.timeExisting)$
з $reminderContributionStart.dayDate$ до $reminderContributionStart.dayTime$ до $reminderContributionEnd.dayDate$ до $reminderContributionEnd.dayTime$
$else$
з $reminderContributionStart.dayDate$ до $reminderContributionEnd.dayDate$
$endif$
$else$
$if(reminderContributionStart.timeExisting)$
$reminderContributionStart.dayDate$ з $reminderContributionStart.dayTime$ до $reminderContributionEnd.dayTime$
$else$
$reminderContributionStart.dayDate$
$endif$
$endif$
$if(reminderContributionZoneId)$
($reminderContributionZoneId$)
$endif$
.
