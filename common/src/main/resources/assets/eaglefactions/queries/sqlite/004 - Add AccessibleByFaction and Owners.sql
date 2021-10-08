ALTER TABLE Claims ADD IsAccessibleByFaction INTEGER NOT NULL DEFAULT TRUE;

CREATE TABLE ClaimOwners (
   WorldUUID     TEXT   NOT NULL,
   ChunkPosition TEXT  NOT NULL,
   PlayerUUID    TEXT   NOT NULL,
   FOREIGN KEY (WorldUUID, ChunkPosition) REFERENCES Claims(WorldUUID, ChunkPosition) ON DELETE CASCADE,
   FOREIGN KEY (PlayerUUID) REFERENCES Players(PlayerUUID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- Set database version to 4
INSERT INTO Version VALUES (4);