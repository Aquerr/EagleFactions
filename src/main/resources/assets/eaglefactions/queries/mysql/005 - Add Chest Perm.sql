ALTER TABLE OfficerPerms ADD Chest BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE MemberPerms ADD Chest BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE RecruitPerms ADD Chest BOOLEAN NOT NULL DEFAULT FALSE;

-- Set database version to 5
INSERT INTO Version VALUES (5);