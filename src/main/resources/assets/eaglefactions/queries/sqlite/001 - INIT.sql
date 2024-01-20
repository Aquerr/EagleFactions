CREATE TABLE version
(
    version INTEGER UNIQUE NOT NULL
);

-- Create Factions Table
CREATE TABLE faction
(
    name              TEXT PRIMARY KEY NOT NULL,
    tag               TEXT             NOT NULL,
    tag_color         TEXT             NULL,
    leader            TEXT             NOT NULL,
    home              TEXT             NULL,
    last_online       INTEGER          NOT NULL,
    description       TEXT             NOT NULL,
    motd              TEXT             NOT NULL,
    is_public         INTEGER          NOT NULL,
    created_date      INTEGER          NOT NULL,
    default_rank_name VARCHAR(36)      NOT NULL
);

-- Create Members Table
CREATE TABLE faction_member
(
    member_uuid  TEXT NOT NULL PRIMARY KEY,
    faction_name TEXT NOT NULL
);

-- Create Faction Ranks Table
CREATE TABLE faction_rank
(
    name            TEXT    NOT NULL,
    faction_name    TEXT    NOT NULL,
    display_name    TEXT,
    ladder_position INTEGER NOT NULL,
    display_in_chat INTEGER NOT NULL,
    PRIMARY KEY (name, faction_name)
);

-- Create Faction Rank Permissions
CREATE TABLE faction_rank_permission
(
    faction_name TEXT NOT NULL,
    rank_name    TEXT NOT NULL,
    permission   TEXT NOT NULL,
    PRIMARY KEY (faction_name, rank_name, permission)
);

-- Create Faction Member Rank Mapping Table
CREATE TABLE faction_member_rank
(
    member_uuid  TEXT NOT NULL,
    faction_name TEXT NOT NULL,
    rank_name    TEXT NOT NULL
);
--
-- Create Faction Relation Table
CREATE TABLE faction_relation
(
    faction_name_1 TEXT NOT NULL,
    faction_name_2 TEXT NOT NULL,
    relation_type  TEXT NOT NULL
);

-- Relation permissions
CREATE TABLE faction_relation_permission
(
    faction_name  TEXT NOT NULL,
    relation_type TEXT NOT NULL,
    permission    TEXT NOT NULL
);


-- Create Claims Table
CREATE TABLE claim
(
    faction_name             TEXT    NOT NULL,
    world_uuid               TEXT    NOT NULL,
    chunk_position           TEXT    NOT NULL,
    is_accessible_by_faction INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (world_uuid, chunk_position)
);

CREATE TABLE claim_owner
(
    world_uuid     TEXT NOT NULL,
    chunk_position TEXT NOT NULL,
    player_uuid    TEXT NOT NULL
);

-- Create FactionsChest Table
CREATE TABLE faction_chest
(
    faction_name TEXT PRIMARY KEY NOT NULL,
    chest_items  BINARY           NOT NULL
);

-- Create Players Table
CREATE TABLE player
(
    player_uuid      TEXT PRIMARY KEY NOT NULL,
    name             TEXT             NOT NULL,
    faction_name     TEXT             NULL,
    power            REAL             NOT NULL,
    max_power        REAL             NOT NULL,
    death_in_warzone INTEGER          NOT NULL
);


-- Protection Flags
CREATE TABLE protection_flag_type
(
    id        INTEGER NOT NULL,
    flag_type TEXT    NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE faction_protection_flag
(
    faction_name            TEXT    NOT NULL,
    protection_flag_type_id INTEGER NOT NULL,
    flag_value              INTEGER NOT NULL
);

INSERT INTO protection_flag_type VALUES (1, 'SPAWN_MONSTERS'),
       (2, 'SPAWN_ANIMALS'),
       (3, 'FIRE_SPREAD'),
       (4, 'ALLOW_EXPLOSION'),
       (5, 'MOB_GRIEF'),
       (6, 'PVP'),
       (7, 'TERRITORY_POWER_LOSS');


-- Set database version to 1
INSERT INTO version VALUES (1);