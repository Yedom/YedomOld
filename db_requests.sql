create database yetube;

CREATE TABLE Users (
	id serial PRIMARY KEY,
	username VARCHAR (50) UNIQUE NOT NULL,
	password VARCHAR (255) NOT NULL,
	email VARCHAR (255) UNIQUE NOT NULL,
	role VARCHAR (255) NOT NULL,
	created_on TIMESTAMP NOT NULL,
	last_login TIMESTAMP
);

CREATE TABLE Roles (
	id serial PRIMARY KEY,
	role VARCHAR (50) UNIQUE NOT NULL,
	inherits TEXT,
	permissions TEXT
);
