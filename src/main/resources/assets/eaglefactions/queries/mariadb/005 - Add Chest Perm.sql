ALTER TABLE OfficerPerms ADD Chest TINYINT(1) NOT NULL DEFAULT TRUE;
ALTER TABLE MemberPerms ADD Chest TINYINT(1) NOT NULL DEFAULT TRUE;
ALTER TABLE RecruitPerms ADD Chest TINYINT(1) NOT NULL DEFAULT FALSE;

-- Set database version to 5
INSERT INTO Version VALUES (5);