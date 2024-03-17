CREATE TABLE version
(
    version INT UNIQUE NOT NULL
);

-- Create Factions Table
CREATE TABLE faction
(
    name              VARCHAR(200) PRIMARY KEY NOT NULL,
    tag               VARCHAR(10)              NOT NULL,
    tag_color         VARCHAR(40)              NULL,
    leader            VARCHAR(36)              NULL,
    home              VARCHAR(200)             NULL,
    last_online       TIMESTAMP WITH TIME ZONE             NOT NULL,
    description       VARCHAR(255)             NOT NULL,
    motd              VARCHAR(255)             NOT NULL,
    is_public         BOOLEAN                  NOT NULL,
    created_date      TIMESTAMP WITH TIME ZONE                NOT NULL
);

-- Create Members Table
CREATE TABLE faction_member
(
    member_uuid  VARCHAR(36)  NOT NULL PRIMARY KEY,
    faction_name VARCHAR(200) NOT NULL
);

-- Create Faction Ranks Table
CREATE TABLE faction_rank
(
    name            VARCHAR(36)  NOT NULL,
    faction_name    VARCHAR(200) NOT NULL,
    display_name    VARCHAR(36),
    ladder_position INT          NOT NULL,
    display_in_chat BOOLEAN      NOT NULL,
    PRIMARY KEY (name, faction_name)
);

-- Create Faction Rank Permissions
CREATE TABLE faction_rank_permission
(
    faction_name VARCHAR(200) NOT NULL,
    rank_name    VARCHAR(36)  NOT NULL,
    permission   VARCHAR(36)  NOT NULL,
    PRIMARY KEY (faction_name, rank_name, permission)
);

-- Create Faction Member Rank Mapping Table
CREATE TABLE faction_member_rank
(
    member_uuid  VARCHAR(36)  NOT NULL,
    faction_name VARCHAR(200) NOT NULL,
    rank_name    VARCHAR(36)  NOT NULL
);

-- Create Faction Relation Table
CREATE TABLE faction_relation
(
    faction_name_1 VARCHAR(200) NOT NULL,
    faction_name_2 VARCHAR(200) NOT NULL,
    relation_type  VARCHAR(20)  NOT NULL
);

-- Relation permissions
CREATE TABLE faction_relation_permission
(
    faction_name  VARCHAR(200) NOT NULL,
    relation_type VARCHAR(20)  NOT NULL,
    permission    VARCHAR(36)  NOT NULL
);


-- Create Claims Table
CREATE TABLE claim
(
    faction_name             VARCHAR(200) NOT NULL,
    world_uuid               VARCHAR(36)  NOT NULL,
    chunk_position           VARCHAR(200) NOT NULL,
    is_accessible_by_faction BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (world_uuid, chunk_position)
);

CREATE TABLE claim_owner
(
    world_uuid     VARCHAR(36)  NOT NULL,
    chunk_position VARCHAR(200) NOT NULL,
    player_uuid    VARCHAR(36)  NOT NULL
);

-- Create FactionsChest Table
CREATE TABLE faction_chest
(
    faction_name VARCHAR(200) PRIMARY KEY NOT NULL,
    chest_items  VARBINARY                   NOT NULL
);

-- Create Players Table
CREATE TABLE player
(
    player_uuid      VARCHAR(36) PRIMARY KEY NOT NULL,
    name             VARCHAR(200)            NOT NULL,
    faction_name     VARCHAR(200)            NULL,
    power            REAL                    NOT NULL,
    max_power        REAL                    NOT NULL,
    death_in_warzone BOOLEAN                 NOT NULL
);


-- Protection Flags
CREATE TABLE protection_flag_type
(
    id        INTEGER PRIMARY KEY NOT NULL,
    flag_type VARCHAR(200)   NOT NULL
);

CREATE TABLE faction_protection_flag
(
    faction_name            VARCHAR(200) NOT NULL,
    protection_flag_type_id INTEGER      NOT NULL,
    flag_value              BOOLEAN      NOT NULL
);
CREATE UNIQUE INDEX ON faction_protection_flag (faction_name, protection_flag_type_id);

INSERT INTO protection_flag_type VALUES (1, 'SPAWN_MONSTERS'),
       (2, 'SPAWN_ANIMALS'),
       (3, 'FIRE_SPREAD'),
       (4, 'ALLOW_EXPLOSION'),
       (5, 'MOB_GRIEF'),
       (6, 'PVP'),
       (7, 'TERRITORY_POWER_LOSS');


-- Set database version to 1
INSERT INTO version VALUES (1);