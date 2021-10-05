ALTER TABLE Factions ADD Description VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE Factions ADD Motd VARCHAR(255) NOT NULL DEFAULT '';

-- Set database version to 2
INSERT INTO Version VALUES (2);