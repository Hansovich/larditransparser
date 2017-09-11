create database larditransparser DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
create table offer (id varchar(30), htmlText varchar(2048) not null, sent TIMESTAMP null, primary key (id)) engine InnoDB;
CREATE USER 'larditransparser'@'localhost' IDENTIFIED BY 'larditransparser';
GRANT ALL ON larditransparser.* TO 'larditransparser'@'localhost';