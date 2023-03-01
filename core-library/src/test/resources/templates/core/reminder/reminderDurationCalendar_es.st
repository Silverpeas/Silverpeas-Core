RECORDATORIO: $contributionTitle;format="toUpperFirstChar"$ tendrá lugar $if(reminderContributionSeveralDays)$
$if(reminderContributionStart.timeExisting)$
de $reminderContributionStart.dayDate$ a $reminderContributionStart.dayTime$ a $reminderContributionEnd.dayDate$ a $reminderContributionEnd.dayTime$
$else$
de $reminderContributionStart.dayDate$ a $reminderContributionEnd.dayDate$
$endif$
$else$
$if(reminderContributionStart.timeExisting)$
el $reminderContributionStart.dayDate$ de $reminderContributionStart.dayTime$ a $reminderContributionEnd.dayTime$
$else$
el $reminderContributionStart.dayDate$
$endif$
$endif$
$if(reminderContributionZoneId)$
($reminderContributionZoneId$)
$endif$
.
