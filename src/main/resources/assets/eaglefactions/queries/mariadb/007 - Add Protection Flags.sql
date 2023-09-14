CREATE TABLE `protection_flag_type` (
    `id`            INT             NOT NULL   UNIQUE PRIMARY KEY,
    `flag_type`     VARCHAR(200)    NOT NULL
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE `faction_protection_flag` (
     `faction_name`               VARCHAR(200)    NOT NULL,
     `protection_flag_type_id`    INT             NOT NULL,
     `flag_value`                 TINYINT(1)      NOT NULL,
     UNIQUE INDEX `faction_protection_flag_UNIQUE` (`faction_name`, `protection_flag_type_id` ASC)
);

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