-- Create the smart_home schema
CREATE SCHEMA smart_home;

-- Switch to the smart_home schema
SET search_path TO smart_home;

-- Create the connected_components table
CREATE TABLE connected_components (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    current_state VARCHAR(20) NOT NULL CHECK (current_state IN ('off', 'on', 'triggered')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create the audit table
CREATE TABLE connected_components_audit (
    id SERIAL PRIMARY KEY,
    component_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    current_state VARCHAR(20) NOT NULL,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    change_type VARCHAR(20) NOT NULL -- 'INSERT', 'UPDATE', or 'DELETE'
);

-- Create the audit trigger function
CREATE OR REPLACE FUNCTION connected_components_audit_trigger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        INSERT INTO connected_components_audit (component_id, name, type, current_state, change_type)
        VALUES (OLD.id, OLD.name, OLD.type, OLD.current_state, 'DELETE');
    ELSIF TG_OP = 'UPDATE' THEN
        -- Check if the values are actually changed
        IF OLD.name <> NEW.name OR OLD.type <> NEW.type OR OLD.current_state <> NEW.current_state THEN
            INSERT INTO connected_components_audit (component_id, name, type, current_state, change_type)
            VALUES (NEW.id, NEW.name, NEW.type, NEW.current_state, 'UPDATE');
        END IF;
    ELSE -- TG_OP = 'INSERT'
        INSERT INTO connected_components_audit (component_id, name, type, current_state, change_type)
        VALUES (NEW.id, NEW.name, NEW.type, NEW.current_state, 'INSERT');
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create the audit trigger
CREATE TRIGGER connected_components_audit
AFTER INSERT OR UPDATE OR DELETE ON connected_components
FOR EACH ROW EXECUTE FUNCTION connected_components_audit_trigger();

-- Create a new role
CREATE ROLE component_role;

-- Grant privileges to the component_role role
GRANT UPDATE(current_state) ON TABLE connected_components TO component_role;

-- Create a new user and assign the component_role role
CREATE USER component_user WITH PASSWORD 'your_password';
GRANT component_role TO component_user;

-- Set default schema for the user
ALTER USER component_user SET search_path TO smart_home;

-- Insert initial data
INSERT INTO connected_components (name, type, current_state) VALUES ('LIGHTBULB', 'producer','off');
INSERT INTO connected_components (name, type, current_state) VALUES ('ENERGY TRACKER', 'consumer','off');
