--Table States
CREATE TABLE states (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
);

-- Default value of States table
INSERT INTO states (name, description)
VALUES ("PENDIENTE_REVISION", "Pendiente de Revisión");

--Table Proposal Types
CREATE TABLE proposal_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    minimum_amount NUMERIC(12,2) NOT NULL,
    maximum_amount NUMERIC(12,2) NOT NULL,
    interest_rate NUMERIC(3,2) NOT NULL,
    automatic_validation BOOLEAN
);

-- Table Proposals
CREATE TABLE proposals (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(12,2) NOT NULL,
    base_salary NUMERIC(12,2) NOT NULL,
    proposal_limit INTEGER NOT NULL,
    limit_date DATE NOT NULL,
    creation_date DATE NOT NULL,
    email VARCHAR(255) NOT NULL,
    state_id BIGINT NOT NULL,
    proposal_type_id BIGINT NOT NULL,
    CONSTRAINT fk_proposals_state FOREIGN KEY (state_id) REFERENCES states(id),
    CONSTRAINT fk_proposals_type FOREIGN KEY (proposal_type_id) REFERENCES proposal_types(id)
);