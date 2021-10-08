
-- Create Version Table
CREATE TABLE `Version` (
  `Version` INT NOT NULL,
  PRIMARY KEY (`Version`)
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `Version_UNIQUE` ON `Version` (`Version`);

-- Create Factions Table
CREATE TABLE `Factions` (
  `Name` VARCHAR(200) NOT NULL,
  `Tag` VARCHAR(200) NOT NULL,
  `TagColor` VARCHAR(40) NULL,
  `Leader` VARCHAR(36) NOT NULL,
  `Home` VARCHAR(200) NULL,
  `LastOnline` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`Name`)
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `Name_UNIQUE` ON `Factions` (`Name`);

-- Create Recruits Table
CREATE TABLE `FactionRecruits` (
  `RecruitUUID` VARCHAR(36) NOT NULL,
  `FactionName` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `RecruitUUID_UNIQUE` ON `FactionRecruits` (`RecruitUUID`);

-- Create Members Table
CREATE TABLE `FactionMembers` (
  `MemberUUID` VARCHAR(36) NOT NULL,
  `FactionName` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `MemberUUID_UNIQUE` ON `FactionMembers` (`MemberUUID`);

-- Create Officers Table
CREATE TABLE `FactionOfficers` (
  `OfficerUUID` VARCHAR(36) NOT NULL,
  `FactionName` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `OfficerUUID_UNIQUE` ON `FactionOfficers` (`OfficerUUID`);

-- Create FactionAlliances Table
CREATE TABLE FactionAlliances (
  `FactionName_1` VARCHAR(200) NOT NULL,
  `FactionName_2` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName_1`) REFERENCES `Factions` (`Name`),
  FOREIGN KEY (`FactionName_2`) REFERENCES `Factions` (`Name`)
) DEFAULT CHARSET = utf8mb4;

-- Create FactionEnemies Table
CREATE TABLE FactionEnemies (
  `FactionName_1` VARCHAR(200) NOT NULL,
  `FactionName_2` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName_1`) REFERENCES `Factions` (`Name`),
  FOREIGN KEY (`FactionName_2`) REFERENCES `Factions` (`Name`)
) DEFAULT CHARSET = utf8mb4;

-- Create FactionTruces Table
CREATE TABLE FactionTruces (
  `FactionName_1` VARCHAR(200) NOT NULL,
  `FactionName_2` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName_1`) REFERENCES `Factions` (`Name`),
  FOREIGN KEY (`FactionName_2`) REFERENCES `Factions` (`Name`)
) DEFAULT CHARSET = utf8mb4;

-- Create OfficerPerms Table
CREATE TABLE `OfficerPerms` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  `Claim` TINYINT(1) NOT NULL,
  `Attack` TINYINT(1) NOT NULL,
  `Invite` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `OfficerPerms` (`FactionName`);

-- Create MemberPerms Table
CREATE TABLE `MemberPerms` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  `Claim` TINYINT(1) NOT NULL,
  `Attack` TINYINT(1) NOT NULL,
  `Invite` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `MemberPerms` (`FactionName`);

-- Create RecruitPerms Table
CREATE TABLE `RecruitPerms` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  `Claim` TINYINT(1) NOT NULL,
  `Attack` TINYINT(1) NOT NULL,
  `Invite` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `RecruitPerms` (`FactionName`);

-- Create AllyPerms Table
CREATE TABLE `AllyPerms` (
  `FactionName` VARCHAR(200) NOT NULL,
  `Use` TINYINT(1) NOT NULL,
  `Place` TINYINT(1) NOT NULL,
  `Destroy` TINYINT(1) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `AllyPerms` (`FactionName`);

-- Create TrucePerms Table
CREATE TABLE `TrucePerms` (
    `FactionName` VARCHAR(200) NOT NULL,
    `Use` TINYINT(1) NOT NULL,
    `Place` TINYINT(1) NOT NULL,
    `Destroy` TINYINT(1) NOT NULL,
    FOREIGN KEY (`FactionName`)
        REFERENCES `Factions` (`Name`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `TrucePerms` (`FactionName`);

-- Create Claims Table
CREATE TABLE `Claims` (
  `FactionName` VARCHAR(200) NOT NULL,
  `WorldUUID` VARCHAR(36) NOT NULL,
  `ChunkPosition` VARCHAR(200) NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
  PRIMARY KEY (`WorldUUID`, `ChunkPosition`)
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `Claim_UNIQUE` ON `Claims` (`WorldUUID`, `ChunkPosition`);

-- Create FactionsChest Table
CREATE TABLE `FactionChests` (
  `FactionName` VARCHAR(200) NOT NULL,
  `ChestItems` BLOB NOT NULL,
  FOREIGN KEY (`FactionName`)
      REFERENCES `Factions` (`Name`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `FactionName_UNIQUE` ON `FactionChests` (`FactionName`);

-- Create Players Table
CREATE TABLE `Players` (
  `PlayerUUID` VARCHAR(36) NOT NULL,
  `Name` VARCHAR(200) NOT NULL,
  `Faction` VARCHAR(200) NULL,
  `Power` FLOAT NOT NULL,
  `MaxPower` FLOAT NOT NULL,
  `DeathInWarzone` TINYINT(1) NOT NULL,
  PRIMARY KEY (`PlayerUUID`)
) DEFAULT CHARSET = utf8mb4;
CREATE UNIQUE INDEX `PlayerUUID_UNIQUE` ON `Players` (`PlayerUUID`);

-- Set database version to 1
INSERT INTO Version VALUES (1);