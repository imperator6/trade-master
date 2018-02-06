# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 192.168.0.167 (MySQL 5.5.5-10.0.31-MariaDB-0ubuntu0.16.04.2)
# Datenbank: trading_master
# Erstellt am: 2018-01-23 06:42:57 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Export von Tabelle positions
# ------------------------------------------------------------

CREATE TABLE `positions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `amount` decimal(25,10) DEFAULT NULL,
  `bot_id` int(11) NOT NULL,
  `buy_date` datetime DEFAULT NULL,
  `buy_fee` decimal(25,10) DEFAULT NULL,
  `buy_rate` decimal(25,10) DEFAULT NULL,
  `buy_signal_id` int(11) NOT NULL,
  `closed` bit(1) NOT NULL,
  `created` datetime NOT NULL,
  `error` bit(1) NOT NULL,
  `error_msg` varchar(255) DEFAULT NULL,
  `ext_sell_order_id` varchar(255) DEFAULT NULL,
  `extbuy_order_id` varchar(255) DEFAULT NULL,
  `hold_position` bit(1) NOT NULL,
  `market` varchar(255) NOT NULL,
  `result` decimal(25,10) DEFAULT NULL,
  `sell_date` datetime DEFAULT NULL,
  `sell_fee` decimal(25,10) DEFAULT NULL,
  `sell_rate` decimal(25,10) DEFAULT NULL,
  `signal_rate` decimal(25,10) NOT NULL,
  `status` varchar(255) NOT NULL,
  `total` decimal(25,10) DEFAULT NULL,
  `total_buy` decimal(25,10) DEFAULT NULL,
  `total_sell` decimal(25,10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `positions` WRITE;
/*!40000 ALTER TABLE `positions` DISABLE KEYS */;

INSERT INTO `positions` (`id`, `amount`, `bot_id`, `buy_date`, `buy_fee`, `buy_rate`, `buy_signal_id`, `closed`, `created`, `error`, `error_msg`, `ext_sell_order_id`, `extbuy_order_id`, `hold_position`, `market`, `result`, `sell_date`, `sell_fee`, `sell_rate`, `signal_rate`, `status`, `total`, `total_buy`, `total_sell`)
VALUES
	(1,8.6377950400,1,'2018-01-21 23:00:00',0.0000014900,0.0000692700,3,b'0','2018-01-22 19:22:44',b'0',NULL,NULL,'895ade1f-640d-4a33-a2e8-4456bb5317ca',b'0','BTC-MAID',1.8334055100,NULL,NULL,NULL,0.0000685400,'open',NULL,NULL,NULL),
	(2,10.3664410900,1,'2018-01-21 23:00:00',0.0000018700,0.0000723900,2,b'0','2018-01-22 19:22:44',b'0',NULL,NULL,'0fb19f09-beb5-483a-ab2d-a1cd0178e8a4',b'0','BTC-RISE',-0.9255422000,NULL,NULL,NULL,0.0000000000,'open',NULL,NULL,NULL);

/*!40000 ALTER TABLE `positions` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
