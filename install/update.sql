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
 

