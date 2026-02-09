insert into categories (name, color) values ('仕事', '#0d6efd');
insert into categories (name, color) values ('プライベート', '#198754');
insert into categories (name, color) values ('緊急', '#dc3545');
insert into categories (name, color) values ('その他', '#fd7e14');

insert into users (username, password, roles, email, enabled) values ('user', '{noop}password', 'ROLE_USER', 'user@example.com', true);
insert into users (username, password, roles, email, enabled) values ('admin', '{noop}adminpass', 'ROLE_ADMIN', 'admin@example.com', true);

insert into groups (name, type, parent_id, color) values ('Company A', 'COMPANY', null, '#0d6efd');
insert into groups (name, type, parent_id, color) values ('Company B', 'COMPANY', null, '#198754');

insert into groups (name, type, parent_id, color)
select 'Sales', 'DEPARTMENT', id, '#0d6efd' from groups where name = 'Company A' and type = 'COMPANY';
insert into groups (name, type, parent_id, color)
select 'Development', 'DEPARTMENT', id, '#0d6efd' from groups where name = 'Company A' and type = 'COMPANY';
insert into groups (name, type, parent_id, color)
select 'Support', 'DEPARTMENT', id, '#198754' from groups where name = 'Company B' and type = 'COMPANY';

insert into groups (name, type, parent_id, color)
select 'Project Apollo', 'PROJECT', id, '#0d6efd' from groups where name = 'Sales' and type = 'DEPARTMENT';
insert into groups (name, type, parent_id, color)
select 'Project Orion', 'PROJECT', id, '#198754' from groups where name = 'Development' and type = 'DEPARTMENT';

insert into groups (name, type, parent_id, color) values ('Client X', 'CLIENT', null, '#dc3545');
insert into groups (name, type, parent_id, color) values ('Client Y', 'CLIENT', null, '#fd7e14');

insert into groups (name, type, parent_id, color)
select 'Project X-1', 'PROJECT', id, '#dc3545' from groups where name = 'Client X' and type = 'CLIENT';
insert into groups (name, type, parent_id, color)
select 'Project Y-1', 'PROJECT', id, '#fd7e14' from groups where name = 'Client Y' and type = 'CLIENT';

insert into groups (name, type, parent_id, color) values ('個人', 'PROJECT', null, '#6c757d');
