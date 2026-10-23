-- Shop E-commerce: dữ liệu hiện tại để nhập bằng phpMyAdmin của XAMPP.
-- CẢNH BÁO: file chứa DROP TABLE; chỉ import vào database shop_api mới/rỗng.
-- Giữ hồ sơ, catalog, đơn hàng và mật khẩu đã băm; không chuyển các phiên đăng nhập cũ.
CREATE DATABASE IF NOT EXISTS shop_api CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shop_api;
-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: 127.0.0.1    Database: shop_api
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(1000) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Clothing & Tools','Bàn phím'),(2,'Clothing & Movies','Âm thanh'),(3,'Baby & Music','Chuột & bàn di'),(4,'Games, Home & Music','Phụ kiện');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `customers` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `email` varchar(150) NOT NULL,
  `name` varchar(100) NOT NULL,
  `password_hash` varchar(100) DEFAULT NULL,
  `phone` varchar(16) DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrfbvkrffamfql7cjmen8v976v` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'456 Nguyen Trai','updated1792151053799857200@example.com','Smoke Updated',NULL,'0900000001','CUSTOMER'),(2,'123 Nguyen Trai, TP HCM','mysql1792153655557454200@example.com','Khach Thu MySQL',NULL,'0900000002','CUSTOMER'),(3,'123 Nguyen Trai, TP HCM','admin@shop.local','Quan tri vien','$2a$10$m/0bC3N83ILD.A.AHmiD8ORzf7QmTsjZQ7aavBfvoOCHbT9kDjQdO','0901000001','ADMIN'),(4,'123 Nguyễn Trãi, TP HCM','demo@shop.local','Nguyen Van An','$2a$10$FF7fvTdaNDNENSVNHRDz6OYs4tRTRhPPfvNKISww/kM2BvsqfJ1x6','0901000002','CUSTOMER'),(5,'123 Nguyễn Trãi, TP HCM','sample0@shop.local','Roger Conn','$2a$10$vfPZp0VGdE00zLmIRULjq.qNqe6LTpB1lM.E0OZ9h/7ImQPDAHn5m','0902000000','CUSTOMER'),(6,'123 Nguyễn Trãi, TP HCM','sample1@shop.local','Miss Arlen Kohler','$2a$10$s.LCwjYdIn1SrzjDw1X5eOUL/9WCSqCMV2z2u/UXbN/hM3AzC.rGa','0902000001','CUSTOMER'),(7,'123 Nguyễn Trãi, TP HCM','sample2@shop.local','Kasie Mraz','$2a$10$3L5vONFREJLTwVbY/yWBbOzPHxf6HZtbBK2eTiyjTfWm/QPdL2Fyu','0902000002','CUSTOMER'),(8,'123 Nguyễn Trãi, TP HCM','sample3@shop.local','Cleo Reinger Sr.','$2a$10$iR.WQu5ETs6.l/cUr8KsXubDbl0YAT66sX6sJ8kfHvwWQ0xHcZDE2','0902000003','CUSTOMER'),(9,'123 Nguyễn Trãi, TP HCM','sample4@shop.local','Kaci Goldner','$2a$10$TkB/c60/BlKjV6afgwGFqOD6aQGbBnpibUZwpm2Mm9EoNwYjnQowu','0902000004','CUSTOMER'),(10,'123 Nguyễn Trãi, TP HCM','sample5@shop.local','Dwayne Glover','$2a$10$N4hQYIIFiviVeOEqcR1LEeN9ntZiG.8L/xt2iqJdqIDTUeBGgcqFW','0902000005','CUSTOMER'),(11,'123 Nguyễn Trãi, TP HCM','sample6@shop.local','Elicia Nader II','$2a$10$De48lwSdRVNauFmh33Dtt.1vQ5MdFVKvfdVqojgcwR8kjfScYqRxC','0902000006','CUSTOMER'),(12,'123 Nguyễn Trãi, TP HCM','sample7@shop.local','Phil Rowe','$2a$10$6sALzwY8l1HT7Oh1K264J.0lN6GfJZx/WWaQqPDx/I1vdjGVGlxBa','0902000007','CUSTOMER'),(13,'123 Nguyen Trai','contract1792156599571@example.com','Contract Customer','$2a$10$aA17X8Q72vPrc03j6VPorud8lnH3vGfzD2sSaqHNHHEmUaaQtAJyC','0901234567','CUSTOMER'),(14,'123 Nguyen Trai','crud1792156599571@example.com','Contract Customer',NULL,'0901234567','CUSTOMER'),(16,'123 Nguyen Trai','contract1792157160810@example.com','Contract Customer','$2a$10$tL6RX8iLqVaK5YffoMNwr.dBVsBag4lom1dFDFLFW3lS9i4zgjwbG','0901234567','CUSTOMER'),(17,'123 Nguyen Trai','crud1792157160810@example.com','Contract Customer',NULL,'0901234567','CUSTOMER'),(19,'123 Nguyen Trai','smoke1792157268951798400@example.com','Smoke Customer','$2a$10$/VIK.o4W1YOf.vN5mbx80O/n.JO34w0THE2KDFTklNzEK3wSVZ.Bi','0901234567','CUSTOMER'),(20,'123 Nguyen Trai','xampp1792159706368543400@example.com','XAMPP Persistence',NULL,'0901234567','CUSTOMER');
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;

--
-- Table structure for table `order_lines`
--

DROP TABLE IF EXISTS `order_lines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_lines` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_name` varchar(255) NOT NULL,
  `quantity` int(11) NOT NULL,
  `subtotal` decimal(19,2) NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKid6v4r249u10sqki1gqr78fvb` (`order_id`),
  KEY `FK5v1oeejtgtf2n3toppm3tkuhh` (`product_id`),
  CONSTRAINT `FK5v1oeejtgtf2n3toppm3tkuhh` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKid6v4r249u10sqki1gqr78fvb` FOREIGN KEY (`order_id`) REFERENCES `shop_orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_lines`
