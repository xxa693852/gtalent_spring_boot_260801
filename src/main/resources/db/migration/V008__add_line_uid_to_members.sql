  ALTER TABLE members
      ADD COLUMN line_uid VARCHAR(64) NULL AFTER password,
      ADD UNIQUE KEY uk_members_line_uid (line_uid);