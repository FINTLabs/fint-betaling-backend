create sequence orderitem_seq start with 1 increment by 1;
create sequence claim_seq start with 10000 increment by 1;
create table organisation
(
    organisation_number varchar(255) not null primary key,
    name                varchar(255)
);
create table claim
(
    order_number                                 bigint not null primary key,
    status                                       varchar(100),
    created_date                                 timestamp,
    last_modified_date                           timestamp,
    invoice_date                                 date,
    payment_due_date                             date,
    amount_due                                   bigint,
    original_amount_due                          bigint,
    timestamp                                    bigint,
    created_by_employee_number                   varchar(255),
    customer_id                                  varchar(255),
    customer_name                                varchar(255),
    invoice_numbers                              varchar(255),
    invoice_uri                                  varchar(255),
    org_id                                       varchar(255),
    organisation_number                          varchar(255)
        constraint fkkw21wvgaqnyrn1kp63w3vp73i
            references organisation,
    principal_code                               varchar(255),
    principal_uri                                varchar(255),
    requested_number_of_days_to_payment_deadline varchar(255),
    status_message                               varchar(255)
);
-- create table creditnote
-- (
--     id           varchar(255) not null primary key,
--     amount       bigint,
--     comment      varchar(255),
--     date         timestamp(6),
--     order_number varchar(255),
--     claim_id     bigint
--         constraint fknvqiliucjt9r4mj1hug9w973x
--             references claim
-- );
create table orderitem
(
    id                   bigint not null primary key,
    description          varchar(255),
    item_code            varchar(255),
    item_price           bigint,
    item_quantity        bigint,
    item_uri             varchar(255),
    original_description varchar(255),
    original_item_price  bigint,
    tax_rate             bigint,
    claim_id             bigint
        constraint fkruat9euoapto0my3i5ua184ue
            references claim
);
