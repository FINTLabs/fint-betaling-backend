create sequence hibernate_sequence start 1 increment 1;
create table claim
(
    order_number                                 int8 not null,
    amount_due                                   int8,
    claim_status                                 int4,
    created_by_employee_number                   varchar(255),
    created_date                                 timestamp,
    customer_id                                  varchar(255),
    customer_name                                varchar(255),
    invoice_date                                 date,
    invoice_numbers                              varchar(255),
    invoice_uri                                  varchar(255),
    last_modified_date                           timestamp,
    org_id                                       varchar(255),
    original_amount_due                          int8,
    payment_due_date                             date,
    principal_code                               varchar(255),
    principal_uri                                varchar(255),
    requested_number_of_days_to_payment_deadline varchar(255),
    status_message                               varchar(255),
    timestamp                                    int8,
    organisation_unit_organisation_number        varchar(255),
    primary key (order_number)
);
create table claim_credit_notes
(
    claim_order_number int8         not null,
    credit_notes_id    varchar(255) not null
);
create table claim_order_items
(
    claim_order_number int8 not null,
    order_items_id     int8 not null
);
create table creditnote
(
    id           varchar(255) not null,
    amount       int8,
    comment      varchar(255),
    date         timestamp,
    order_number varchar(255),
    primary key (id)
);
create table lineitem
(
    item_code   varchar(255) not null,
    description varchar(255),
    item_price  int8,
    taxrate     int8,
    uri         varchar(255),
    primary key (item_code)
);
create table orderitem
(
    id                   int8 not null,
    description          varchar(255),
    item_code            varchar(255),
    item_price           int8,
    item_quantity        int8,
    item_uri             varchar(255),
    original_description varchar(255),
    original_item_price  int8,
    tax_rate             int8,
    primary key (id)
);
create table organisation
(
    organisation_number varchar(255) not null,
    name                varchar(255),
    primary key (organisation_number)
);
alter table claim_credit_notes
    add constraint UK_iduwx99pnhxxq01x8wfn6f6s3 unique (credit_notes_id);
alter table claim_order_items
    add constraint UK_axcj3n7d257pbc9mv1txoglld unique (order_items_id);
alter table claim
    add constraint FKnqqwwrtfkwy0q24b6g26s0hrk foreign key (organisation_unit_organisation_number) references organisation;
alter table claim_credit_notes
    add constraint FKj0gp4bbspx8qxtum4wmg791d7 foreign key (credit_notes_id) references creditnote;
alter table claim_credit_notes
    add constraint FK2090e4kq2gv93prjrp0bonmdn foreign key (claim_order_number) references claim;
alter table claim_order_items
    add constraint FKkdmj5wc95hk8k2xs7ut6borj foreign key (order_items_id) references orderitem;
alter table claim_order_items
    add constraint FKtfsdi5yof26rfwb9y0uaqo7pk foreign key (claim_order_number) references claim;
create sequence hibernate_sequence start 1 increment 1;
create table claim
(
    order_number                                 int8 not null,
    amount_due                                   int8,
    claim_status                                 int4,
    created_by_employee_number                   varchar(255),
    created_date                                 timestamp,
    customer_id                                  varchar(255),
    customer_name                                varchar(255),
    invoice_date                                 date,
    invoice_numbers                              varchar(255),
    invoice_uri                                  varchar(255),
    last_modified_date                           timestamp,
    org_id                                       varchar(255),
    original_amount_due                          int8,
    payment_due_date                             date,
    principal_code                               varchar(255),
    principal_uri                                varchar(255),
    requested_number_of_days_to_payment_deadline varchar(255),
    status_message                               varchar(255),
    timestamp                                    int8,
    organisation_unit_organisation_number        varchar(255),
    primary key (order_number)
);
create table claim_credit_notes
(
    claim_order_number int8         not null,
    credit_notes_id    varchar(255) not null
);
create table claim_order_items
(
    claim_order_number int8 not null,
    order_items_id     int8 not null
);
create table creditnote
(
    id           varchar(255) not null,
    amount       int8,
    comment      varchar(255),
    date         timestamp,
    order_number varchar(255),
    primary key (id)
);
create table lineitem
(
    item_code   varchar(255) not null,
    description varchar(255),
    item_price  int8,
    taxrate     int8,
    uri         varchar(255),
    primary key (item_code)
);
create table orderitem
(
    id                   int8 not null,
    description          varchar(255),
    item_code            varchar(255),
    item_price           int8,
    item_quantity        int8,
    item_uri             varchar(255),
    original_description varchar(255),
    original_item_price  int8,
    tax_rate             int8,
    primary key (id)
);
create table organisation
(
    organisation_number varchar(255) not null,
    name                varchar(255),
    primary key (organisation_number)
);
create table value_converting
(
    id                  bigserial not null,
    display_name        varchar(255),
    from_application_id int8,
    from_type_id        varchar(255),
    to_application_id   varchar(255),
    to_type_id          varchar(255),
    primary key (id)
);
alter table claim_credit_notes
    add constraint UK_iduwx99pnhxxq01x8wfn6f6s3 unique (credit_notes_id);
alter table claim_order_items
    add constraint UK_axcj3n7d257pbc9mv1txoglld unique (order_items_id);
alter table claim
    add constraint FKnqqwwrtfkwy0q24b6g26s0hrk foreign key (organisation_unit_organisation_number) references organisation;
alter table claim_credit_notes
    add constraint FKj0gp4bbspx8qxtum4wmg791d7 foreign key (credit_notes_id) references creditnote;
alter table claim_credit_notes
    add constraint FK2090e4kq2gv93prjrp0bonmdn foreign key (claim_order_number) references claim;
alter table claim_order_items
    add constraint FKkdmj5wc95hk8k2xs7ut6borj foreign key (order_items_id) references orderitem;
alter table claim_order_items
    add constraint FKtfsdi5yof26rfwb9y0uaqo7pk foreign key (claim_order_number) references claim;
