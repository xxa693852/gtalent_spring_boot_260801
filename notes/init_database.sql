CREATE DATABASE project26_801
    DEFAULT CHARACTER SET = 'utf8mb4';

CREATE DATABASE project26_801_test
    DEFAULT CHARACTER SET = 'utf8mb4';

CREATE USER 'project26_801'@'%' IDENTIFIED BY '你的密碼';
-- Grant select privilege to all databases;
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
    ON project26_801.*
    TO 'project26_801'@'%';

CREATE USER 'project26_801_test'@'%' IDENTIFIED BY '123123';
-- Grant select privilege to all databases;
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX
    ON project26_801_test.*
    TO 'project26_801_test'@'%';
