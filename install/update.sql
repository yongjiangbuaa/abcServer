CREATE TABLE `user_profile` (
  `uid` varchar(40) COLLATE utf8_unicode_ci NOT NULL COMMENT '玩家id',
  `heart`int	DEFAULT  '0' COMMENT  '命',
  `gold` int	DEFAULT '0' COMMENT '金币',
  `star` int	DEFAULT '0' COMMENT '星',
  `heartTime`	bigint	DEFAULT '0' COMMENT '生命恢复倒计时',
  `level`	int DEFAULT '1' COMMENT '关卡', 
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `uid_bind` (
  `uid` varchar(40) COLLATE utf8_unicode_ci NOT NULL COMMENT '玩家id',
  `bindId` varchar(200) COLLATE utf8_unicode_ci NOT NULL COMMENT '绑定类型id',
  `type`	int DEFAULT '0' COMMENT '绑定类型 1设备 2.三方账号1比如fb 3...',
  `time`	bigint	DEFAULT '0' COMMENT '绑定时间',
  PRIMARY KEY (`bindId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


