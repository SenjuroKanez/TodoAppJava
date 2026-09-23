CREATE TABLE users (
 id VARCHAR(36) PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE,
 email VARCHAR(254) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL,
 theme VARCHAR(10) NOT NULL DEFAULT 'SYSTEM', timezone VARCHAR(80) NOT NULL DEFAULT 'UTC', created_at VARCHAR(40) NOT NULL
);
CREATE TABLE projects (
 id VARCHAR(36) PRIMARY KEY, owner_id VARCHAR(36) NOT NULL REFERENCES users(id),
 name VARCHAR(100) NOT NULL, color VARCHAR(7) NOT NULL, archived BOOLEAN NOT NULL DEFAULT FALSE,
 inbox BOOLEAN NOT NULL DEFAULT FALSE, version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE members (
 project_id VARCHAR(36) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 user_id VARCHAR(36) NOT NULL REFERENCES users(id), role VARCHAR(10) NOT NULL CHECK(role IN ('OWNER','EDITOR','VIEWER')),
 PRIMARY KEY(project_id,user_id)
);
CREATE INDEX members_user ON members(user_id,project_id);
CREATE TABLE invitations (
 id VARCHAR(36) PRIMARY KEY, project_id VARCHAR(36) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 user_id VARCHAR(36) NOT NULL REFERENCES users(id), role VARCHAR(10) NOT NULL CHECK(role IN ('EDITOR','VIEWER')),
 UNIQUE(project_id,user_id)
);
CREATE TABLE revision_clock (id INTEGER PRIMARY KEY, revision BIGINT NOT NULL);
INSERT INTO revision_clock VALUES(1,0);
CREATE TABLE tasks (
 id VARCHAR(36) PRIMARY KEY, project_id VARCHAR(36) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 title VARCHAR(200) NOT NULL, description TEXT NOT NULL, data_json TEXT NOT NULL,
 version BIGINT NOT NULL, revision BIGINT NOT NULL, deleted_at VARCHAR(40) NOT NULL DEFAULT ''
);
CREATE INDEX tasks_project ON tasks(project_id,revision);
CREATE TABLE changes (
 revision BIGINT NOT NULL, task_id VARCHAR(36) NOT NULL, project_id VARCHAR(36) NOT NULL,
 removed BOOLEAN NOT NULL DEFAULT FALSE, PRIMARY KEY(revision,task_id,project_id)
);
CREATE INDEX changes_project ON changes(project_id,revision);
CREATE TABLE operations (
 user_id VARCHAR(36) NOT NULL REFERENCES users(id), operation_id VARCHAR(36) NOT NULL,
 result_json TEXT NOT NULL, created_at VARCHAR(40) NOT NULL, PRIMARY KEY(user_id,operation_id)
);
CREATE TABLE notifications (
 id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36) NOT NULL REFERENCES users(id),
 project_id VARCHAR(36) NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
 task_id VARCHAR(36) NOT NULL DEFAULT '', message VARCHAR(500) NOT NULL,
 created_at VARCHAR(40) NOT NULL, read_at VARCHAR(40) NOT NULL DEFAULT '', dedup_key VARCHAR(200) NOT NULL UNIQUE
);
CREATE INDEX notifications_user ON notifications(user_id,created_at);
CREATE TABLE subscriptions (
 id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36) NOT NULL REFERENCES users(id),
 endpoint VARCHAR(2048) NOT NULL UNIQUE, p256dh VARCHAR(256) NOT NULL, auth VARCHAR(128) NOT NULL
);
CREATE TABLE push_deliveries (
 notification_id VARCHAR(36) NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
 subscription_id VARCHAR(36) NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
 attempts INTEGER NOT NULL DEFAULT 0, next_attempt VARCHAR(40) NOT NULL, delivered BOOLEAN NOT NULL DEFAULT FALSE,
 PRIMARY KEY(notification_id,subscription_id)
);
CREATE TABLE filters (
 id VARCHAR(36) PRIMARY KEY, user_id VARCHAR(36) NOT NULL REFERENCES users(id), name VARCHAR(80) NOT NULL, data_json TEXT NOT NULL
);
