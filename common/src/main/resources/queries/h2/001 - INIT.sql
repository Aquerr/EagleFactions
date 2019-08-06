CREATE TABLE Version (
    Version INT NOT NULL
);
CREATE UNIQUE INDEX ON Version (Version);

-- Create Factions Table
CREATE TABLE Factions (
   Id          INT AUTO_INCREMENT              NOT NULL,
   Name        VARCHAR(200)        UNIQUE      NOT NULL,
   Tag         VARCHAR(10)                     NOT NULL,
   TagColor    VARCHAR(40)                     NULL,
   Leader      VARCHAR(36)                     NOT NULL,
   Home        VARCHAR(200)                    NULL,
   LastOnline  VARCHAR(200)                    NOT NULL,
   Alliances    VARCHAR                     NOT NULL,
   Enemies      VARCHAR                     NOT NULL,
   PRIMARY KEY (Name)
);
CREATE UNIQUE INDEX ON Factions (Name);

-- Create Recruits Table
CREATE TABLE FactionRecruits (
    RecruitUUID     VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName     VARCHAR(200)    NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON FactionRecruits (RecruitUUID);

-- Create Members Table
CREATE TABLE FactionMembers (
    MemberUUID  VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName VARCHAR(200)    UNIQUE  NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON FactionMembers (MemberUUID);

-- Create Officers Table
CREATE TABLE FactionOfficers (
    OfficerUUID VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName VARCHAR(200)    UNIQUE  NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON FactionOfficers (OfficerUUID);

---- Create FactionAlliances Table
--CREATE TABLE FactionAlliances (
--   FactionName  VARCHAR(200)      UNIQUE        NOT NULL,
--   AlliancesIds VARCHAR(200)                    NOT NULL,
--   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
--);
--CREATE UNIQUE INDEX ON FactionAlliances (FactionName);
--
---- Create FactionEnemies Table
--CREATE TABLE FactionEnemies (
--   FactionName VARCHAR(200)        UNIQUE      NOT NULL,
--   EnemiesIds  VARCHAR(200)                    NOT NULL,
--   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
--);
--CREATE UNIQUE INDEX ON FactionEnemies (FactionName);

---- Create FactionTruces Table
--CREATE TABLE `FactionTruces` (
--   `FactionName`   VARCHAR(200)                             NOT NULL,
--   `TrucesIds`  VARCHAR(200)        UNIQUE      NOT NULL,
--);
--CREATE UNIQUE INDEX ON FactionTruces (FactionName);

-- Create LeaderFlags Table
CREATE TABLE LeaderFlags (
   FactionName   VARCHAR(200)     UNIQUE       NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON LeaderFlags (FactionName);

-- Create OfficerFlags Table
CREATE TABLE OfficerFlags (
   FactionName   VARCHAR(200)    UNIQUE        NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON OfficerFlags (FactionName);

-- Create MemberFlags Table
CREATE TABLE MemberFlags (
   FactionName   VARCHAR(200)      UNIQUE      NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON MemberFlags (FactionName);

-- Create RecruitFlags Table
CREATE TABLE RecruitFlags (
   FactionName   VARCHAR(200)   UNIQUE         NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON RecruitFlags (FactionName);

-- Create AllyFlags Table
CREATE TABLE AllyFlags (
   FactionName   VARCHAR(200)    UNIQUE        NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON AllyFlags (FactionName);

-- Create Claims Table
CREATE TABLE Claims (
   Id            INT AUTO_INCREMENT              NOT NULL,
   FactionName   VARCHAR(200)                  NOT NULL,
   WorldUUID     VARCHAR(36)                            NOT NULL,
   ChunkPosition VARCHAR(200)                  NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON Claims (Id);

-- Create FactionsChest Table
CREATE TABLE FactionChests (
    FactionName VARCHAR(200)    UNIQUE  NOT NULL,
    ChestItems  BINARY            NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ON FactionChests (FactionName);

-- Create Players Table
CREATE TABLE Players (
    PlayerUUID VARCHAR(36) PRIMARY KEY NOT NULL,
    Name    VARCHAR(200)    NOT NULL,
    Power   REAL NOT NULL,
    MaxPower    REAL NOT NULL,
    DeathInWarzone BOOLEAN NOT NULL
);
CREATE UNIQUE INDEX ON Players (PlayerUUID);

-- Set database version to 1
INSERT INTO Version VALUES (1);