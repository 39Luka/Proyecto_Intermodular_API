-- Flyway migration V5: ensure products.version exists.
--
-- This is a defensive migration for environments that had the schema created/updated before optimistic locking
-- was introduced and where V2 might have been skipped or marked as applied.

set @has_version := (
    select count(*)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'products'
      and column_name = 'version'
);

set @sql := if(
    @has_version = 0,
    'alter table products add column version bigint not null default 0',
    'select 1'
);

prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

