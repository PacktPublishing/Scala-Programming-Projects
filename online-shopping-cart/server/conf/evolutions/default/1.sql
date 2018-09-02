# --- !Ups
CREATE TABLE IF NOT EXISTS PUBLIC.PRODUCT (
  name VARCHAR(100) NOT NULL,
  code VARCHAR(255) NOT NULL,
  description VARCHAR(1000) NOT NULL,
  price DOUBLE NOT NULL,
  PRIMARY KEY(code)
);
INSERT INTO PUBLIC.PRODUCT (name,code, description, price) VALUES ('NAO','ALD1','NAO is an humanoid robot.', 3500);
INSERT INTO PUBLIC.PRODUCT (name,code, description, price) VALUES ('PEPPER','ALD2','PEPPER is a robot moving with wheels and with a screen as human interaction',7000);
INSERT INTO PUBLIC.PRODUCT (name,code, description, price) VALUES ('BEOBOT','BEO1','Beobot is a multipurpose robot.',159);


CREATE TABLE IF NOT EXISTS PUBLIC.CART (
  id IDENTITY AUTO_INCREMENT,
  user VARCHAR(255) NOT NULL,
  code VARCHAR(255) NOT NULL,
  qty INT NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT UC_CART UNIQUE (user,code)
);

# --- !Downs
DROP TABLE PRODUCT;
DROP TABLE CART;