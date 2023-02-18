CREATE TABLE Version (
    Version INTEGER NOT NULL
);
CREATE UNIQUE INDEX idx_version ON Version (Version);

-- Create Factions Table
CREATE TABLE Factions (
   Name        TEXT PRIMARY KEY        NOT NULL,
   Tag         TEXT                     NOT NULL,
   TagColor    TEXT                     NULL,
   Leader      TEXT                     NOT NULL,
   Home        TEXT                    NULL,
   LastOnline  TEXT                    NOT NULL
);
CREATE UNIQUE INDEX idx_faction_name ON Factions (Name);

-- Create Recruits Table
CREATE TABLE FactionRecruits (
    RecruitUUID     TEXT    UNIQUE  NOT NULL,
    FactionName     TEXT    NOT NULL
);
CREATE UNIQUE INDEX idx_faction_recruit_uuid ON FactionRecruits (RecruitUUID);

-- Create Members Table
CREATE TABLE FactionMembers (
    MemberUUID  TEXT    UNIQUE  NOT NULL,
    FactionName TEXT NOT NULL
);
CREATE UNIQUE INDEX idx_faction_member_uuid ON FactionMembers (MemberUUID);

-- Create Officers Table
CREATE TABLE FactionOfficers (
    OfficerUUID TEXT    UNIQUE  NOT NULL,
    FactionName TEXT NOT NULL
);
CREATE UNIQUE INDEX idx_faction_officer_uuid ON FactionOfficers (OfficerUUID);

-- Create FactionAlliances Table
CREATE TABLE FactionAlliances (
    FactionName_1 TEXT   NOT NULL,
    FactionName_2 TEXT   NOT NULL
);

-- Create FactionEnemies Table
CREATE TABLE FactionEnemies (
  FactionName_1 TEXT   NOT NULL,
  FactionName_2 TEXT   NOT NULL
);

-- Create FactionTruces Table
CREATE TABLE FactionTruces (
  FactionName_1 TEXT   NOT NULL,
  FactionName_2 TEXT   NOT NULL
);

-- Create OfficerPerms Table
CREATE TABLE OfficerPerms (
   FactionName   TEXT    UNIQUE        NOT NULL,
   Use         INTEGER                         NOT NULL,
   Place       INTEGER                         NOT NULL,
   Destroy     INTEGER                         NOT NULL,
   Claim       INTEGER                         NOT NULL,
   Attack      INTEGER                         NOT NULL,
   Invite      INTEGER                         NOT NULL
);
CREATE UNIQUE INDEX idx_officer_perms_faction_name ON OfficerPerms (FactionName);

-- Create MemberPerms Table
CREATE TABLE MemberPerms (
   FactionName   TEXT      UNIQUE      NOT NULL,
   Use         INTEGER                         NOT NULL,
   Place       INTEGER                         NOT NULL,
   Destroy     INTEGER                         NOT NULL,
   Claim       INTEGER                         NOT NULL,
   Attack      INTEGER                         NOT NULL,
   Invite      INTEGER                         NOT NULL
);
CREATE UNIQUE INDEX idx_member_perms_faction_name ON MemberPerms (FactionName);

-- Create RecruitPerms Table
CREATE TABLE RecruitPerms (
   FactionName   TEXT   UNIQUE         NOT NULL,
   Use         INTEGER                         NOT NULL,
   Place       INTEGER                         NOT NULL,
   Destroy     INTEGER                         NOT NULL,
   Claim       INTEGER                         NOT NULL,
   Attack      INTEGER                         NOT NULL,
   Invite      INTEGER                         NOT NULL
);
CREATE UNIQUE INDEX idx_recruit_perms_faction_name ON RecruitPerms (FactionName);

-- Create AllyPerms Table
CREATE TABLE AllyPerms (
   FactionName   TEXT    UNIQUE        NOT NULL,
   Use         INTEGER                         NOT NULL,
   Place       INTEGER                         NOT NULL,
   Destroy     INTEGER                         NOT NULL
);
CREATE UNIQUE INDEX idx_ally_perms_faction_name ON AllyPerms (FactionName);

-- Create TrucePerms Table
CREATE TABLE TrucePerms (
   FactionName   TEXT    UNIQUE        NOT NULL,
   Use         INTEGER                         NOT NULL,
   Place       INTEGER                         NOT NULL,
   Destroy     INTEGER                         NOT NULL
);
CREATE UNIQUE INDEX idx_truce_perms_faction_name ON TrucePerms (FactionName);

-- Create Claims Table
CREATE TABLE Claims (
   FactionName   VARCHAR(200)                  NOT NULL,
   WorldUUID     VARCHAR(36)                            NOT NULL,
   ChunkPosition VARCHAR(200)                  NOT NULL,
   PRIMARY KEY (WorldUUID, ChunkPosition)
);
CREATE UNIQUE INDEX idx_claims_world_uuid_position ON Claims (WorldUUID, ChunkPosition);

-- Create FactionsChest Table
CREATE TABLE FactionChests (
    FactionName TEXT    UNIQUE  NOT NULL,
    ChestItems  BINARY            NOT NULL
);
CREATE UNIQUE INDEX idx_faction_chest_name ON FactionChests (FactionName);

-- Create Players Table
CREATE TABLE Players (
    PlayerUUID TEXT PRIMARY KEY,
    Name    TEXT    NOT NULL,
    Faction TEXT    NULL,
    Power   REAL NOT NULL,
    MaxPower    REAL NOT NULL,
    DeathInWarzone INTEGER NOT NULL
);
CREATE UNIQUE INDEX idx_player_uuid ON Players (PlayerUUID);

-- Set database version to 1
INSERT INTO Version VALUES (1);