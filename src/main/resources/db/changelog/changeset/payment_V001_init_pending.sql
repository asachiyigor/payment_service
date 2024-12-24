CREATE TABLE payment_operations
(
    id                 BIGSERIAL PRIMARY KEY,
    status             varchar(10),
    amount             NUMERIC(10, 2) NOT NULL,
    currency           varchar(3),
    from_account_id    BIGINT         NOT NULL,
    to_account_id      BIGINT         NOT NULL,
    operation_type     varchar(11),
    clear_scheduled_at  TIMESTAMP,
    created_at         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);