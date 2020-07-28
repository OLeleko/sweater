insert into usr(id, username, password, active)
values(1, 'admin', '$2y$12$JbeOUyYkSFok5nXGD/44KeEYBIyNcYhoE3O6btO.sE4l3VIhooNam', true);

insert into user_role(user_id, roles)
values (1, 'USER'), (1, 'ADMIN');