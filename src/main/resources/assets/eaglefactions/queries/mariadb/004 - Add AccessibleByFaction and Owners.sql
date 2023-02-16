ALTER TABLE Claims ADD IsAccessibleByFaction TINYINT(1) NOT NULL DEFAULT TRUE;

CREATE TABLE `ClaimOwners` (
   `WorldUUID`     VARCHAR(36)   NOT NULL,
   `ChunkPosition` VARCHAR(200)  NOT NULL,
   `PlayerUUID`    VARCHAR(36)   NOT NULL,
   FOREIGN KEY (`WorldUUID`, `ChunkPosition`) REFERENCES `Claims` (`WorldUUID`, `ChunkPosition`),
   FOREIGN KEY (`PlayerUUID`) REFERENCES `Players` (`PlayerUUID`)
) DEFAULT CHARSET = utf8mb4;

-- Set database version to 4
INSERT INTO Version VALUES (4);