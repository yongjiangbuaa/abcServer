CREATE TABLE `user_profile` (
  `uid` varchar(40) COLLATE utf8_unicode_ci NOT NULL COMMENT '玩家id',
  `heart`int	DEFAULT  '0' COMMENT  '命',
  `gold` int	DEFAULT '0' COMMENT '金币',
  `star` int	DEFAULT '0' COMMENT '星',
  `heartTime`	bigint	DEFAULT '0' COMMENT '生命恢复倒计时',
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


