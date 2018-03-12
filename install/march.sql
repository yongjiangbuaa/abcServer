CREATE TABLE `userprofile` (
  `uid` varchar(40) COLLATE utf8_unicode_ci NOT NULL COMMENT '玩家id',
  `heart`int	DEFAULT  '0' COMMENT  '命',
  `gold` int	DEFAULT '0' COMMENT '金币',
  `star` int	DEFAULT '0' COMMENT '星',
  `heartTime`	bigint	DEFAULT '0' COMMENT '生命恢复倒计时',
  `level`	int DEFAULT '1' COMMENT '关卡',
  `name` varchar(150) CHARACTER SET utf8mb4 NOT NULL,
  `exp` bigint(20) NOT NULL DEFAULT '0',
  `paidGold` bigint(20) DEFAULT '0',
  `payTotal` bigint(20) NOT NULL DEFAULT '0',
  `parseRegisterId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `gcmRegisterId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `openedPos` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pic` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `picVer` int(11) DEFAULT '0',
  `country` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `allianceId` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `chNameCount` varchar(255) COLLATE utf8_unicode_ci DEFAULT '0',
  `worldPoint` int(11) DEFAULT '0',
  `deviceId` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `gaid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `platform` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pf` varchar(40) COLLATE utf8_unicode_ci DEFAULT 'market_global',
  `lang` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `mt` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `appVersion` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `gmFlag` int(11) DEFAULT '0',
  `regTime` bigint(20) NOT NULL DEFAULT '0',
  `offLineTime` bigint(20) DEFAULT '0',
  `banTime` bigint(20) DEFAULT '0',
  `chatBanTime` bigint(20) DEFAULT '0',
  `banGMName` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `noticeBanTime` bigint(20) DEFAULT '0',
  `lastOnlineTime` bigint(20) DEFAULT '0',
  `gmail` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `serverId` int(11) DEFAULT NULL,
  `crossFightSrcServerId` int(11) NOT NULL DEFAULT '-1',
  `lastModGoldGetTime` bigint(20) DEFAULT '0',
  `modGoldGetTimeInterval` bigint(20) DEFAULT '0',
  `modGoldAmount` bigint(10) DEFAULT '0',
  `beTrainingModTime` bigint(20) DEFAULT '0',
  `guideStep` varchar(255) COLLATE utf8_unicode_ci DEFAULT '',
  `payRiskFactor` int(11) DEFAULT '0',
  `phoneDevice` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `crystal` bigint(20) NOT NULL DEFAULT '0',
  `nationalFlag` varchar(40) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'CN',
  `dragonEggType` int(11) NOT NULL DEFAULT '0',
  `dragonEggDurationTime` bigint(20) NOT NULL DEFAULT '72000000' COMMENT '龙蛋孵化需要的持续时间',
  `lastAppVersion` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '上次登录的版本号',
  `bustPic` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lastLoginTime` bigint(20) NOT NULL DEFAULT '0',
  `curGaid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `isBusinessman` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是资源商',
  `dragonEndTime` bigint(20) NOT NULL DEFAULT '0',
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

DROP TABLE IF EXISTS `stat_reg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stat_reg` (
  `uid` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `type` int(4) NOT NULL DEFAULT '0',
  `time` bigint(20) NOT NULL,
  `pf` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pfId` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `referrer` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  `country` varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ip` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ipcountry` varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`uid`,`type`),
  KEY `index_time_pf_country` (`time`,`pf`,`country`) USING BTREE,
  KEY `country_index` (`country`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
 

