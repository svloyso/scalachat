# --- !Ups

create table messages (
  id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  room TEXT NOT NULL,
  user TEXT NOT NULL,
  text TEXT NOT NULL,
  datetime BIGINT NOT NULL
);

# --- !Downs
drop table messages;
