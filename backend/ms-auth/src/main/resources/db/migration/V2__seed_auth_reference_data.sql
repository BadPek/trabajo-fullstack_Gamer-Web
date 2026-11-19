-- =====================================================================
-- ms-auth · V2: datos de referencia
-- Roles y las regiones/comunas que ofrece hoy el formulario de registro
-- (BACKEND_CONTRACT.md v0.6, "Validación de perfil y contacto").
-- IDs fijos para que todos los entornos tengan los mismos valores.
-- =====================================================================

INSERT INTO roles (role_code) VALUES ('CLIENTE');
INSERT INTO roles (role_code) VALUES ('ADMINISTRADOR');

INSERT INTO regions (region_id, code, name) VALUES (1, 'metropolitana', 'Región Metropolitana de Santiago');
INSERT INTO regions (region_id, code, name) VALUES (2, 'valparaiso', 'Región de Valparaíso');
INSERT INTO regions (region_id, code, name) VALUES (3, 'biobio', 'Región del Biobío');

INSERT INTO communes (commune_id, region_id, name) VALUES (101, 1, 'Santiago Centro');
INSERT INTO communes (commune_id, region_id, name) VALUES (102, 1, 'Providencia');
INSERT INTO communes (commune_id, region_id, name) VALUES (103, 1, 'Las Condes');
INSERT INTO communes (commune_id, region_id, name) VALUES (104, 1, 'Maipú');
INSERT INTO communes (commune_id, region_id, name) VALUES (105, 1, 'Puente Alto');
INSERT INTO communes (commune_id, region_id, name) VALUES (106, 1, 'Ñuñoa');
INSERT INTO communes (commune_id, region_id, name) VALUES (201, 2, 'Viña del Mar');
INSERT INTO communes (commune_id, region_id, name) VALUES (202, 2, 'Valparaíso');
INSERT INTO communes (commune_id, region_id, name) VALUES (203, 2, 'Quilpué');
INSERT INTO communes (commune_id, region_id, name) VALUES (204, 2, 'Villa Alemana');
INSERT INTO communes (commune_id, region_id, name) VALUES (205, 2, 'Concón');
INSERT INTO communes (commune_id, region_id, name) VALUES (301, 3, 'Concepción');
INSERT INTO communes (commune_id, region_id, name) VALUES (302, 3, 'Talcahuano');
INSERT INTO communes (commune_id, region_id, name) VALUES (303, 3, 'San Pedro de la Paz');
INSERT INTO communes (commune_id, region_id, name) VALUES (304, 3, 'Chiguayante');
INSERT INTO communes (commune_id, region_id, name) VALUES (305, 3, 'Los Ángeles');
