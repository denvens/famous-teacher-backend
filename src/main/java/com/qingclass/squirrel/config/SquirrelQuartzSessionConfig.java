package com.qingclass.squirrel.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 *
 *
 * @author 苏天奇
 * */
@Configuration
@MapperScan(basePackages = "com.qingclass.squirrel.mapper.quartz", sqlSessionFactoryRef = "sqlSessionBeanSquirrelQuartz")
public class SquirrelQuartzSessionConfig {

    @Autowired
    @Qualifier("dataSourceSquirrelQuartz")
    DataSource resourceDataSource;

    @Bean("sqlSessionBeanSquirrelQuartz")
    @Primary
    public SqlSessionFactory sqlSessionSquirrelResourceFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(resourceDataSource); // 使用titan数据源, 连接titan库

        return factoryBean.getObject();

    }

    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionSquirrelResourceFactory()); // 使用上面配置的Factory
        return template;
    }

    @Bean(name = "squirrelQuartzTransaction")
    public PlatformTransactionManager platformTransactionManager(){
        return new DataSourceTransactionManager(resourceDataSource);
    }


}
