-- V2__create_claim_status_history.sql

CREATE SEQUENCE history_seq
    START WITH 10000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE claim_status_history (
                                      id BIGINT PRIMARY KEY DEFAULT nextval('history_seq'),
                                      order_number VARCHAR(255),
                                      status VARCHAR(100),
                                      status_message VARCHAR(255),
                                      timestamp BIGINT
);