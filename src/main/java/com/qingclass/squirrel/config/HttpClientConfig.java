package com.qingclass.squirrel.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
		import org.springframework.context.annotation.Configuration;

/**
 * httpClient 的相关配置，主要是增大了连接数
 *
 * @author 苏天奇
 * */
@Configuration
public class HttpClientConfig {

	@Bean
	public HttpClient initHttpClient(){
		int CONNECTION_TIMEOUT_MS = 5 * 1000;

		RequestConfig requestConfig = RequestConfig
				.custom()
				.setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
				.setConnectTimeout(CONNECTION_TIMEOUT_MS)
				.setSocketTimeout(CONNECTION_TIMEOUT_MS)
				.build();

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultMaxPerRoute(200);
		connManager.setMaxTotal(200);

		CloseableHttpClient client = HttpClients
				.custom()
				.setConnectionManager(connManager)
				.setDefaultRequestConfig(requestConfig)
				.build();

		return client;
	}

}
