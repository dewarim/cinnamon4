CREATE TABLE users (
  id BIGINT PRIMARY KEY,
  name VARCHAR(255) NOT NULL ,
  pwd VARCHAR(255) NOT NULL 
);

INSERT INTO users VALUES (1,'admin','admin');