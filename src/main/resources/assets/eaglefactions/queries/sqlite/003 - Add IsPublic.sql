ALTER TABLE Factions ADD IsPublic INTEGER NOT NULL DEFAULT 0;

-- Set database version to 3
INSERT INTO Version VALUES (3);