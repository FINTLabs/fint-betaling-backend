create sequence orderitem_seq start with 1 increment by 50;
create table claim
(
    claim_status                                 smallint check (claim_status between 0 and 9),
    created_date                                 date,
    invoice_date                                 date,
    last_modified_date                           date,
    payment_due_date                             date,
    amount_due                                   bigint,
    order_number                                 bigint not null,
    original_amount_due                          bigint,
    timestamp                                    bigint,
    created_by_employee_number                   varchar(255),
    customer_id                                  varchar(255) unique,
    invoice_numbers                              varchar(255),
    invoice_uri                                  varchar(255),
    org_id                                       varchar(255),
    organisation_unit_organisation_number        varchar(255) unique,
    principal_code                               varchar(255),
    principal_uri                                varchar(255),
    requested_number_of_days_to_payment_deadline varchar(255),
    status_message                               varchar(255),
    primary key (order_number)
);
create table claim_credit_notes
(
    claim_order_number bigint       not null,
    credit_notes_id    varchar(255) not null unique
);
create table claim_order_items
(
    claim_order_number bigint not null,
    order_items_id     bigint not null unique
);
create table creditnote
(
    date         date,
    amount       bigint,
    comment      varchar(255),
    id           varchar(255) not null,
    order_number varchar(255),
    primary key (id)
);
create table customer
(
    id   varchar(255) not null,
    name varchar(255),
    primary key (id)
);
create table lineitem
(
    item_price  bigint,
    taxrate     bigint,
    description varchar(255),
    item_code   varchar(255) not null,
    uri         varchar(255),
    primary key (item_code)
);
create table orderitem
(
    id                 bigint not null,
    item_price         bigint,
    item_quantity      bigint,
    description        varchar(255),
    lineitem_item_code varchar(255) unique,
    primary key (id)
);
create table organisation
(
    name                varchar(255),
    organisation_number varchar(255) not null,
    primary key (organisation_number)
);
alter table if exists claim
    add constraint FKlmon26is6b9p2wipdl88yn68y foreign key (customer_id) references customer;
alter table if exists claim
    add constraint FKnqqwwrtfkwy0q24b6g26s0hrk foreign key (organisation_unit_organisation_number) references organisation;
alter table if exists claim_credit_notes
    add constraint FKj0gp4bbspx8qxtum4wmg791d7 foreign key (credit_notes_id) references creditnote;
alter table if exists claim_credit_notes
    add constraint FK2090e4kq2gv93prjrp0bonmdn foreign key (claim_order_number) references claim;
alter table if exists claim_order_items
    add constraint FKkdmj5wc95hk8k2xs7ut6borj foreign key (order_items_id) references orderitem;
alter table if exists claim_order_items
    add constraint FKtfsdi5yof26rfwb9y0uaqo7pk foreign key (claim_order_number) references claim;
alter table if exists orderitem
    add constraint FKspymui268558oowe2wm47lcy7 foreign key (lineitem_item_code) references lineitem;