CREATE TABLE app_info
(

  _id        INTEGER      NOT NULL PRIMARY KEY,
  db_version INTEGER      NOT NULL,
  name       VARCHAR(256) NOT NULL
);

CREATE TABLE device_type
(
  type VARCHAR(50) NOT NULL  PRIMARY KEY
);

CREATE TABLE transfer_type
(
  type VARCHAR(50) NOT NULL  PRIMARY KEY
);

INSERT INTO device_type VALUES ('MOBILE');
INSERT INTO device_type VALUES ('DESKTOP');

INSERT INTO transfer_type VALUES ('SENT');
INSERT INTO transfer_type VALUES ('RECEIVED');

INSERT INTO app_info VALUES (1, 1, 'unknown device');


CREATE TABLE device
(
  _id           INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
  name          VARCHAR(256) NOT NULL,
  display_name  VARCHAR(256) NOT NULL         DEFAULT 'Unknown Device',
  mac_address   VARCHAR(512) NOT NULL UNIQUE,
  device_type   VARCHAR(50)  NOT NULL,
  auth_hash     VARCHAR(512),
  last_known_ip VARCHAR(32)  NOT NULL,
  status        SMALLINT     NOT NULL         DEFAULT 0,
  last_access   INTEGER      NOT NULL,
  FOREIGN KEY (device_type) REFERENCES device_type (type) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE transfer
(
  _id              INTEGER     NOT NULL PRIMARY KEY AUTOINCREMENT,
  device_id        INTEGER     NOT NULL,
  transfer_type    VARCHAR(50) NOT NULL,
  file_name        INTEGER     NOT NULL,
  expected_size    INTEGER     NOT NULL,
  transferred_size INTEGER     NOT NULL,
  time             INTEGER     NOT NULL,
  status           SMALLINT    NOT NULL         DEFAULT 0,
  FOREIGN KEY (device_id) REFERENCES device (_id) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (transfer_type) REFERENCES transfer_type (type) ON DELETE NO ACTION ON UPDATE NO ACTION
);
