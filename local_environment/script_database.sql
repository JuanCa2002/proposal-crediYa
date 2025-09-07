-------------------- TABLES -----------------------

-- Table states
CREATE TABLE IF NOT EXISTS states (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

-- Default value of States table
INSERT INTO states (name, description)
VALUES ('PENDIENTE_REVISION', 'Pendiente de Revisión')
ON CONFLICT (name) DO NOTHING;

-- Table proposal_types
CREATE TABLE IF NOT EXISTS proposal_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    minimum_amount NUMERIC(12,2) NOT NULL,
    maximum_amount NUMERIC(12,2) NOT NULL,
    interest_rate NUMERIC(3,2) NOT NULL,
    automatic_validation BOOLEAN
);

-- Table proposals
CREATE TABLE IF NOT EXISTS proposals (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(12,2) NOT NULL,
    base_salary NUMERIC(12,2) NOT NULL,
    monthly_fee NUMERIC(12,2),
    proposal_limit INTEGER NOT NULL,
    limit_date DATE NOT NULL,
    creation_date DATE NOT NULL,
    email VARCHAR(255) NOT NULL,
    state_id BIGINT NOT NULL,
    proposal_type_id BIGINT NOT NULL,
    CONSTRAINT fk_proposals_state FOREIGN KEY (state_id) REFERENCES states(id),
    CONSTRAINT fk_proposals_type FOREIGN KEY (proposal_type_id) REFERENCES proposal_types(id)
);
