-- // create store
-- Migration SQL that makes the change goes here.

CREATE TABLE `tbCart` (
  `inId` int(11) NOT NULL,
  `inSessionId` int(11) NOT NULL,
  `vcSessionCode` varchar(10) NOT NULL,
  `inUserId` int(11) DEFAULT NULL,
  `inProductVariationId` int(11) NOT NULL,
  `inSizeId` int(11) NOT NULL,
  `inQty` int(11) NOT NULL DEFAULT '1',
  `dtAdded` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`inId`,`inSessionId`,`vcSessionCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbCartCoupon` (
  `inSessionId` int(11) NOT NULL,
  `vcSessionCode` varchar(10) NOT NULL,
  `inUserId` int(11) NOT NULL,
  `inCouponClaimId` int(11) NOT NULL,
  `dtAdded` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`inSessionId`,`vcSessionCode`,`inCouponClaimId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tbCompany` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(200) NOT NULL,
  `inStatus` int(11) NOT NULL DEFAULT '0',
  `dtCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtActive` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbCompanyComment` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inCompanyId` int(11) NOT NULL,
  `inUserId` int(11) NOT NULL,
  `txComment` longtext NOT NULL,
  `dtCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`inId`),
  KEY `FK_tbCompanyComment_tbCompany` (`inCompanyId`),
  CONSTRAINT `FK_tbCompanyComment_tbCompany` FOREIGN KEY (`inCompanyId`) REFERENCES `tbCompany` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbContent` (
  `inid` int(11) NOT NULL AUTO_INCREMENT,
  `vcTitle` varchar(500) NOT NULL,
  `vcUrl` varchar(50) NOT NULL,
  `inAccessRequired` int(11) NOT NULL DEFAULT '0',
  `dtCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtRelease` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `txBlurb` longtext,
  `txBodyHtml` longtext,
  `txBodyText` longtext,
  PRIMARY KEY (`inid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbCountry` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(75) DEFAULT NULL,
  `vcNameProper` varchar(100) DEFAULT NULL,
  `vcLead` varchar(75) DEFAULT NULL,
  `vcCode` varchar(10) DEFAULT NULL,
  `vcPostalMask` varchar(20) DEFAULT NULL,
  `btActive` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inId`),
  KEY `ix_tbCountry_active` (`btActive`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbCoupon` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(100) DEFAULT NULL,
  `inType` int(11) DEFAULT NULL,
  `dtStart` datetime(6) DEFAULT NULL,
  `dtEnd` datetime(6) DEFAULT NULL,
  `dePercentOff` bigint(20) DEFAULT NULL,
  `moDollarOff` decimal(19,4) DEFAULT NULL,
  `moOver` decimal(19,4) DEFAULT NULL,
  `moUnder` decimal(19,4) DEFAULT NULL,
  `smBuy` smallint(6) DEFAULT NULL,
  `smGet` smallint(6) DEFAULT NULL,
  `smCategory` smallint(6) DEFAULT NULL,
  `txDesc` longtext,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbCouponClaim` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inCouponId` int(11) DEFAULT NULL,
  `vcClaimCode` varchar(20) NOT NULL,
  `dtClaimed` datetime(6) DEFAULT NULL,
  `dtUsed` datetime(6) DEFAULT NULL,
  `btExpire` tinyint(1) NOT NULL DEFAULT '0',
  `inClaimCount` int(11) NOT NULL DEFAULT '0',
  `inUsedCount` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inId`),
  KEY `FK_tbCouponClaim_tbCoupon` (`inCouponId`),
  CONSTRAINT `FK_tbCouponClaim_tbCoupon` FOREIGN KEY (`inCouponId`) REFERENCES `tbCoupon` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbGiftCertificateClaim` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcClaimCode` varchar(20) NOT NULL,
  `inIssuedBy` int(11) DEFAULT NULL,
  `inClaimedBy` int(11) DEFAULT NULL,
  `dtClaimed` datetime(6) DEFAULT NULL,
  `dtUsed` datetime(6) DEFAULT NULL,
  `dtExpires` datetime(6) DEFAULT NULL,
  `moDollarTotal` decimal(19,4) DEFAULT NULL,
  `moDollarSpent` decimal(19,4) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbHomepage` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcImage` varchar(100) NOT NULL,
  `vcLink` varchar(200) DEFAULT NULL,
  `inAccessRequired` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbHomepageItem` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcTitle` varchar(100) NOT NULL,
  `vcLink` varchar(150) DEFAULT NULL,
  `inAccessRequired` int(11) NOT NULL DEFAULT '0',
  `inRank` int(11) DEFAULT NULL,
  `txBlurb` longtext,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbIdOrder` (
  `inId` int(11) NOT NULL,
  `inStoreId` int(11) NOT NULL,
  `inUserId` int(11) NOT NULL,
  `vcRandom` varchar(10) NOT NULL,
  `dtTimestamp` varchar(50) NOT NULL,
  PRIMARY KEY (`inId`,`inStoreId`,`inUserId`,`vcRandom`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbIdTransaction` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inStoreId` int(11) NOT NULL,
  `inUserId` int(11) NOT NULL,
  `vcRandom` varchar(10) NOT NULL,
  `dtTimestamp` varchar(50) NOT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbLinkHomepageItemImage` (
  `inHomepageItemId` int(11) NOT NULL,
  `inImageId` int(11) NOT NULL,
  `vcLink` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`inHomepageItemId`,`inImageId`),
  KEY `FK_tbLinkHomepageItemImage_tbImage` (`inImageId`),
  CONSTRAINT `FK_tbLinkHomepageItemImage_tbHomepageItem` FOREIGN KEY (`inHomepageItemId`) REFERENCES `tbHomepageItem` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_tbLinkHomepageItemImage_tbImage` FOREIGN KEY (`inImageId`) REFERENCES `tbImage` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbLinkHomepageItemProductVariation` (
  `inHomepageItemId` int(11) NOT NULL,
  `inProductVariationId` int(11) NOT NULL,
  PRIMARY KEY (`inHomepageItemId`,`inProductVariationId`),
  KEY `FK_tbLinkHomepageItemProductVariation_tbProductVariation` (`inProductVariationId`),
  CONSTRAINT `FK_tbLinkHomepageItemProductVariation_tbHomepageItem` FOREIGN KEY (`inHomepageItemId`) REFERENCES `tbHomepageItem` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_tbLinkHomepageItemProductVariation_tbProductVariation` FOREIGN KEY (`inProductVariationId`) REFERENCES `tbProductVariation` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `tbOrder` (
  `vcId` varchar(20) NOT NULL,
  `inStoreId` int(11) NOT NULL,
  `inUserId` int(11) NOT NULL,
  `inStatus` int(11) DEFAULT NULL,
  `inBillId` int(11) DEFAULT '0',
  `inShipId` int(11) DEFAULT '0',
  `inGiftId` int(11) DEFAULT '0',
  `inShipMethodId` int(11) DEFAULT '0',
  `inAffiliateId` int(11) DEFAULT NULL,
  `vcPO` varchar(100) DEFAULT NULL,
  `dtCreated` datetime(6) DEFAULT NULL,
  `dtModified` datetime(6) DEFAULT NULL,
  `moSubtotal` decimal(19,4) DEFAULT NULL,
  `moTotal` decimal(19,4) DEFAULT NULL,
  `moTotal_Ship` decimal(19,4) DEFAULT NULL,
  `moTotal_Tax` decimal(19,4) DEFAULT NULL,
  `moTotal_Coupon_Product` decimal(19,4) DEFAULT NULL,
  `moTotal_Coupon_Order` decimal(19,4) DEFAULT NULL,
  `moTotal_Coupon_Ship` decimal(19,4) DEFAULT NULL,
  `moTotal_Credit` decimal(19,4) DEFAULT NULL,
  `moTotal_GiftCert` decimal(19,4) DEFAULT NULL,
  `moCharged_Subtotal` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `moCharged_Ship` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `moCharged_Tax` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `moCharged_Total` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `moCharged_Refund` decimal(19,4) DEFAULT NULL,
  `moApplied_Credit` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `moApplied_Coupon` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `moApplied_GiftCert` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `inShipCount` int(11) NOT NULL DEFAULT '0',
  `dtShipBegan` datetime(6) DEFAULT NULL,
  `dtShipComplete` datetime(6) DEFAULT NULL,
  `txComments` longtext,
  `vcIpAddress` varchar(20) DEFAULT NULL,
  `ftTax_Rate` double DEFAULT NULL,
  `inOrder_Coupon` int(11) DEFAULT NULL,
  `inShip_Coupon` int(11) DEFAULT NULL,
  `btOrder_Complete` tinyint(1) NOT NULL DEFAULT '0',
  `btDeleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`vcId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbOrderAddress` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcOrderId` varchar(20) NOT NULL,
  `inUserId` int(11) NOT NULL,
  `inType` int(11) DEFAULT NULL,
  `vcReference` varchar(30) DEFAULT NULL,
  `vcNameFirst` varchar(100) NOT NULL,
  `vcNameLast` varchar(100) NOT NULL,
  `vcTitle` varchar(100) DEFAULT NULL,
  `vcAddress1` varchar(100) DEFAULT NULL,
  `vcAddress2` varchar(100) DEFAULT NULL,
  `vcCity` varchar(50) DEFAULT NULL,
  `vcState` varchar(2) DEFAULT NULL,
  `vcZip` varchar(20) DEFAULT NULL,
  `inCountryId` int(11) DEFAULT NULL,
  `vcPhoneNumber` varchar(20) DEFAULT NULL,
  `vcFaxNumber` varchar(20) DEFAULT NULL,
  `btIsOffice` tinyint(1) NOT NULL DEFAULT '0',
  `inPreferredShipMethodId` int(11) DEFAULT NULL,
  PRIMARY KEY (`inId`),
  KEY `FK_tbOrderAddress_tbCountry` (`inCountryId`),
  CONSTRAINT `FK_tbOrderAddress_tbCountry` FOREIGN KEY (`inCountryId`) REFERENCES `tbCountry` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbOrderComment` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcOrderId` varchar(20) NOT NULL,
  `inUserId` int(11) DEFAULT NULL,
  `dtStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `boPrivate` tinyint(1) NOT NULL DEFAULT '0',
  `txBody` longtext,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbOrderDetail` (
  `inid` int(11) NOT NULL AUTO_INCREMENT,
  `vcOrderId` varchar(20) NOT NULL,
  `inProductVariationId` int(11) NOT NULL,
  `inSizeId` int(11) DEFAULT NULL,
  `vcSizeDesc` varchar(30) DEFAULT NULL,
  `inQty` int(11) NOT NULL,
  `moPriceOne` decimal(19,4) DEFAULT NULL,
  `moPriceTotal` decimal(19,4) DEFAULT NULL,
  `moCoupon_Applied` decimal(19,4) DEFAULT NULL,
  `inWeightOne` int(11) DEFAULT NULL,
  `inWeightTotal` int(11) DEFAULT NULL,
  `inShipmentId` int(11) DEFAULT NULL,
  `inPaymentId` int(11) DEFAULT NULL,
  `vcCouponIdBreakdown` varchar(100) DEFAULT NULL,
  `vcCouponBreakdown` varchar(200) DEFAULT NULL,
  `vcItemDesc` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`inid`),
  KEY `FK_tbOrderDetail_tbProductVariation` (`inProductVariationId`),
  CONSTRAINT `FK_tbOrderDetail_tbProductVariation` FOREIGN KEY (`inProductVariationId`) REFERENCES `tbProductVariation` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbPayment` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inUserId` int(11) NOT NULL,
  `vcType` varchar(4) NOT NULL,
  `inSuccess` int(11) NOT NULL DEFAULT '0',
  `vcOrderId` varchar(50) DEFAULT NULL,
  `moTotalHeld` decimal(19,4) DEFAULT NULL,
  `moTotalReturned` decimal(19,4) DEFAULT NULL,
  `moTotalSettled` decimal(19,4) DEFAULT NULL,
  `moTotalSettledShipping` decimal(19,4) DEFAULT NULL,
  `moTotalSettledSubtotal` decimal(19,4) DEFAULT NULL,
  `moTotalSettledTax` decimal(19,4) DEFAULT NULL,
  `vcAccountNumber` varchar(30) DEFAULT NULL,
  `vcEncAccountNumber` varchar(80) DEFAULT NULL,
  `vcCreditName` varchar(100) DEFAULT NULL,
  `vcEncCreditName` varchar(150) DEFAULT NULL,
  `inCardMonth` int(11) DEFAULT NULL,
  `inCardYear` int(11) DEFAULT NULL,
  `dtHeld` datetime(6) DEFAULT NULL,
  `dtSettled` datetime(6) DEFAULT NULL,
  `dtVoided` datetime(6) DEFAULT NULL,
  `moChargedSubtotal` decimal(19,4) DEFAULT NULL,
  `moChargedTax` decimal(19,4) DEFAULT NULL,
  `moChargedShipping` decimal(19,4) DEFAULT NULL,
  `moChargedTotal` decimal(19,4) DEFAULT NULL,
  `moReturnedSubtotal` decimal(19,4) DEFAULT NULL,
  `moReturnedTax` decimal(19,4) DEFAULT NULL,
  `moReturnedShipping` decimal(19,4) DEFAULT NULL,
  `moReturnedTotal` decimal(19,4) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbPaymentTransaction` (
  `inId` int(11) NOT NULL,
  `inPaymentId` int(11) NOT NULL,
  `inType` int(11) NOT NULL,
  `moAmount` decimal(19,4) DEFAULT NULL,
  `inResult` int(11) NOT NULL,
  `vcAuthCode` varchar(20) DEFAULT NULL,
  `vcAvsAddress` varchar(1) DEFAULT NULL,
  `vcAvsI` varchar(1) DEFAULT NULL,
  `vcAvsZip` varchar(1) DEFAULT NULL,
  `vcCvv2Match` varchar(1) DEFAULT NULL,
  `vcPnRef` varchar(30) DEFAULT NULL,
  `vcResponseMessage` varchar(500) DEFAULT NULL,
  `dtCreated` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `txComment` longtext,
  PRIMARY KEY (`inId`),
  KEY `FK_tbPaymentTransaction_tbPayment` (`inPaymentId`),
  CONSTRAINT `FK_tbPaymentTransaction_tbPayment` FOREIGN KEY (`inPaymentId`) REFERENCES `tbpayment` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbSession` (
  `inId` int(11) NOT NULL,
  `vcSessionCode` varchar(15) NOT NULL,
  `vcStore` varchar(50) NOT NULL,
  `wholesale` tinyint(1) DEFAULT '0',
  `inUserId` int(11) NOT NULL,
  `dtLogin` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `dtActive` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtExpire` datetime(6) DEFAULT NULL,
  `vcIp` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`inId`,`vcSessionCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbShipment` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcOrderId` varchar(20) NOT NULL,
  `inShipmentTypeId` int(11) NOT NULL,
  `dtCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `vcTrackingNumber` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`inId`),
  KEY `FK_tbShipment_tbOrder` (`vcOrderId`),
  CONSTRAINT `FK_tbShipment_tbOrder` FOREIGN KEY (`vcOrderId`) REFERENCES `tbOrder` (`vcId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbShipmentType` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbShipMethod` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcName` varchar(100) NOT NULL,
  `btWholesale` tinyint(1) NOT NULL DEFAULT '0',
  `btRetail` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbShipPrice` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inShipMethodId` int(11) NOT NULL,
  `moDollarMin` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `moDollarMax` decimal(19,4) NOT NULL DEFAULT '0.0000',
  `inWeightMin` int(11) NOT NULL DEFAULT '0',
  `inWeightMax` int(11) NOT NULL DEFAULT '0',
  `inItemMin` int(11) NOT NULL DEFAULT '0',
  `inItemMax` char(10) NOT NULL DEFAULT '0',
  `vcCalculation` varchar(200) NOT NULL,
  PRIMARY KEY (`inId`),
  KEY `FK_tbShipPrice_tbShipMethod` (`inShipMethodId`),
  CONSTRAINT `FK_tbShipPrice_tbShipMethod` FOREIGN KEY (`inShipMethodId`) REFERENCES `tbShipMethod` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbShipTerritory` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inShipMethodId` int(11) NOT NULL,
  `inCountryId` int(11) NOT NULL,
  `vcState` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`inId`),
  KEY `FK_tbShipTerritory_tbShipMethod` (`inShipMethodId`),
  CONSTRAINT `FK_tbShipTerritory_tbShipMethod` FOREIGN KEY (`inShipMethodId`) REFERENCES `tbShipMethod` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbState` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcAbbrev` varchar(2) NOT NULL,
  `vcName` varchar(100) NOT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbStore` (
  `inId` int(11) NOT NULL,
  `vcName` varchar(100) NOT NULL,
  `vcAbbrev` varchar(10) NOT NULL,
  `btWholesale` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbTax` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `vcState` varchar(2) DEFAULT NULL,
  `vcZip` varchar(20) DEFAULT NULL,
  `dePercent` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE `tbTax`;
DROP TABLE `tbStore`;
DROP TABLE `tbState`;
DROP TABLE `tbShipTerritory`;
DROP TABLE `tbShipPrice`;
DROP TABLE `tbShipMethod`;
DROP TABLE `tbShipmentType`;
DROP TABLE `tbShipment`;
DROP TABLE `tbSession`;
DROP TABLE `tbPaymentTransaction`;
DROP TABLE `tbPayment`;
DROP TABLE `tbOrderDetail`;
DROP TABLE `tbOrderComment`;
DROP TABLE `tbOrderAddress`;
DROP TABLE `tbOrder`;
DROP TABLE `tbLinkHomepageItemProductVariation`;
DROP TABLE `tbLinkHomepageItemImage`;
DROP TABLE `tbIdTransaction`;
DROP TABLE `tbIdOrder`;
DROP TABLE `tbHomepageItem`;
DROP TABLE `tbHomepage`;
DROP TABLE `tbGiftCertificateClaim`;
DROP TABLE `tbCouponClaim`;
DROP TABLE `tbCoupon`;
DROP TABLE `tbCountry`;
DROP TABLE `tbContent`;
DROP TABLE `tbCompanyComment`;
DROP TABLE `tbCompany`;
DROP TABLE `tbCartItem`;
DROP TABLE `tbCart`;
