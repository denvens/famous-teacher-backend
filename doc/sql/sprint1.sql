
-- 用户表
CREATE TABLE squirrel_users (
    id bigint(11) NOT NULL AUTO_INCREMENT,
    openId  varchar(64) NOT NULL  ,
	unionId varchar(64) NOT NULL  ,
	nickName  varchar(64) DEFAULT '',
	sex int(1) DEFAULT '0',
	headImgUrl varchar(1024) DEFAULT '',
	createdAt datetime ,
 PRIMARY KEY (`id`),
 UNIQUE KEY(`openId`),
 INDEX(`unionId`)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4;


