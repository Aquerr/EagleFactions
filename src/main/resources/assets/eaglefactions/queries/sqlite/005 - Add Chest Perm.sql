ALTER TABLE OfficerPerms ADD Chest INTEGER NOT NULL DEFAULT 1;
ALTER TABLE MemberPerms ADD Chest INTEGER NOT NULL DEFAULT 1;
ALTER TABLE RecruitPerms ADD Chest INTEGER NOT NULL DEFAULT 0;

-- Set database version to 5
INSERT INTO Version VALUES (5);