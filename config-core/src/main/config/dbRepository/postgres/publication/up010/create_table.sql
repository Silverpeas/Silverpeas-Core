UPDATE SB_Publication_Publi set pubStatus='Valid' where instanceId like 'kmax%';

UPDATE SB_Publication_Publi 
set infoId='0' 
where instanceId like 'kmax%' 
and infoId < 'a'
and infoId not in (select infoId from SB_Publication_Info where instanceId like 'kmax%')
;