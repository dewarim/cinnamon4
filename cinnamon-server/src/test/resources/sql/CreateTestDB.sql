CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  pwd VARCHAR(255) NOT NULL,
  obj_version int NOT NULL DEFAULT 0,
  login_type VARCHAR(64) NOT NULL DEFAULT 'CINNAMON'
);

create SEQUENCE seq_user_id start with 1;

INSERT INTO users(id,name,pwd) VALUES ( nextval('seq_user_id'),'admin','admin');

create table sessions
(
  id bigint PRIMARY KEY,
  expires timestamp,
  ticket varchar(255),
  username varchar(128),
  ui_language_id bigint,
  user_id bigint not null
)
;

create SEQUENCE seq_session_id start with 1;
