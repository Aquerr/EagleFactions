CREATE TABLE Version (
    Version INT NOT NULL
);
CREATE UNIQUE INDEX Version_Version ON Version (Version);

-- Create Factions Table
CREATE TABLE Factions (
   Id          INT UNSIGNED AUTO_INCREMENT              NOT NULL,
   Name        VARCHAR(200)        UNIQUE      NOT NULL,
   Tag         VARCHAR(10)                     NOT NULL,
   TagColor    VARCHAR(40)                     NULL,
   Leader      VARCHAR(36)                     NOT NULL,
   Home        VARCHAR(200)                    NULL,
   LastOnline  VARCHAR(200)                    NOT NULL,
   Alliances    VARCHAR(255)                     NOT NULL,
   Enemies      VARCHAR(255)                     NOT NULL,
   PRIMARY KEY (Name)
);
CREATE UNIQUE INDEX Factions_Name ON Factions (Name);

-- Create Recruits Table
CREATE TABLE FactionRecruits (
    RecruitUUID     VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName     VARCHAR(200)    NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX FactionRecruits_RecruitUUID ON FactionRecruits (RecruitUUID);

-- Create Members Table
CREATE TABLE FactionMembers (
    MemberUUID  VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName VARCHAR(200)    UNIQUE  NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX FactionMembers_MemberUUID ON FactionMembers (MemberUUID);

-- Create Officers Table
CREATE TABLE FactionOfficers (
    OfficerUUID VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName VARCHAR(200)    UNIQUE  NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX FactionOfficers_OfficerUUID ON FactionOfficers (OfficerUUID);

-- Create FactionAlliances Table
-- CREATE TABLE FactionAlliances (
--   FactionName  VARCHAR(200)      UNIQUE        NOT NULL,
--   AlliancesIds VARCHAR(200)                    NOT NULL,
--   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
-- );
-- CREATE UNIQUE INDEX FactionAlliances_FactionName ON FactionAlliances (FactionName);
--
-- Create FactionEnemies Table
-- CREATE TABLE FactionEnemies (
--   FactionName VARCHAR(200)        UNIQUE      NOT NULL,
--   EnemiesIds  VARCHAR(200)                    NOT NULL,
--   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
-- );
-- CREATE UNIQUE INDEX FactionEnemies_FactionName ON FactionEnemies (FactionName);

-- Create FactionTruces Table
-- CREATE TABLE `FactionTruces` (
--   `FactionName`   VARCHAR(200)                             NOT NULL,
--   `TrucesIds`  VARCHAR(200)        UNIQUE      NOT NULL,
-- );
-- CREATE UNIQUE INDEX FactionTruces_FactionName ON FactionTruces (FactionName);

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
CREATE UNIQUE INDEX LeaderFlags_FactionName ON LeaderFlags (FactionName);

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
CREATE UNIQUE INDEX OfficerFlags_FactionName ON OfficerFlags (FactionName);

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
CREATE UNIQUE INDEX MemberFlags_FactionName ON MemberFlags (FactionName);

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
CREATE UNIQUE INDEX RecruitFlags_FactionName ON RecruitFlags (FactionName);

-- Create AllyFlags Table
CREATE TABLE AllyFlags (
   FactionName   VARCHAR(200)    UNIQUE        NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX AllyFlags_FactionName ON AllyFlags (FactionName);

-- Create Claims Table
CREATE TABLE Claims (
   Id            INT AUTO_INCREMENT              NOT NULL,
   FactionName   VARCHAR(200)                  NOT NULL,
   WorldUUID   VARCHAR(36)                           NOT NULL,
   ChunkPosition VARCHAR(200)                  NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX Claims_Id ON Claims (Id);

-- Create FactionsChest Table
CREATE TABLE FactionChests (
    FactionName VARCHAR(200)    UNIQUE  NOT NULL,
    ChestItems  BINARY            NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name) ON DELETE CASCADE
);
CREATE UNIQUE INDEX FactionChests_FactionName ON FactionChests (FactionName);

-- Create Players Table
CREATE TABLE Players (
    PlayerUUID VARCHAR(36) PRIMARY KEY NOT NULL,
    Name    VARCHAR(200)    NOT NULL,
    Power   REAL NOT NULL,
    Maxpower    REAL NOT NULL,
    DeathInWarzone BOOLEAN NOT NULL
);
CREATE UNIQUE INDEX Players_PlayerUUID ON Players (PlayerUUID);

-- Set database version to 1
INSERT INTO Version VALUES (1);