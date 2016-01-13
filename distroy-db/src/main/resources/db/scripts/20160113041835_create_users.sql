-- // create users
-- Migration SQL that makes the change goes here.

CREATE TABLE `tbUser` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `smUserType` smallint(6) NOT NULL DEFAULT '0',
  `inCompanyId` int(11) DEFAULT NULL,
  `vcEmail` varchar(200) NOT NULL,
  `vcPassword` varchar(30) DEFAULT NULL,
  `btPasswordNoExpire` tinyint(1) NOT NULL DEFAULT '1',
  `dtPasswordLastReset` datetime(6) DEFAULT NULL,
  `dtPasswordExpires` datetime(6) DEFAULT NULL,
  `dtCreated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtModified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `dtLastLogin` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `btMailingList` tinyint(1) NOT NULL DEFAULT '0',
  `inBillId` int(11) DEFAULT NULL,
  `inShipId` int(11) DEFAULT NULL,
  PRIMARY KEY (`inId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbUserPref` (
  `inUserId` int(11) NOT NULL,
  `vcKey` varchar(100) NOT NULL,
  `vcValue` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`inUserId`,`vcKey`),
  CONSTRAINT `FK_tbUserPref_tbUser` FOREIGN KEY (`inUserId`) REFERENCES `tbUser` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tbAddress` (
  `inId` int(11) NOT NULL AUTO_INCREMENT,
  `inUserId` int(11) NOT NULL,
  `inType` int(11) NOT NULL DEFAULT '0',
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
  KEY `FK_tbAddress_tbUser` (`inUserId`),
  CONSTRAINT `FK_tbAddress_tbUser` FOREIGN KEY (`inUserId`) REFERENCES `tbUser` (`inId`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE `tbAddress`;
DROP TABLE `tbUserPref`;
DROP TABLE `tbUser`;
