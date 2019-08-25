DROP DATABASE IF EXISTS ds_0;
CREATE DATABASE ds_0;
CREATE TABLE ds_0.order_0
(
    id               INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT NOT NULL,
    product_id       INT,
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);
CREATE TABLE ds_0.order_1
(
    id               INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT NOT NULL,
    product_id       INT,
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);
CREATE TABLE ds_0.order_2
(
    id               INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT NOT NULL,
    product_id       INT,
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);
DROP DATABASE IF EXISTS ds_1;
CREATE DATABASE ds_1;
CREATE TABLE ds_1.order_0
(
    id               INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT NOT NULL NOT NULL,
    product_id       INT,
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);
CREATE TABLE ds_1.order_1
(
    id               INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT NOT NULL,
    product_id       INT,
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);
CREATE TABLE ds_1.order_2
(
    id               INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT NOT NULL,
    product_id       INT,
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);
