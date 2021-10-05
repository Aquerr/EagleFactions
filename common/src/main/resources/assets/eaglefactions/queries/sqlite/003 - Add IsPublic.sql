ALTER TABLE Factions ADD IsPublic BOOLEAN NOT NULL DEFAULT FALSE;

-- Set database version to 3
INSERT INTO Version VALUES (3);