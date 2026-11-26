DROP INDEX uq_carts_guest_active;

ALTER TABLE carts
    DROP CONSTRAINT ck_carts_un_propietario;

ALTER TABLE carts
    MODIFY (user_id NOT NULL);

ALTER TABLE carts
    DROP COLUMN guest_token_hash;
