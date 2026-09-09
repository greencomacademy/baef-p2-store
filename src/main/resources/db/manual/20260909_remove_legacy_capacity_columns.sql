-- Manual one-time migration. This project does not auto-run Flyway/Liquibase migrations.
-- Run only once against the existing development schema after taking the normal local backup.
-- Do not reset or recreate the database.

ALTER TABLE menus
    DROP CONSTRAINT chk_menus_batch_capacity,
    DROP COLUMN batch_capacity;

ALTER TABLE stores
    DROP CONSTRAINT chk_stores_kitchen_capacity,
    DROP COLUMN kitchen_capacity;

ALTER TABLE order_items
    DROP COLUMN batch_capacity_snapshot;

CREATE TABLE IF NOT EXISTS internal_menu_creation_operations (
    operation_key VARCHAR(180) NOT NULL PRIMARY KEY,
    store_id BIGINT NOT NULL,
    menu_id BIGINT NULL,
    completed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_internal_menu_creation_operations_menu
        FOREIGN KEY (menu_id) REFERENCES menus(id)
);
