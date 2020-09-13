package com.qingclass.squirrel.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 连接多源数据库的核心配置类
 *
 * @author 苏天奇
 * */
@Configuration
public class ResourceDataSource {
	public static final String DATASOURCE_SQUIRREL_RESOURCE = "dataSourceSquirrelResource";

	public static final String DATASOURCE_SQUIRREL = "dataSourceSquirrel";

	public static final String DATASOURCE_SQUIRREL_QUARTZ = "dataSourceSquirrelQuartz";
	
	@Bean(DATASOURCE_SQUIRREL_RESOURCE)
	@ConfigurationProperties(prefix = "spring.datasource.squirrel.resource")
	@Primary
	public DataSource resourceDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(DATASOURCE_SQUIRREL)
	@ConfigurationProperties(prefix = "spring.datasource.squirrel")
	public DataSource squirrelDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(DATASOURCE_SQUIRREL_QUARTZ)
	@ConfigurationProperties(prefix = "org.quartz.dataSource.qzDS")
	public DataSource squirrelDataSourceQuartz() {
		return DataSourceBuilder.create().build();
	}

}