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
  `min_result` decimal(25,10) DEFAULT NULL,
  `age` varchar(255) DEFAULT NULL,
  `last_know_base_currency_value` decimal(25,10) DEFAULT NULL,
  `last_know_rate` decimal(25,10) DEFAULT NULL,
  `last_update` datetime DEFAULT NULL,
  `settings` text DEFAULT NULL,
  `test` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=latin1;

-- Exportiere Daten aus Tabelle trading_master.positions: ~2 rows (ungefähr)
/*!40000 ALTER TABLE `positions` DISABLE KEYS */;
INSERT INTO `positions` (`id`, `amount`, `bot_id`, `buy_date`, `buy_fee`, `buy_rate`, `buy_signal_id`, `closed`, `created`, `error`, `error_msg`, `ext_sell_order_id`, `extbuy_order_id`, `market`, `result`, `sell_date`, `sell_fee`, `sell_rate`, `signal_rate`, `status`, `total`, `total_buy`, `total_sell`, `sell_in_pogress`, `trailing_stop_loss`, `max_result`, `min_result`, `age`, `last_know_base_currency_value`, `last_know_rate`, `last_update`, `settings`, `test`) VALUES
	(1, 8.6377950400, 2, '2018-01-22 19:24:37', 0.0000014900, 0.0000692700, 3, b'1', '2018-01-22 13:22:44', b'0', NULL, NULL, '895ade1f-640d-4a33-a2e8-4456bb5317ca', 'BTC-MAID', -13.0648188200, NULL, NULL, NULL, 0.0000685400, 'open', NULL, NULL, NULL, b'0', NULL, -7.0160242500, -16.7027573300, '7 Days', 0.0005201680, 0.0000602200, '2018-01-29 16:34:26', '{"stopLoss":{"enabled":false,"value":5}}', NULL),
	(2, 10.3664410900, 2, '2018-01-21 16:00:00', 0.0000018700, 0.0000723900, 2, b'0', '2018-01-22 12:22:44', b'0', NULL, NULL, '0fb19f09-beb5-483a-ab2d-a1cd0178e8a4', 'BTC-RISE', -14.2699267900, NULL, NULL, NULL, 0.0000000000, 'open', NULL, NULL, NULL, b'0', NULL, 1.8096422200, -14.8363033600, '8 Days', 0.0006433413, 0.0000620600, '2018-01-30 15:42:55', NULL, NULL);
/*!40000 ALTER TABLE `positions` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
