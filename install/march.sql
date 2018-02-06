CREATE TABLE `user_profile` (
  `uid` varchar(40) COLLATE utf8_unicode_ci NOT NULL COMMENT '玩家id',
  `heart`int	DEFAULT  '0' COMMENT  '命',
  `gold` int	DEFAULT '0' COMMENT '金币',
  `star` int	DEFAULT '0' COMMENT '星',
  `heartTime`	bigint	DEFAULT '0' COMMENT '生命恢复倒计时',
  `level`	int DEFAULT '1' COMMENT '关卡', 
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

//账号系统 
CREATE TABLE `uid_bind` (
  `uid` varchar(40) COLLATE utf8_unicode_ci NOT NULL COMMENT '玩家id',
  `bindId` varchar(200) COLLATE utf8_unicode_ci NOT NULL COMMENT '绑定类型id',
  `type`	int DEFAULT '0' COMMENT '绑定类型 1设备 2.三方账号1比如fb 3...',
  `time`	bigint	DEFAULT '0' COMMENT '绑定时间',
  PRIMARY KEY (`bindId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--背包系统--
CREATE TABLE `user_item` (
  `uuid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `ownerId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `itemId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `count` int(11) NOT NULL DEFAULT '0',
  `value` int(11) DEFAULT NULL,
  `vanishTime` bigint(20) DEFAULT '0',
  PRIMARY KEY (`uuid`),
  KEY `uitem_ownerId_index` (`ownerId`,`itemId`,`value`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

//剧情
CREATE TABLE `user_story` (
  `uuid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `ownerId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `storyId` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `subId` int(11) NOT NULL DEFAULT '0',
  `type` int(11) DEFAULT NULL,
  `updateTime` bigint(20) DEFAULT '0',
  PRIMARY KEY (`uuid`),
  KEY `uitem_ownerId_index` (`ownerId`,`storyId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

