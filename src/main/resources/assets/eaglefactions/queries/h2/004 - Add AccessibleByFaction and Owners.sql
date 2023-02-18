ALTER TABLE Claims ADD IsAccessibleByFaction BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE ClaimOwners (
   WorldUUID     VARCHAR(36)   NOT NULL,
   ChunkPosition VARCHAR(200)  NOT NULL,
   PlayerUUID    VARCHAR(36)   NOT NULL
);

-- Set database version to 4
INSERT INTO Version VALUES (4);