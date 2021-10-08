ALTER TABLE Factions ADD Description TEXT NOT NULL DEFAULT '';
ALTER TABLE Factions ADD Motd TEXT NOT NULL DEFAULT '';

-- Set database version to 2
INSERT INTO Version VALUES (2);