insert into categories (name, color) values ('仕事', '#0d6efd');
insert into categories (name, color) values ('プライベート', '#198754');
insert into categories (name, color) values ('緊急', '#dc3545');
insert into categories (name, color) values ('その他', '#fd7e14');
insert into users (username, password, roles, email, enabled) values ('user', '{noop}password', 'ROLE_USER', 'user@example.com', true);
insert into users (username, password, roles, email, enabled) values ('admin', '{noop}adminpass', 'ROLE_ADMIN', 'admin@example.com', true);
