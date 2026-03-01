-- Flyway migration V1: initial schema for Bakery API (MySQL).
-- Note: This schema matches the JPA model and Spring's default physical naming strategy (camelCase -> snake_case).

create table if not exists users (
    id bigint not null auto_increment,
    email varchar(255) not null,
    password varchar(255) not null,
    role varchar(255) not null,
    enabled boolean not null default true,
    primary key (id),
    unique key uk_users_email (email)
) engine=InnoDB;

create table if not exists categories (
    id bigint not null auto_increment,
    name varchar(100) not null,
    primary key (id),
    unique key uk_categories_name (name)
) engine=InnoDB;

create table if not exists products (
    id bigint not null auto_increment,
    name varchar(100) not null,
    description varchar(255),
    price decimal(10,2) not null,
    stock int not null,
    category_id bigint not null,
    active boolean not null default true,
    primary key (id),
    key idx_product_category (category_id),
    key idx_product_active (active),
    constraint fk_products_category
        foreign key (category_id) references categories (id)
) engine=InnoDB;

create table if not exists promotions (
    id bigint not null auto_increment,
    type varchar(31) not null,
    description varchar(255) not null,
    start_date date not null,
    end_date date,
    active boolean not null default true,
    product_id bigint not null,
    discount_percentage decimal(5,2),
    buy_quantity int,
    pay_quantity int,
    primary key (id),
    key idx_promotion_product (product_id),
    key idx_promotion_dates (start_date, end_date),
    key idx_promotion_active (active),
    constraint fk_promotions_product
        foreign key (product_id) references products (id)
) engine=InnoDB;

create table if not exists promotion_usage (
    id bigint not null auto_increment,
    promotion_id bigint not null,
    user_id bigint not null,
    used_at datetime(6) not null,
    primary key (id),
    unique key uk_promotion_usage_promotion_user (promotion_id, user_id),
    key idx_promotion_usage_promotion (promotion_id),
    key idx_promotion_usage_user (user_id),
    constraint fk_promotion_usage_promotion
        foreign key (promotion_id) references promotions (id),
    constraint fk_promotion_usage_user
        foreign key (user_id) references users (id)
) engine=InnoDB;

create table if not exists purchases (
    id bigint not null auto_increment,
    user_id bigint not null,
    created_at datetime(6) not null,
    status varchar(255) not null,
    total decimal(10,2) not null,
    primary key (id),
    key idx_purchase_user (user_id),
    key idx_purchase_status (status),
    constraint fk_purchases_user
        foreign key (user_id) references users (id)
) engine=InnoDB;

create table if not exists purchase_items (
    id bigint not null auto_increment,
    purchase_id bigint not null,
    product_id bigint not null,
    promotion_id bigint,
    quantity int not null,
    unit_price decimal(10,2) not null,
    discount_amount decimal(10,2) not null,
    subtotal decimal(10,2) not null,
    primary key (id),
    key idx_purchase_items_purchase (purchase_id),
    key idx_purchase_items_product (product_id),
    key idx_purchase_items_promotion (promotion_id),
    constraint fk_purchase_items_purchase
        foreign key (purchase_id) references purchases (id),
    constraint fk_purchase_items_product
        foreign key (product_id) references products (id),
    constraint fk_purchase_items_promotion
        foreign key (promotion_id) references promotions (id)
) engine=InnoDB;

