CREATE TABLE `msyb_resource`.`msyb_logistics` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `open_id` varchar(64) NOT NULL COMMENT 'openId',
  `level_id` int(11) DEFAULT '0' COMMENT 'levelId',
  `status` int(2) DEFAULT '0' COMMENT '是否当前使用地址，0否；1是',
  `name` varchar(64) NOT NULL COMMENT '姓名',
  `mobile` CHAR(11) DEFAULT '0' COMMENT '电话',
  `province` varchar(20) DEFAULT '0' COMMENT '省',
  `city` varchar(20) DEFAULT '0' COMMENT '市',
  `area` varchar(20) DEFAULT '0' COMMENT '区',
  `address` varchar(200) DEFAULT '0' COMMENT '地址',
  `create_time` datetime DEFAULT NULL COMMENT '添加时间',
  `update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;
------------------------------------------------------------
ALTER TABLE `msyb_resource`.`msyb_logistics` 
ADD COLUMN `logistics_code` VARCHAR(64) NULL DEFAULT '0' COMMENT '物流编号' AFTER `status`;
------------------------------------------------------------
ALTER TABLE `msyb_resource`.`msyb_logistics` 
CHANGE COLUMN `status` `transaction_id` INT(11) NULL DEFAULT '0' COMMENT '交易单号' ;
------------------------------------------------------------
------------------------------------------------------------
ALTER TABLE `msyb`.`scholarship_apply_for` 
CHANGE COLUMN `ent_pay_order_scholarship_id` `bigbayTranctionId` VARCHAR(128) NULL DEFAULT '0' COMMENT '商户单号，海湾的单号' ;

ALTER TABLE `msyb`.`scholarship_apply_for` 
CHANGE COLUMN `bigbayTranctionId` `bigbay_tranction_id` VARCHAR(128) NULL DEFAULT '0' COMMENT '商户单号，海湾的单号' ;
------------------------------------------------------------

