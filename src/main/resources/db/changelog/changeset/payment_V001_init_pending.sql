CREATE TABLE payment_operations
(
    id                 BIGSERIAL PRIMARY KEY,
    status             SMALLINT       NOT NULL DEFAULT 0,
    amount             NUMERIC(10, 2) NOT NULL,
    currency           SMALLINT       NOT NULL,
    balance_id         BIGINT         NOT NULL,
    from_account_id    BIGINT         NOT NULL,
    to_account_id      BIGINT         NOT NULL,
    clear_scheduled_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);