alter table employees
    add column if not exists created_by_admin_id bigint;

create index if not exists idx_employees_created_by_admin_id
    on employees(created_by_admin_id);

create table if not exists password_reset_tokens (
    token varchar(120) primary key,
    employee_id bigint not null references employees(id),
    expires_at timestamptz not null,
    used_at timestamptz,
    created_at timestamptz not null default now()
);

create index if not exists idx_password_reset_tokens_employee
    on password_reset_tokens(employee_id);

create index if not exists idx_password_reset_tokens_expires
    on password_reset_tokens(expires_at);

update employees
set created_by_admin_id = id
where role = 'ADMIN'
  and created_by_admin_id is null;
