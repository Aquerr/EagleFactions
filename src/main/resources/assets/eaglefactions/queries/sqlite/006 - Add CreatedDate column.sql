ALTER TABLE Factions ADD CreatedDate TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Set database version to 6
INSERT INTO Version VALUES (6);
