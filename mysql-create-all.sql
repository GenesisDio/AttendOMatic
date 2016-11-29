create table courses (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  section                       varchar(255),
  reference_id                  varchar(255),
  next_keycode                  varchar(255),
  current_keycode               varchar(255),
  keycode_open_time             datetime(6),
  keycode_expire_time           datetime(6),
  teacher_id                    bigint,
  sheet_id                      varchar(255),
  constraint uq_courses_reference_id unique (reference_id),
  constraint pk_courses primary key (id)
);

create table receipts (
  id                            integer auto_increment not null,
  student_id                    varchar(255),
  time_submitted                datetime(6),
  constraint pk_receipts primary key (id)
);

create table teachers (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  email                         varchar(255),
  id_token                      TEXT,
  constraint pk_teachers primary key (id)
);

alter table courses add constraint fk_courses_teacher_id foreign key (teacher_id) references teachers (id) on delete restrict on update restrict;
create index ix_courses_teacher_id on courses (teacher_id);

