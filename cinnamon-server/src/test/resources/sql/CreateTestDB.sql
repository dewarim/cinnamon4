CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  pwd VARCHAR(255) NOT NULL,
  obj_version int NOT NULL DEFAULT 0,
  login_type VARCHAR(64) NOT NULL DEFAULT 'CINNAMON',
  activated BOOLEAN NOT NULL DEFAULT TRUE 
);

create SEQUENCE seq_user_id start with 1;

INSERT INTO users(id,name,pwd,activated) VALUES ( nextval('seq_user_id'),'admin','$2a$10$VG9LCf6h/Qwb7Y.pafHkaepdnJNgFZUzzuMV3EcyvLbKnueHQ4IW.',true);

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

create table acls(
  id bigint PRIMARY KEY, 
  name varchar(255) UNIQUE 
);

create SEQUENCE seq_acl_id start with 1;
insert into acls(id,name) values(nextval('seq_acl_id'),'_default_acl');
insert into acls(id,name) values(nextval('seq_acl_id'),'reviewers.acl');

create table groups(
  id bigint PRIMARY KEY,
  name varchar(255) UNIQUE
);
create SEQUENCE seq_groups start with 1;
insert into groups(id,name) VALUES(nextval('seq_groups'),'_superusers');


create table group_users(
  user_id BIGINT NOT NULL ,
  group_id BIGINT NOT NULL 
);

create UNIQUE INDEX  group_users_user_group
  on group_users(user_id,group_id)
  ;

-- admin is member of superuser group:
insert into group_users VALUES(1,1);
