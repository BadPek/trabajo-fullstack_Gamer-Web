CREATE TABLE categories (
    category_id     CHAR(36) NOT NULL,
    name            VARCHAR2(100 CHAR) NOT NULL,
    name_normalized VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_name_normalized UNIQUE (name_normalized)
);

CREATE TABLE companies (
    company_id      CHAR(36) NOT NULL,
    name            VARCHAR2(150 CHAR) NOT NULL,
    name_normalized VARCHAR2(150 CHAR) NOT NULL,
    CONSTRAINT pk_companies PRIMARY KEY (company_id),
    CONSTRAINT uq_companies_name_normalized UNIQUE (name_normalized)
);

CREATE TABLE game_modes (
    mode_id CHAR(36) NOT NULL,
    name    VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT pk_game_modes PRIMARY KEY (mode_id),
    CONSTRAINT uq_game_modes_name UNIQUE (name)
);

CREATE TABLE languages (
    language_id CHAR(36) NOT NULL,
    name        VARCHAR2(100 CHAR) NOT NULL,
    CONSTRAINT pk_languages PRIMARY KEY (language_id),
    CONSTRAINT uq_languages_name UNIQUE (name)
);

CREATE TABLE products (
    product_id        CHAR(36) NOT NULL,
    title             VARCHAR2(150 CHAR) NOT NULL,
    price_clp         NUMBER(10) NOT NULL,
    category_id       CHAR(36) NOT NULL,
    short_description VARCHAR2(500 CHAR) NOT NULL,
    description_text  CLOB NOT NULL,
    release_date      DATE NOT NULL,
    video_url         VARCHAR2(2048 CHAR) NULL,
    status            VARCHAR2(10 CHAR) DEFAULT 'ACTIVE' NOT NULL,
    created_at        TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    updated_at        TIMESTAMP(6) DEFAULT SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (product_id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (category_id),
    CONSTRAINT ck_products_precio CHECK (price_clp > 0),
    CONSTRAINT ck_products_fecha CHECK (release_date = TRUNC(release_date)),
    CONSTRAINT ck_products_status CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE INDEX ix_products_publico ON products (status, release_date DESC, product_id);
CREATE INDEX ix_products_admin ON products (created_at DESC, product_id);
CREATE INDEX ix_products_admin_status ON products (status, created_at DESC, product_id);
CREATE INDEX ix_products_category ON products (category_id);

CREATE TABLE product_companies (
    product_id   CHAR(36) NOT NULL,
    company_id   CHAR(36) NOT NULL,
    company_role VARCHAR2(10 CHAR) NOT NULL,
    CONSTRAINT pk_product_companies PRIMARY KEY (product_id, company_id, company_role),
    CONSTRAINT fk_pc_product FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_company FOREIGN KEY (company_id) REFERENCES companies (company_id),
    CONSTRAINT ck_pc_role CHECK (company_role IN ('DEVELOPER', 'PUBLISHER'))
);

CREATE INDEX ix_pc_company ON product_companies (company_id);

CREATE TABLE product_media (
    media_id   CHAR(36) NOT NULL,
    product_id CHAR(36) NOT NULL,
    media_ref  VARCHAR2(2048 CHAR) NOT NULL,
    media_type VARCHAR2(10 CHAR) DEFAULT 'IMAGE' NOT NULL,
    media_role VARCHAR2(10 CHAR) NOT NULL,
    position   NUMBER(3) NOT NULL,
    CONSTRAINT pk_product_media PRIMARY KEY (media_id),
    CONSTRAINT uq_product_media_posicion UNIQUE (product_id, media_role, position),
    CONSTRAINT fk_product_media_product FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE,
    CONSTRAINT ck_product_media_type CHECK (media_type IN ('IMAGE', 'VIDEO')),
    CONSTRAINT ck_product_media_role CHECK (media_role IN ('COVER', 'GALLERY')),
    CONSTRAINT ck_product_media_posicion CHECK (
        (media_role = 'COVER' AND position = 0)
        OR (media_role = 'GALLERY' AND position BETWEEN 1 AND 20)
    )
);

CREATE TABLE system_requirements (
    product_id       CHAR(36) NOT NULL,
    requirement_kind VARCHAR2(12 CHAR) NOT NULL,
    os               VARCHAR2(300 CHAR) NOT NULL,
    processor        VARCHAR2(300 CHAR) NOT NULL,
    memory           VARCHAR2(300 CHAR) NOT NULL,
    graphics         VARCHAR2(300 CHAR) NOT NULL,
    directx          VARCHAR2(300 CHAR) NOT NULL,
    storage          VARCHAR2(300 CHAR) NOT NULL,
    CONSTRAINT pk_system_requirements PRIMARY KEY (product_id, requirement_kind),
    CONSTRAINT fk_sr_product FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE,
    CONSTRAINT ck_sr_kind CHECK (requirement_kind IN ('MINIMUM', 'RECOMMENDED'))
);

CREATE TABLE product_game_modes (
    product_id CHAR(36) NOT NULL,
    mode_id    CHAR(36) NOT NULL,
    CONSTRAINT pk_product_game_modes PRIMARY KEY (product_id, mode_id),
    CONSTRAINT fk_pgm_product FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE,
    CONSTRAINT fk_pgm_mode FOREIGN KEY (mode_id) REFERENCES game_modes (mode_id)
);

CREATE INDEX ix_pgm_mode ON product_game_modes (mode_id);

CREATE TABLE product_languages (
    product_id  CHAR(36) NOT NULL,
    language_id CHAR(36) NOT NULL,
    CONSTRAINT pk_product_languages PRIMARY KEY (product_id, language_id),
    CONSTRAINT fk_pl_product FOREIGN KEY (product_id) REFERENCES products (product_id) ON DELETE CASCADE,
    CONSTRAINT fk_pl_language FOREIGN KEY (language_id) REFERENCES languages (language_id)
);

CREATE INDEX ix_pl_language ON product_languages (language_id);

CREATE OR REPLACE TRIGGER trg_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
BEGIN
    :NEW.updated_at := SYS_EXTRACT_UTC(SYSTIMESTAMP);
END;
/
