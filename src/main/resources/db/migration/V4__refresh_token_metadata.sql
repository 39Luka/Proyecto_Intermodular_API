alter table refresh_tokens
    add column last_used_at datetime(6) null,
    add column ip varchar(64) null,
    add column user_agent varchar(255) null;

create index idx_refresh_token_revoked on refresh_tokens (revoked_at);
