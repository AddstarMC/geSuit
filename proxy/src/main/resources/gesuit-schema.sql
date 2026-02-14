-- MySQL dump 10.13  Distrib 5.5.40, for debian-linux-gnu (x86_64)

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bans`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bans` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `banned_playername` varchar(100) DEFAULT NULL,
  `banned_uuid` varchar(100) DEFAULT NULL,
  `banned_ip` varchar(15) DEFAULT NULL,
  `banned_by` varchar(100) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '0',
  `banned_on` datetime NOT NULL,
  `banned_until` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `banned_playername` (`banned_playername`),
  KEY `banned_uuid` (`banned_uuid`),
  KEY `banned_ip` (`banned_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `homes`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `homes` (
  `player` varchar(100) NOT NULL DEFAULT '',
  `home_name` varchar(100) NOT NULL DEFAULT '',
  `server` varchar(100) NOT NULL DEFAULT '',
  `world` varchar(100) DEFAULT NULL,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `z` double DEFAULT NULL,
  `yaw` float DEFAULT NULL,
  `pitch` float DEFAULT NULL,
  PRIMARY KEY (`player`,`home_name`,`server`),
  CONSTRAINT `homes_ibfk_1` FOREIGN KEY (`player`) REFERENCES `players` (`uuid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `players`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `players` (
  `playername` varchar(100) DEFAULT NULL,
  `uuid` varchar(100) NOT NULL DEFAULT '',
  `firstonline` datetime NOT NULL,
  `lastonline` datetime NOT NULL,
  `ipaddress` varchar(100) DEFAULT NULL,
  `tps` tinyint(1) DEFAULT '1',
  `newspawn` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `portals`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `portals` (
  `portalname` varchar(100) NOT NULL DEFAULT '',
  `server` varchar(100) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `destination` varchar(100) DEFAULT NULL,
  `world` varchar(100) DEFAULT NULL,
  `filltype` varchar(100) DEFAULT 'AIR',
  `xmax` int(11) DEFAULT NULL,
  `xmin` int(11) DEFAULT NULL,
  `ymax` int(11) DEFAULT NULL,
  `ymin` int(11) DEFAULT NULL,
  `zmax` int(11) DEFAULT NULL,
  `zmin` int(11) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`portalname`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `spawns`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `spawns` (
  `spawnname` varchar(100) NOT NULL DEFAULT '',
  `server` varchar(100) NOT NULL DEFAULT '',
  `world` varchar(100) DEFAULT NULL,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `z` double DEFAULT NULL,
  `yaw` float DEFAULT NULL,
  `pitch` float DEFAULT NULL,
  PRIMARY KEY (`spawnname`,`server`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tracking`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tracking` (
  `player` varchar(20) NOT NULL,
  `uuid` varchar(32) NOT NULL,
  `ip` varchar(15) NOT NULL,
  `firstseen` datetime NOT NULL,
  `lastseen` datetime NOT NULL,
  UNIQUE KEY `player` (`player`,`uuid`,`ip`),
  KEY `playerseen` (`player`,`lastseen`),
  KEY `ipseen` (`ip`,`lastseen`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warps`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `warps` (
  `warpname` varchar(100) NOT NULL DEFAULT '',
  `server` varchar(100) DEFAULT NULL,
  `world` varchar(100) DEFAULT NULL,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `z` double DEFAULT NULL,
  `yaw` float DEFAULT NULL,
  `pitch` float DEFAULT NULL,
  `hidden` tinyint(1) DEFAULT '0',
  `global` tinyint(1) DEFAULT '1',
  `description` varchar(128) DEFAULT NULL,
  PRIMARY KEY (`warpname`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
--
-- Table structure for table `ontime`
--
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ontime` (
  `uuid` varchar(32) NOT NULL,
  `timeslot` datetime NOT NULL,
  `time` int(10) unsigned NOT NULL,
  UNIQUE KEY `pair` (`uuid`,`timeslot`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