--

/*!40000 ALTER TABLE `order_lines` DISABLE KEYS */;
INSERT INTO `order_lines` VALUES (1,'Ban phim co',2,1500000.00,750000.00,1,1),(2,'Ban phim MySQL',2,1500000.00,750000.00,2,3),(3,'Contract Keyboard',2,1500000.00,750000.00,3,20),(4,'Nova Mechanical K75',2,700000.00,350000.00,4,4),(5,'Nova Mechanical K75',1,350000.00,350000.00,5,4),(6,'Contract Keyboard',2,1500000.00,750000.00,6,22),(7,'Smoke Keyboard',2,1500000.00,750000.00,7,24),(8,'XAMPP Storage Test',1,125000.00,125000.00,8,25);
/*!40000 ALTER TABLE `order_lines` ENABLE KEYS */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(2000) DEFAULT NULL,
  `name` varchar(150) NOT NULL,
  `price` decimal(15,2) NOT NULL,
  `stock` int(11) NOT NULL,
  `version` bigint(20) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'USB keyboard','Ban phim co',750000.00,10,3,'/images/keyboard.svg',1),(3,'San pham kiem thu MySQL','Ban phim MySQL',750000.00,8,2,'/images/keyboard.svg',1),(4,'Thiết kế tối giản cho góc làm việc. Fugit rem itaque totam aliquam hic labore distinctio natus quam reiciendis perspiciatis.','Nova Mechanical K75',350000.00,16,4,'/images/keyboard.svg',1),(5,'Thiết kế tối giản cho góc làm việc. Optio exercitationem ut dolor adipisci eum est vel voluptatibus recusandae.','Studio Keys TKL',525000.00,20,0,'/images/keyboard.svg',1),(6,'Thiết kế tối giản cho góc làm việc. Dolorum asperiores ab nostrum velit minima minima repellendus omnis dignissimos animi nostrum esse voluptatem.','AirType Wireless',700000.00,25,0,'/images/keyboard.svg',1),(7,'Thiết kế tối giản cho góc làm việc. Tempore hic quia aliquam voluptatibus sed quas quis laboriosam et cupiditate earum consectetur vel animi.','Mono Keys Mini',875000.00,30,0,'/images/keyboard.svg',1),(8,'Thiết kế tối giản cho góc làm việc. Eos eaque incidunt ad vitae ipsam cumque voluptatem nobis corrupti ipsum blanditiis itaque qui voluptas.','Wave Pro Headphones',590000.00,15,0,'/images/headphones.svg',2),(9,'Thiết kế tối giản cho góc làm việc. Perferendis dicta earum incidunt quibusdam quaerat pariatur pariatur laudantium tenetur at corporis doloremque voluptate.','Studio Buds Air',765000.00,20,0,'/images/headphones.svg',2),(10,'Thiết kế tối giản cho góc làm việc. Ipsam pariatur quod expedita praesentium id ad et vitae reiciendis corrupti rem dolorum.','Soundbar Mini',940000.00,25,0,'/images/headphones.svg',2),(11,'Thiết kế tối giản cho góc làm việc. Earum eius laboriosam tenetur quaerat itaque laudantium quae temporibus praesentium quis aspernatur iure.','Echo Wireless',1115000.00,30,0,'/images/headphones.svg',2),(12,'Thiết kế tối giản cho góc làm việc. Eos vitae natus doloremque dolores maxime sunt vitae laborum asperiores vitae.','Orbit Mouse Pro',830000.00,15,0,'/images/mouse.svg',3),(13,'Thiết kế tối giản cho góc làm việc. Doloribus totam harum est commodi quisquam dolorum repudiandae maiores nisi consectetur placeat.','Glide Mouse Wireless',1005000.00,20,0,'/images/mouse.svg',3),(14,'Thiết kế tối giản cho góc làm việc. Neque eos ut qui quisquam doloribus sequi eos dignissimos accusamus non.','Desk Mat XL',1180000.00,25,0,'/images/mouse.svg',3),(15,'Thiết kế tối giản cho góc làm việc. Perspiciatis eos ratione quo asperiores vitae delectus tempore ex nulla aperiam voluptatem cum praesentium.','Precision Mouse',1355000.00,30,0,'/images/mouse.svg',3),(16,'Thiết kế tối giản cho góc làm việc. Distinctio adipisci quia odit nesciunt dicta natus fugiat laborum voluptatem perspiciatis perferendis temporibus sint.','Dock USB-C 7 in 1',1070000.00,15,0,'/images/accessories.svg',4),(17,'Thiết kế tối giản cho góc làm việc. Delectus beatae quibusdam pariatur omnis nisi consectetur numquam molestias in.','Laptop Stand Air',1245000.00,20,0,'/images/accessories.svg',4),(18,'Thiết kế tối giản cho góc làm việc. Voluptas perferendis nobis mollitia aspernatur placeat laboriosam reprehenderit natus tempore blanditiis.','Webcam Focus HD',1420000.00,25,0,'/images/accessories.svg',4),(19,'Thiết kế tối giản cho góc làm việc. Deserunt veritatis eius ut deleniti tempora dolorum fugiat fuga blanditiis voluptate a perferendis error voluptate.','Desk Light Halo',1595000.00,30,0,'/images/accessories.svg',4),(20,'Contract verification','Contract Keyboard',750000.00,8,1,'/images/keyboard.svg',1),(22,'Contract verification','Contract Keyboard',750000.00,8,1,'/images/keyboard.svg',1),(24,'Smoke test v2','Smoke Keyboard',750000.00,5,2,'/images/keyboard.svg',1),(25,'Kiem tra luu du lieu XAMPP','XAMPP Storage Test',125000.00,2,1,'/images/keyboard.svg',1);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;

