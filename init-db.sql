-- Create the dater user and its database
CREATE DATABASE dater owner postgres;
CREATE ROLE dater WITH LOGIN PASSWORD 'dater';

\c dater
ALTER SCHEMA public OWNER TO dater;

-- Create the role group_readwrite
CREATE ROLE group_readwrite;

-- Grant schema-level privileges
GRANT USAGE, CREATE ON SCHEMA public TO group_readwrite;

-- Grant privileges on all existing tables in the public schema
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON ALL TABLES IN SCHEMA public TO group_readwrite;

-- Grant privileges on all existing sequences in the public schema
GRANT USAGE, SELECT
    ON ALL SEQUENCES IN SCHEMA public TO group_readwrite;

-- Grant execute privileges on all existing functions in the public schema
GRANT EXECUTE
    ON ALL FUNCTIONS IN SCHEMA public TO group_readwrite;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON TABLES TO group_readwrite;

-- Set default privileges for future sequences
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT
    ON SEQUENCES TO group_readwrite;

-- Set default privileges for future functions
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT EXECUTE
    ON FUNCTIONS TO group_readwrite;

-- Create the role app_user that can manage rw operations
CREATE ROLE app_user WITH LOGIN PASSWORD 'password';
GRANT group_readwrite TO app_user;

-- Create the extension for UUID generation
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";