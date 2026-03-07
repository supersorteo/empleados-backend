alter table employees
    add column if not exists force_password_change boolean not null default true;

create table if not exists manual_adjustment_requests (
    id bigserial primary key,
    employee_id bigint not null references employees(id),
    requested_by_id bigint not null references employees(id),
    attendance_record_id bigint references attendance_records(id),
    requested_event_type varchar(30),
    requested_time timestamptz,
    reason varchar(400) not null,
    evidence_url varchar(300),
    status varchar(20) not null default 'PENDING',
    reviewer_id bigint references employees(id),
    reviewer_comment varchar(400),
    control_point_id bigint references control_points(id),
    created_at timestamptz not null default now(),
    reviewed_at timestamptz
);

create index if not exists idx_adjustment_status_created on manual_adjustment_requests(status, created_at desc);
create index if not exists idx_adjustment_employee_created on manual_adjustment_requests(employee_id, created_at desc);

create table if not exists audit_logs (
    id bigserial primary key,
    actor_id bigint references employees(id),
    action varchar(80) not null,
    entity_type varchar(80) not null,
    entity_id varchar(80) not null,
    details text,
    created_at timestamptz not null default now()
);

create index if not exists idx_audit_created on audit_logs(created_at desc);
