-- --------------------------------------------------------
-- Host:                         S930A3546
-- Server Version:               10.2.7-MariaDB - mariadb.org binary distribution
-- Server Betriebssystem:        Win64
-- HeidiSQL Version:             9.4.0.5174
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Exportiere Struktur von Tabelle trading_master.positions
CREATE TABLE IF NOT EXISTS `positions` (
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
  `sell_in_pogress` bit(1) NOT NULL,
  `trailing_stop_loss` decimal(19,2) DEFAULT NULL,
  `max_result` decimal(25,10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle trading_master.positions: ~0 rows (ungefähr)
/*!40000 ALTER TABLE `positions` DISABLE KEYS */;
INSERT INTO `positions` (`id`, `amount`, `bot_id`, `buy_date`, `buy_fee`, `buy_rate`, `buy_signal_id`, `closed`, `created`, `error`, `error_msg`, `ext_sell_order_id`, `extbuy_order_id`, `hold_position`, `market`, `result`, `sell_date`, `sell_fee`, `sell_rate`, `signal_rate`, `status`, `total`, `total_buy`, `total_sell`, `sell_in_pogress`, `trailing_stop_loss`, `max_result`) VALUES
	(1, 8.6377950400, 2, '2018-01-21 19:00:00', 0.0000014900, 0.0000692700, 3, b'0', '2018-01-22 15:22:44', b'0', NULL, NULL, '895ade1f-640d-4a33-a2e8-4456bb5317ca', b'0', 'BTC-MAID', -7.3913671100, NULL, NULL, NULL, 0.0000685400, 'open', NULL, NULL, NULL, b'0', NULL, -7.0160242500),
	(2, 10.3664410900, 2, '2018-01-21 19:00:00', 0.0000018700, 0.0000723900, 2, b'0', '2018-01-22 15:22:44', b'0', NULL, NULL, '0fb19f09-beb5-483a-ab2d-a1cd0178e8a4', b'0', 'BTC-RISE', 1.2570797100, NULL, NULL, NULL, 0.0000000000, 'open', NULL, NULL, NULL, b'0', NULL, 1.6576875300),
	(3, 6.4388663600, 2, '2018-01-21 19:00:00', 0.0000019700, 0.0001225200, 34, b'0', '2018-01-23 16:34:31', b'0', NULL, '', '5caa7705-915e-4db4-b395-eedd11cf1882', b'0', 'BTC-UNB', -3.1015344400, NULL, NULL, NULL, 0.0001200300, 'open', NULL, NULL, NULL, b'0', NULL, -2.0568070500);
/*!40000 ALTER TABLE `positions` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
