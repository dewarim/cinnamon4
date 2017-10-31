CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL ,
  pwd VARCHAR(255) NOT NULL 
);

create SEQUENCE seq_user_id start with 0;
INSERT INTO users VALUES ( nextval('seq_user_id'),'admin','admin');

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
