ALTER TABLE Claims ADD IsAccessibleByFaction BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE `ClaimOwners` (
   `WorldUUID`     VARCHAR(36)   NOT NULL,
   `ChunkPosition` VARCHAR(200)  NOT NULL,
   `PlayerUUID`    VARCHAR(36)   NOT NULL,
   CONSTRAINT `ClaimOwners_Claim` FOREIGN KEY (`WorldUUID`, `ChunkPosition`) REFERENCES `Claims` (`WorldUUID`, `ChunkPosition`) ON DELETE CASCADE,
   CONSTRAINT `ClaimOwners_PlayerUUID` FOREIGN KEY (`PlayerUUID`) REFERENCES `Players` (`PlayerUUID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) DEFAULT CHARSET = utf8mb4;

-- Set database version to 4
INSERT INTO Version VALUES (4);