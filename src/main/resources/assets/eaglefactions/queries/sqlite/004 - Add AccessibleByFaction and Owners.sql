ALTER TABLE Claims ADD IsAccessibleByFaction INTEGER NOT NULL DEFAULT 1;

CREATE TABLE ClaimOwners (
   WorldUUID     TEXT   NOT NULL,
   ChunkPosition TEXT  NOT NULL,
   PlayerUUID    TEXT   NOT NULL
);

-- Set database version to 4
INSERT INTO Version VALUES (4);