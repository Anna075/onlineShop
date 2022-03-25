INSERT INTO user (city, number, street, zipcode, firstname, password, surname, username)
VALUES ('Bucuresti', 2, 'Lalelelor', '123', 'Admin first', 'password', 'lastName', 'adminUsername'),
       ('Timisoara', 21, 'Libertatii', '22', 'Client first', 'password2', 'lastName', 'clientUsername'),
       ('Iasi', 21, 'Unirii', '24', 'Client first', 'password5', 'lastName', 'expeditorUsername');
INSERT INTO user_roles values(1, 'ADMIN'),(1, 'EXPEDITOR');
INSERT INTO user_roles values(2, 'CLIENT');
INSERT INTO user_roles values(3, 'EXPEDITOR');