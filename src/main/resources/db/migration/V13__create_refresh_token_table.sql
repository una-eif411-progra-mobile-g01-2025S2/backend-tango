CREATE TABLE IF NOT EXISTS refresh_token (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_token(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_token_hash ON refresh_token(token_hash);

-- Inicializa contraseñas existentes con formato bcrypt si aún están en texto plano
UPDATE app_user
SET password = CASE
    WHEN password LIKE '$2a$%' OR password LIKE '$2b$%' OR password LIKE '$2y$%' THEN password
    ELSE password
END;
