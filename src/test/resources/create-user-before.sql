delete from user_role;
delete from usr;

insert into usr(id, active, password, username) values
(1, true, '$2y$12$JbeOUyYkSFok5nXGD/44KeEYBIyNcYhoE3O6btO.sE4l3VIhooNam', 'dru'),
(2, true, '$2y$12$JbeOUyYkSFok5nXGD/44KeEYBIyNcYhoE3O6btO.sE4l3VIhooNam', 'mike');

insert into user_role(user_id, roles) values
(1, 'USER'), (1, 'ADMIN'),
(2, 'USER');