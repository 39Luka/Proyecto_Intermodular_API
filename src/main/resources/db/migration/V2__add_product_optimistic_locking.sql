-- Flyway migration V2: add optimistic locking to products to prevent overselling under concurrent updates.

alter table products
    add column version bigint not null default 0;

