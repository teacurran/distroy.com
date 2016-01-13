-- // create marketing
-- Migration SQL that makes the change goes here.

CREATE TABLE `tbIndustryCategory` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inCategoryTypeId` int(11) DEFAULT NULL,
  `inRank` int(11) DEFAULT NULL,
  `vcName` varchar(100) DEFAULT NULL,
  `vcPath` varchar(300) DEFAULT NULL,
  `inParent` int(11) DEFAULT NULL,
  `btActive` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbIndustryCompany` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(150) DEFAULT NULL,
  `vcOwner` varchar(150) DEFAULT NULL,
  `vcEmail` varchar(150) DEFAULT NULL,
  `vcPassword` varchar(100) DEFAULT NULL,
  `vcAddress1` varchar(150) DEFAULT NULL,
  `vcAddress2` varchar(150) DEFAULT NULL,
  `vcCity` varchar(150) DEFAULT NULL,
  `vcState` varchar(2) DEFAULT NULL,
  `vcCountry` varchar(2) DEFAULT NULL,
  `vcUrl` varchar(150) DEFAULT NULL,
  `btStoreLocater` tinyint(1) NOT NULL DEFAULT '0',
  `vcEcommerce` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbLinkIndustryCategoryIndustryCompany` (
  `inIndustryCategoryId` int(11) NOT NULL,
  `inIndustryCompanyId` int(11) NOT NULL,
  `moPriceLow` decimal(19,4) DEFAULT NULL,
  `moPriceHigh` decimal(19,4) DEFAULT NULL,
  PRIMARY KEY (`inIndustryCategoryId`,`inIndustryCompanyId`),
  KEY `FK_tbLinkIndustryCategoryIndustryCompany_tbIndustryCompany` (`inIndustryCompanyId`),
  CONSTRAINT `FK_tbLinkIndustryCategoryIndustryCompany_tbIndustryCompany` FOREIGN KEY (`inIndustryCompanyId`) REFERENCES `tbIndustryCompany` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_tbLinkIndustryCategoryIndustryCompany_tbIndustryCategory` FOREIGN KEY (`inIndustryCategoryId`) REFERENCES `tbIndustryCategory` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tbMailingList` (
  `vcEmail` varchar(200) NOT NULL,
  `dtAdded` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `btSubscribed` tinyint(1) DEFAULT NULL,
  `dtPromotionSent` datetime(6) DEFAULT NULL,
  `dtPromotionClaimed` datetime(6) DEFAULT NULL,
  `dtPromotionUsed` datetime(6) DEFAULT NULL,
  `moPromotionUsed` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`vcEmail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tbRawMaterialVendor` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbRawMaterial` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inRawMaterialVendorId` int(11) DEFAULT NULL,
  `vcSku` varchar(50) DEFAULT NULL,
  `vcName` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`inId`),
  KEY `FK_tbRawMaterial_tbRawMaterialVendor` (`inRawMaterialVendorId`),
  CONSTRAINT `FK_tbRawMaterial_tbRawMaterialVendor` FOREIGN KEY (`inRawMaterialVendorId`) REFERENCES `tbRawMaterialVendor` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbRawMaterialVariation` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inRawMaterialId` int(11) NOT NULL,
  `vcColor` varchar(50) NOT NULL,
  `vcSizes` varchar(50) NOT NULL,
  `inDozPerCase` int(11) NOT NULL DEFAULT '0',
  `moPriceCase` decimal(19,4) NOT NULL,
  `moPriceDozen` decimal(19,4) NOT NULL,
  `moPricePiece` decimal(19,4) NOT NULL,
  PRIMARY KEY (`inId`),
  KEY `FK_tbRawMaterialVariation_tbRawMaterial` (`inRawMaterialId`),
  CONSTRAINT `FK_tbRawMaterialVariation_tbRawMaterial` FOREIGN KEY (`inRawMaterialId`) REFERENCES `tbRawMaterial` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE `tbRawMaterialVariation`;
DROP TABLE `tbRawMaterial`;
DROP TABLE `tbRawMaterialVendor`;
DROP TABLE `tbMailingList`;
DROP TABLE `tbLinkIndustryCategoryIndustryCompany`;
DROP TABLE `tbIndustryCompany`;
DROP TABLE `tbIndustryCategory`;


