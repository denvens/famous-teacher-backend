CREATE TABLE `msyb_resource`.`msyb_certificate` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `openId` varchar(64) NOT NULL,
  `classify` INT(11) NULL DEFAULT 0 COMMENT '准考证类型:1,大学英语四级考试(CET-4);2,大学英语六级考试(CET-6)	',
  `number` varchar(64) DEFAULT '0' COMMENT '准考证号',
  `createDate` datetime DEFAULT NULL COMMENT '添加时间',
  `updateDate` datetime DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
------------------------------------------------------------
ALTER TABLE `msyb_resource`.`squirrel_levels` 
ADD COLUMN `return_fee_day` INT(11) NULL DEFAULT NULL COMMENT '返学费天数' AFTER `buySite`;
------------------------------------------------------------
DROP TABLE `msyb`.`scholarship_apply_for`;

CREATE TABLE `msyb`.`scholarship_apply_for` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ent_pay_order_scholarship_id` bigint(20) DEFAULT '0',
  `scholarship_open_id` varchar(64) NOT NULL COMMENT '申请人openId',
  `begin_at` date NOT NULL COMMENT '开课批次',
  `level_id` int(11) NOT NULL COMMENT 'levelId',
  `learn_day` int(11) NOT NULL DEFAULT '0' COMMENT '学习天数',
  `learn_make_up_day` int(11) NOT NULL DEFAULT '0' COMMENT '补学天数',
  `status` int(2) NOT NULL COMMENT '奖学金申请状态: 3,已申请,审核中; 4,审核通过,发放成功; 5,审核拒绝;',
  `refund_status` int(2) NOT NULL DEFAULT '0' COMMENT '奖学金退款状态: 0,待退款; 1,已退款; 2,退款失败;',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '金额',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间,奖学金申请时间',
  `updated_at` datetime DEFAULT NULL COMMENT '修改时间,奖学金发放时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=269 DEFAULT CHARSET=utf8 COMMENT='奖学金申请表';
------------------------------------------------------------
ALTER TABLE `msyb`.`scholarship_apply_for` 
CHANGE COLUMN `learn_day` `learn_day` INT(11) NOT NULL DEFAULT '0' COMMENT '学习天数' ,
CHANGE COLUMN `make_up_learn_day` `make_up_learn_day` INT(11) NOT NULL DEFAULT '0' COMMENT '补学天数' ;
------------------------------------------------------------





