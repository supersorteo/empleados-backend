create table employees (
    id bigserial primary key,
    full_name varchar(120) not null,
    employee_code varchar(64) not null unique,
    username varchar(80) not null unique,
    email varchar(120),
    dni varchar(32),
    password_hash varchar(255) not null,
    role varchar(20) not null,
    active boolean not null default true,
    shift_start time,
    tolerance_minutes integer not null default 10,
    max_pause_minutes integer not null default 45,
    created_at timestamptz not null default now()
);

create table control_points (
    id bigserial primary key,
    name varchar(120) not null,
    active boolean not null default true
);

create table qr_tokens (
    token_id varchar(120) primary key,
    employee_id bigint not null references employees(id),
    requested_event_type varchar(30) not null,
    created_at timestamptz not null,
    expires_at timestamptz not null,
    used_at timestamptz,
    control_point_id bigint references control_points(id)
);

create table attendance_records (
    id bigserial primary key,
    employee_id bigint not null references employees(id),
    control_point_id bigint not null references control_points(id),
    event_type varchar(30) not null,
    event_time timestamptz not null,
    late boolean not null default false,
    excess_pause boolean not null default false,
    source varchar(40) not null
);

create index idx_attendance_employee_time on attendance_records(employee_id, event_time desc);
create index idx_attendance_time on attendance_records(event_time desc);
create index idx_qr_employee_created on qr_tokens(employee_id, created_at desc);
