alter table userprofile add KEY `allianceid_lasttime_index` (`allianceId`,`lastOnlineTime`);
alter table userprofile add KEY `lang_index` (`lang`);
alter table userprofile add KEY `time_level_index` (`regTime`,`level`,`lastOnlineTime`,`pf`);
