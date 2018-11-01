CREATE TABLE Version (
    Version INT NOT NULL
);
CREATE UNIQUE INDEX ON Version (Version);

-- Create Factions Table
CREATE TABLE Factions (
   Id          INT AUTO_INCREMENT              NOT NULL,
   Name        VARCHAR(200)        UNIQUE      NOT NULL,
   Tag         VARCHAR(10)                     NOT NULL,
   TagColor    VARCHAR(40)                     NOT NULL,
   Leader      VARCHAR(36)                     NOT NULL,
   Home        VARCHAR(200)                    NULL,
   LastOnline  VARCHAR(200)                    NOT NULL,
   PRIMARY KEY (Id)
);
CREATE UNIQUE INDEX ON Factions (Name);

-- Create FactionAlliances Table
CREATE TABLE FactionAlliances (
   FactionId    INT                             NOT NULL,
   AlliancesIds VARCHAR(200)                    NOT NULL
);
CREATE UNIQUE INDEX ON FactionAlliances (FactionId);

-- Create FactionEnemies Table
CREATE TABLE FactionEnemies (
   FactionId   INT                             NOT NULL,
   EnemiesIds  VARCHAR(200)                    NOT NULL
);
CREATE UNIQUE INDEX ON FactionEnemies (FactionId);

---- Create FactionTruces Table
--CREATE TABLE `FactionTruces` (
--   `FactionId`   INT                             NOT NULL,
--   `TrucesIds`  VARCHAR(200)        UNIQUE      NOT NULL,
--);
--CREATE UNIQUE INDEX ON `FactionEnemies` (`FactionId`);

-- Create LeaderFlags Table
CREATE TABLE LeaderFlags (
   FactionId   INT                             NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL
);
CREATE UNIQUE INDEX ON LeaderFlags (FactionId);

-- Create OfficerFlags Table
CREATE TABLE OfficerFlags (
   FactionId   INT                             NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL
);
CREATE UNIQUE INDEX ON OfficerFlags (`FactionId`);

-- Create MemberFlags Table
CREATE TABLE MemberFlags (
   FactionId   INT                             NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL
);
CREATE UNIQUE INDEX ON MemberFlags (FactionId);

-- Create RecruitFlags Table
CREATE TABLE RecruitFlags (
   FactionId   INT                             NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL
);
CREATE UNIQUE INDEX ON `RecruitFlags` (`FactionId`);

-- Create AllyFlags Table
CREATE TABLE AllyFlags (
   FactionId   INT                             NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL
);
CREATE UNIQUE INDEX ON AllyFlags (FactionId);

-- Set database version to 1
INSERT INTO Version VALUES (1);