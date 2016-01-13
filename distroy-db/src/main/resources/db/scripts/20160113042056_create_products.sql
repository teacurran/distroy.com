-- // create products
-- Migration SQL that makes the change goes here.

CREATE TABLE `tbArtist` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcNameFirst` varchar(100) DEFAULT NULL,
  `vcNameLast` varchar(100) DEFAULT NULL,
  `vcNameDisplay` varchar(200) DEFAULT NULL,
  `btActive` tinyint(1) NOT NULL DEFAULT '0',
  `moRoyaltyDollarRetail` decimal(19,4) DEFAULT NULL,
  `moRoyaltyDollarWholesale` decimal(19,4) DEFAULT NULL,
  `deRoyaltyPercentRetail` double DEFAULT NULL,
  `deRoyaltyPercentWholesale` double DEFAULT NULL,
  `dtCreated` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `dtModified` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `txDesc` longtext,
  `moRoyaltiesPaid` decimal(19,4) DEFAULT '0.0000',
  `moRoyaltiesOwed` decimal(19,4) DEFAULT '0.0000',
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbBrand` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(200) NOT NULL,
  `vcPageName` varchar(200) DEFAULT NULL,
  `vcLogo` varchar(50) DEFAULT NULL,
  `btActive` tinyint(1) NOT NULL DEFAULT '0',
  `txDesc` longtext,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbArtistRoyalties` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inArtistId` int(11) NOT NULL,
  `vcNameDisplay` varchar(200) DEFAULT NULL,
  `dtCheck` datetime(6) DEFAULT '0000-00-00 00:00:00.000000',
  `vcCheckNumber` varchar(10) DEFAULT NULL,
  `moCheckAmount` decimal(19,4) DEFAULT '0.0000',
  `dtCreated` datetime(6) DEFAULT NULL,
  `dtModified` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`inId`),
  KEY `FK_tbArtistRoyalties_tbArtist` (`inArtistId`),
  CONSTRAINT `FK_tbArtistRoyalties_tbArtist` FOREIGN KEY (`inArtistId`) REFERENCES `tbArtist` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbCategory` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inCategoryTypeId` int(11) DEFAULT NULL,
  `inRank` int(11) DEFAULT NULL,
  `vcName` varchar(100) DEFAULT NULL,
  `vcPath` varchar(300) DEFAULT NULL,
  `inParent` int(11) DEFAULT NULL,
  `btActive` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbImage` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inProductId` int(11) NOT NULL,
  `vcName` varchar(500) DEFAULT NULL,
  `vcNameOriginalThumb` varchar(500) DEFAULT NULL,
  `vcNameOriginalStandard` varchar(500) DEFAULT NULL,
  `vcNameOriginalEnlarge` varchar(500) DEFAULT NULL,
  `inOrientation` int(11) DEFAULT NULL,
  `inRank` int(11) DEFAULT NULL,
  `txDesc` longtext,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbProduct` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(200) NOT NULL,
  `vcSearchName` varchar(200) DEFAULT NULL,
  `vcSku` varchar(20) DEFAULT NULL,
  `btActiveForRetail` tinyint(1) NOT NULL DEFAULT '0',
  `btActiveForWholesale` tinyint(1) NOT NULL DEFAULT '0',
  `btTaxable` tinyint(1) NOT NULL DEFAULT '0',
  `inBrandId` int(11) NOT NULL,
  `dtCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtInStock` datetime(6) DEFAULT NULL,
  `txDesc` longtext,
  `txMetaDescription` longtext,
  `txTextDescription` longtext,
  PRIMARY KEY (`inId`),
  KEY `FK_tbProduct_tbBrand` (`inBrandId`),
  CONSTRAINT `FK_tbProduct_tbBrand` FOREIGN KEY (`inBrandId`) REFERENCES `tbBrand` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbProductVariation` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inProductId` int(11) DEFAULT NULL,
  `vcSku` varchar(20) NOT NULL,
  `vcStyle` varchar(100) DEFAULT NULL,
  `vcColor` varchar(100) DEFAULT NULL,
  `vcDesc` varchar(100) NOT NULL,
  `moPriceCost` decimal(19,4) NOT NULL,
  `moPriceRetail` decimal(19,4) NOT NULL,
  `moPriceRetailSale` decimal(19,4) DEFAULT NULL,
  `moPriceWholesale` decimal(19,4) NOT NULL,
  `moPriceWholesaleSale` decimal(19,4) DEFAULT NULL,
  `btSale` tinyint(1) DEFAULT '0',
  `btActive` tinyint(1) NOT NULL DEFAULT '0',
  `inRank` int(11) DEFAULT NULL,
  `txLongDesc` longtext,
  PRIMARY KEY (`inId`),
  KEY `FK_tbProductVariation_tbProduct` (`inProductId`),
  CONSTRAINT `FK_tbProductVariation_tbProduct` FOREIGN KEY (`inProductId`) REFERENCES `tbProduct` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbLinkProductArtist` (
  `inProductId` int(11) NOT NULL,
  `inArtistId` int(11) NOT NULL,
  `vcRelationship` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`inProductId`,`inArtistId`),
  CONSTRAINT `FK_tbLinkProductArtist_tbProduct` FOREIGN KEY (`inProductId`) REFERENCES `tbProduct` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbLinkProductVariationCategory` (
  `inProductVariationId` int(11) NOT NULL,
  `inCategoryId` int(11) NOT NULL,
  `inRank` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inProductVariationId`,`inCategoryId`),
  KEY `FK_tbLinkProductVariationCategory_tbCategory` (`inCategoryId`),
  CONSTRAINT `FK_tbLinkProductVariationCategory_tbCategory` FOREIGN KEY (`inCategoryId`) REFERENCES `tbCategory` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_tbLinkProductVariationCategory_tbProductVariation` FOREIGN KEY (`inProductVariationId`) REFERENCES `tbProductVariation` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbLinkProductVariationImage` (
  `inProductVariationId` int(11) NOT NULL,
  `inImageId` int(11) NOT NULL,
  `inRank` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inProductVariationId`,`inImageId`),
  KEY `FK_tbLinkProductVariationImage_tbImage` (`inImageId`),
  CONSTRAINT `FK_tbLinkProductVariationImage_tbImage` FOREIGN KEY (`inImageId`) REFERENCES `tbimage` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_tbLinkProductVariationImage_tbProductVariation` FOREIGN KEY (`inProductVariationId`) REFERENCES `tbProductVariation` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbSize` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(100) NOT NULL,
  `vcNameShort` varchar(10) DEFAULT NULL,
  `inRank` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbLinkProductVariationSize` (
  `inProductVariationid` int(11) NOT NULL,
  `inSizeId` int(11) NOT NULL,
  `inRank` int(11) NOT NULL DEFAULT '0',
  `inQtyInStock` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inProductVariationid`,`inSizeId`),
  KEY `FK_tbLinkProductVariationSize_tbSize` (`inSizeId`),
  CONSTRAINT `FK_tbLinkProductVariationSize_tbProductVariation` FOREIGN KEY (`inProductVariationid`) REFERENCES `tbProductVariation` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_tbLinkProductVariationSize_tbSize` FOREIGN KEY (`inSizeId`) REFERENCES `tbSize` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE `tbLinkProductVariationSize`;

DROP TABLE `tbLinkProductVariationImage`;
DROP TABLE `tbLinkProductVariationCategory`;
DROP TABLE `tbLinkProductArtist`;
DROP TABLE `tbProductVariation`;
DROP TABLE `tbProduct`;
DROP TABLE `tbImage`;
DROP TABLE `tbCategory`;
DROP TABLE `tbArtistRoyalties`;
DROP TABLE `tbBrand`;
DROP TABLE `tbArtist`;
