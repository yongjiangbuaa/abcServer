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

