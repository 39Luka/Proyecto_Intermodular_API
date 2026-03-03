-- Flyway migration V6: simplify refresh token model.
-- Remove per-session metadata (IP/user-agent/last-used) to keep the model minimal while
-- preserving server-side revocation + rotation.

alter table refresh_tokens
    drop column last_used_at,
    drop column ip,
    drop column user_agent;

