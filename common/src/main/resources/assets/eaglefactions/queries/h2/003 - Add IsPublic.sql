ALTER TABLE Factions ADD IsPublic BOOLEAN NOT NULL DEFAULT FALSE AFTER Motd;

-- Set database version to 3
INSERT INTO Version VALUES (3);