-- Sequences allocate ids by blocks of 50 (Hibernate's pooled optimizer): no round trip per insert,
-- and inserts can be sent in JDBC batches, which IDENTITY columns prevent.
create sequence project_seq start with 1 increment by 50;
create sequence data_entity_seq start with 1 increment by 50;
create sequence attribute_seq start with 1 increment by 50;

create table project (
    id         bigint primary key,
    name       varchar(255)             not null unique,
    version    bigint                   not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

-- Sub-entities carry their project too: a whole project loads with one query, and names are unique per project
-- (they become table names in the SQL export).
create table data_entity (
    id         bigint primary key,
    project_id bigint       not null references project (id) on delete cascade,
    parent_id  bigint references data_entity (id) on delete cascade,
    name       varchar(255) not null,
    quantity   integer      not null check (quantity >= 0),
    version    bigint       not null,
    constraint uk_data_entity_project_name unique (project_id, name)
);

create index idx_data_entity_parent on data_entity (parent_id);

-- Type and constraints are a value owned by the attribute: one JSON column instead of three tables and joins.
create table attribute (
    id             bigint primary key,
    data_entity_id bigint       not null references data_entity (id) on delete cascade,
    name           varchar(255) not null,
    quantity       integer      not null,
    definition     json         not null,
    constraint uk_attribute_entity_name unique (data_entity_id, name)
);
