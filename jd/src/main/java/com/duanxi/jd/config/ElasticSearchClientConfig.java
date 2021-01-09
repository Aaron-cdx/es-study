package com.duanxi.jd.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author caoduanxi
 * @Date 2021/1/8 17:01
 * @Motto Keep thinking, keep coding!
 * es的客户端配置
 */
@Configuration
public class ElasticSearchClientConfig {
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost("121.5.58.228", 9200, "http")
        ));
    }
}