--
-- Table structure for table `shop_orders`
--

DROP TABLE IF EXISTS `shop_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `shop_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `status` varchar(255) NOT NULL,
  `total` decimal(19,2) NOT NULL,
  `customer_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmiwbl5bhlprv7k06rg7uv8y3e` (`customer_id`),
  CONSTRAINT `FKmiwbl5bhlprv7k06rg7uv8y3e` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop_orders`
--

/*!40000 ALTER TABLE `shop_orders` DISABLE KEYS */;
INSERT INTO `shop_orders` VALUES (1,'2026-10-16 11:44:14.454175','CANCELLED',1500000.00,1),(2,'2026-10-16 12:27:36.760468','PENDING',1500000.00,2),(3,'2026-10-16 13:17:01.062119','PAID',1500000.00,14),(4,'2026-10-16 13:18:57.021967','CANCELLED',700000.00,4),(5,'2026-10-16 13:19:51.208555','SHIPPED',350000.00,4),(6,'2026-10-16 13:26:03.315333','PAID',1500000.00,17),(7,'2026-10-16 13:27:49.087543','CANCELLED',1500000.00,19),(8,'2026-10-16 14:08:27.003832','PENDING',125000.00,20);
/*!40000 ALTER TABLE `shop_orders` ENABLE KEYS */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-16 21:12:55

-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: 127.0.0.1    Database: shop_api
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account_sessions`
--

DROP TABLE IF EXISTS `account_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_sessions` (
  `token_hash` varchar(64) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `customer_id` bigint(20) NOT NULL,
  PRIMARY KEY (`token_hash`),
  KEY `FKocfe6t2870thypy6mj4xkoisy` (`customer_id`),
  CONSTRAINT `FKocfe6t2870thypy6mj4xkoisy` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-16 21:12:55
