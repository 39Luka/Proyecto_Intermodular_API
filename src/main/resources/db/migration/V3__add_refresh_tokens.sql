-- Flyway migration V3: refresh tokens for access token rotation.

create table if not exists refresh_tokens (
    id bigint not null auto_increment,
    user_id bigint not null,
    token_hash varchar(64) not null,
    created_at datetime(6) not null,
    expires_at datetime(6) not null,
    revoked_at datetime(6),
    primary key (id),
    unique key uk_refresh_tokens_hash (token_hash),
    key idx_refresh_token_user (user_id),
    key idx_refresh_token_expires (expires_at),
    constraint fk_refresh_tokens_user
        foreign key (user_id) references users (id)
) engine=InnoDB;

