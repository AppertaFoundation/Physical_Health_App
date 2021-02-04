
    create table AppertaUser (
       id  bigserial not null,
        emailAddress varchar(255) not null,
        role varchar(255) not null,
        username varchar(255) not null,
        primary key (id)
    );

    create table CompositionParameters (
       id  bigserial not null,
        ehrName varchar(2000) not null,
        parameterName varchar(500) not null,
        setName varchar(255) not null,
        app_id int8 not null,
        primary key (id)
    );

    create table Device (
       id  bigserial not null,
        deviceType varchar(255) not null,
        token varchar(255),
        username varchar(255) not null,
        uuid varchar(255) not null,
        primary key (id)
    );

    create table DeviceApp (
       id  bigserial not null,
        appName varchar(255),
        primary key (id)
    );

    create table DeviceNotification (
       id  bigserial not null,
        lastAction timestamp,
        payload varchar(255) not null,
        sendTime timestamp,
        state varchar(255) not null,
        user_id int8 not null,
        primary key (id)
    );

    create table Hcp (
       id  bigserial not null,
        firstNames varchar(255) not null,
        jobTitle varchar(255) not null,
        lastName varchar(255) not null,
        location varchar(255) not null,
        nhsId varchar(255) not null,
        title varchar(255) not null,
        user_id int8,
        primary key (id)
    );

    create table OAuthUser (
       id  bigserial not null,
        emailAddress varchar(255),
        password varchar(255),
        username varchar(255),
        primary key (id)
    );

    create table PasswordResetToken (
       value varchar(255) not null,
        issued timestamp not null,
        status varchar(255) not null,
        user_id int8 not null,
        primary key (value)
    );

    create table QueryTemplate (
       id  bigserial not null,
        name varchar(255) not null,
        template text not null,
        app_id int8 not null,
        primary key (id)
    );

    create table Template (
       id  bigserial not null,
        template varchar(255) not null,
        app_id int8 not null,
        primary key (id)
    );

    alter table AppertaUser
       add constraint UK_19mbj5ge5xv1opx7vyat3jdsc unique (username);

    alter table Device
       add constraint UK_lk6pgf1sfm39410mpxasmtdia unique (uuid);

    alter table DeviceApp
       add constraint UK_jhkmpedwkqf4btibfdeuoqirr unique (appName);

    alter table Hcp
       add constraint UK_kq13smw1dwaed03k9ejhldwnp unique (nhsId);

    alter table OAuthUser
       add constraint UK_2hcdd6xwhqtctvef72u4p1y4j unique (username);

    alter table CompositionParameters
       add constraint FK4u8d4qn8ivc45rka0rn2122t
       foreign key (app_id)
       references DeviceApp;

    alter table DeviceNotification
       add constraint FK5tbnqwitfb2uikeiicok4ans3
       foreign key (user_id)
       references AppertaUser;

    alter table Hcp
       add constraint FK3icf96rlmbqsxuhoj3j1mcj6c
       foreign key (user_id)
       references AppertaUser;

    alter table PasswordResetToken
       add constraint FK6rhjty21cldv5qru8eexo00rg
       foreign key (user_id)
       references OAuthUser;

    alter table QueryTemplate
       add constraint FK33lqpeupquvxned0otg6ewflw
       foreign key (app_id)
       references DeviceApp;

    alter table Template
       add constraint FKher0xx51o8qf68cqggg33twu0
       foreign key (app_id)
       references DeviceApp;
