#ALTER TABLE `user_profile` ADD COLUMN `paidGold` BIGINT  NOT NULL DEFAULT '0' COMMENT '';
ALTER TABLE `user_profile` MODIFY COLUMN `gold` BIGINT  NOT NULL DEFAULT '0' COMMENT '';
 

