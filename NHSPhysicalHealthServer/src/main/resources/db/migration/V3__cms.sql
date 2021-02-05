create table CmsPage (
    id  bigserial not null,
    name varchar(255) not null,
    modified timestamp not null,
    linksHeader varchar(255) not null,
    primary key (id)
);

create table CmsPageFragment (
    id  bigserial not null,
    cmsPage_id int8,
    cmsPageOrder int4 not null,
    api varchar(255) not null,
    primaryEntityName varchar(255) not null,
    secondaryEntityName varchar(255),
    jsonMainEntityPosition int4 not null,
    jsonSectionHeading varchar(255) not null,
    primary key (id)
);


create table CmsPageLink (
    id  bigserial not null,
    cmsPageOrder int4 not null,
    description varchar(255) not null,
    label varchar(255) not null,
    url varchar(255) not null,
    cmsPage_id int8,
    primary key (id)
);

create table CmsCache (
    id  bigserial not null,
    api varchar(255) not null,
    primaryEntityName varchar(255) not null,
    secondaryEntityName varchar(255),
    content text,
    loaded timestamp not null,
    primary key (id)
);

alter table CmsPage
   add constraint UK_ic6leqsbqh5vlhonvl62m9gs4 unique (name);

alter table CmsPageFragment
   add constraint FK1icejcy1sdysy94ee5nnbw740
   foreign key (cmsPage_id)
   references CmsPage;

alter table CmsPageLink
   add constraint FKfjjjpccxwowo3rvagxgcxl3vj
   foreign key (cmsPage_id)
   references CmsPage;
