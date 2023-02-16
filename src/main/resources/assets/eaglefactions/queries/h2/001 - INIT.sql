CREATE TABLE Version (
    Version INT NOT NULL
);
CREATE UNIQUE INDEX ON Version (Version);

-- Create Factions Table
CREATE TABLE Factions (
   Name        VARCHAR(200)                    NOT NULL,
   Tag         VARCHAR(10)                     NOT NULL,
   TagColor    VARCHAR(40)                     NULL,
   Leader      VARCHAR(36)                     NOT NULL,
   Home        VARCHAR(200)                    NULL,
   LastOnline  VARCHAR(200)                    NOT NULL,
   PRIMARY KEY (Name)
);
CREATE UNIQUE INDEX ON Factions (Name);

-- Create Recruits Table
CREATE TABLE FactionRecruits (
    RecruitUUID     VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName     VARCHAR(200)    NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON FactionRecruits (RecruitUUID);

-- Create Members Table
CREATE TABLE FactionMembers (
    MemberUUID  VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName VARCHAR(200) NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON FactionMembers (MemberUUID);

-- Create Officers Table
CREATE TABLE FactionOfficers (
    OfficerUUID VARCHAR(36)    UNIQUE  NOT NULL,
    FactionName VARCHAR(200) NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON FactionOfficers (OfficerUUID);

-- Create FactionAlliances Table
CREATE TABLE FactionAlliances (
  FactionName_1 VARCHAR(200) NOT NULL,
  FactionName_2 VARCHAR(200) NOT NULL,
  FOREIGN KEY (FactionName_1) REFERENCES Factions(Name),
  FOREIGN KEY (FactionName_2) REFERENCES Factions(Name)
);

-- Create FactionEnemies Table
CREATE TABLE FactionEnemies (
  FactionName_1 VARCHAR(200) NOT NULL,
  FactionName_2 VARCHAR(200) NOT NULL,
  FOREIGN KEY (FactionName_1) REFERENCES Factions(Name),
  FOREIGN KEY (FactionName_2) REFERENCES Factions(Name)
);

-- Create FactionTruces Table
CREATE TABLE FactionTruces (
  FactionName_1 VARCHAR(200) NOT NULL,
  FactionName_2 VARCHAR(200) NOT NULL,
  FOREIGN KEY (FactionName_1) REFERENCES Factions(Name),
  FOREIGN KEY (FactionName_2) REFERENCES Factions(Name)
);

-- Create OfficerPerms Table
CREATE TABLE OfficerPerms (
   FactionName   VARCHAR(200)    UNIQUE        NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON OfficerPerms (FactionName);

-- Create MemberPerms Table
CREATE TABLE MemberPerms (
   FactionName   VARCHAR(200)      UNIQUE      NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON MemberPerms (FactionName);

-- Create RecruitPerms Table
CREATE TABLE RecruitPerms (
   FactionName   VARCHAR(200)   UNIQUE         NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   Claim       BOOLEAN                         NOT NULL,
   Attack      BOOLEAN                         NOT NULL,
   Invite      BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON RecruitPerms (FactionName);

-- Create AllyPerms Table
CREATE TABLE AllyPerms (
   FactionName   VARCHAR(200)    UNIQUE        NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON AllyPerms (FactionName);

-- Create TrucePerms Table
CREATE TABLE TrucePerms (
   FactionName   VARCHAR(200)    UNIQUE        NOT NULL,
   Use         BOOLEAN                         NOT NULL,
   Place       BOOLEAN                         NOT NULL,
   Destroy     BOOLEAN                         NOT NULL,
   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON TrucePerms (FactionName);

-- Create Claims Table
CREATE TABLE Claims (
   Id            INT AUTO_INCREMENT              NOT NULL,
   FactionName   VARCHAR(200)                  NOT NULL,
   WorldUUID     VARCHAR(36)                            NOT NULL,
   ChunkPosition VARCHAR(200)                  NOT NULL,
   PRIMARY KEY (WorldUUID, ChunkPosition),
   FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON Claims (WorldUUID, ChunkPosition);

-- Create FactionsChest Table
CREATE TABLE FactionChests (
    FactionName VARCHAR(200)    UNIQUE  NOT NULL,
    ChestItems  BINARY            NOT NULL,
    FOREIGN KEY (FactionName) REFERENCES Factions(Name)
);
CREATE UNIQUE INDEX ON FactionChests (FactionName);

-- Create Players Table
CREATE TABLE Players (
    PlayerUUID VARCHAR(36) PRIMARY KEY NOT NULL,
    Name    VARCHAR(200)    NOT NULL,
    Faction VARCHAR(200)    NULL,
    Power   REAL NOT NULL,
    MaxPower    REAL NOT NULL,
    DeathInWarzone BOOLEAN NOT NULL
);
CREATE UNIQUE INDEX ON Players (PlayerUUID);

-- Set database version to 1
INSERT INTO Version VALUES (1);