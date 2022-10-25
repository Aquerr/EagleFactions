CREATE TABLE protection_flag_type (
    id            INTEGER         NOT NULL,
    flag_type     VARCHAR(200)    NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ON protection_flag_type (id);

CREATE TABLE faction_protection_flag (
     faction_name          VARCHAR(200)         NOT NULL,
     protection_flag_type_id    INTEGER         NOT NULL,
     flag_value BOOLEAN                         NOT NULL,
     FOREIGN KEY (faction_name) REFERENCES Factions(Name) ON DELETE CASCADE ON UPDATE CASCADE,
     FOREIGN KEY (protection_flag_type_id) REFERENCES protection_flag_type ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE UNIQUE INDEX ON faction_protection_flag (faction_name, protection_flag_type_id);

INSERT INTO protection_flag_type VALUES
(1, 'SPAWN_MONSTERS'),
(2, 'SPAWN_ANIMALS'),
(3, 'FIRE_SPREAD'),
(4, 'ALLOW_EXPLOSION'),
(5, 'MOB_GRIEF'),
(6, 'PVP'),
(7, 'TERRITORY_POWER_LOSS');

-- Set database version to 7
INSERT INTO Version VALUES (7);