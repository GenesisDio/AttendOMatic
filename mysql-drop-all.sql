alter table courses drop foreign key fk_courses_teacher_id;
drop index ix_courses_teacher_id on courses;

drop table if exists courses;

drop table if exists receipts;

drop table if exists teachers;

