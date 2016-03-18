EXEC sp_rename 'sb_interest_center', 'sb_interests';
EXEC sp_rename 'sb_interest_center_axis', 'sb_interests_axis';
UPDATE uniqueid SET tablename = 'sb_interests' WHERE tablename = 'sb_interest_center';