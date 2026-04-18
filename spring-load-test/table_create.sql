
create table if not exists sample_user(
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name varchar(50) NOT NULL,
    age int DEFAULT 20,
    email varchar(256) DEFAULT 'email@email@email@email@email@email@email@email@email@email@email@email@email@email',
    address varchar(256) DEFAULT 'address-address-address-address-address-address-address-address-address-address',
    phone varchar(50) DEFAULT '090-090-090-8878-9932',
    website varchar(256) DEFAULT 'https://www.sample.sample.sample.cortr.com',
    sex varchar(50) DEFAULT 'men',
    sample1 varchar(50) DEFAULT 'sample1-sample1-sample1-sample1-sample1-sample1',
    sample2 varchar(50) DEFAULT 'sample2-',
    PRIMARY KEY (id)
);