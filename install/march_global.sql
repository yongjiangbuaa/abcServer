CREATE TABLE `usermapping` (
  `gameUid` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `mappingType` varchar(20) COLLATE utf8_unicode_ci NOT NULL DEFAULT '',
  `mappingValue` varchar(150) CHARACTER SET utf8mb4 DEFAULT NULL,
  `lastTime` bigint(20) DEFAULT '0',
  `active` int(11) DEFAULT '0',
  PRIMARY KEY (`gameUid`,`mappingType`),
  KEY `pf_pi_lasttime_index` (`mappingType`,`mappingValue`,`lastTime`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `account_new` (
  `gameUid` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `server` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `uuid` varchar(100) COLLATE utf8_unicode_ci NOT NULL,
  `deviceId` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `gaid` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `country` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `emailAccount` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `googleAccount` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `googleAccountName` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `facebookAccount` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `facebookAccountName` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pf` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pfId` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `gameUserName` varchar(150) CHARACTER SET utf8mb4 DEFAULT NULL,
  `gameUserLevel` int(11) NOT NULL,
  `lastTime` bigint(20) NOT NULL,
  `emailConfirm` int(4) DEFAULT '0',
  `passmd5` varchar(40) COLLATE utf8_unicode_ci DEFAULT NULL,
  `active` int(11) DEFAULT '0',
  PRIMARY KEY (`gameUid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `server_info` (
  `id` int(10) NOT NULL,
  `type` int(4) NOT NULL,
  `cross_fight_server_id` int(10) DEFAULT NULL,
  `daoliangStart` bigint(20) DEFAULT '0',
  `daoliangEnd` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
