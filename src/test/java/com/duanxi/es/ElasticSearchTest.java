package com.duanxi.es;

import com.alibaba.fastjson.JSON;
import com.duanxi.es.config.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author caoduanxi
 * @Date 2021/1/8 17:13
 * @Motto Keep thinking, keep coding!
 * es的具体执行类
 * {@Qualifier 注解主要是用来当同时含有多个Bean名称都相同的时候，此时可以使用此标注出想要使用的那个Bean}
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticSearchTest {
    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient restHighLevelClient;
    // ES的index名称
    private static final String ES_INDEX_NAME = "collage";

    /**
     * 创建索引
     */
    @Test
    public void createIndex() throws IOException {
        // 创建索引
        CreateIndexRequest indexRequest = new CreateIndexRequest(ES_INDEX_NAME);
        // 客户端执行请求获取响应结果
        CreateIndexResponse response = restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);

        System.out.println(response.toString());
    }

    /**
     * 判断索引是否存在
     */
    @Test
    public void existIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest(ES_INDEX_NAME);
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println("exists: " + exists);
    }

    /**
     * 删除索引
     */
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(ES_INDEX_NAME);
        AcknowledgedResponse delete = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println("delete:" + delete.isAcknowledged());
    }

    /**
     * 创建文档信息
     */
    @Test
    public void createDocument() throws IOException {
        User user = new User("张三", 25, "njupt");
        // 创建请求
        IndexRequest request = new IndexRequest(ES_INDEX_NAME);
        // 设置规则，这里人为指定id为2
        request.id("1").timeout(TimeValue.timeValueSeconds(1));
        // 放入数据
        request.source(JSON.toJSONString(user), XContentType.JSON);
        // 发送创建文本的请求
        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        // 结果
        System.out.println(response.toString());
        System.out.println("状态: " + response.status());
    }

    /**
     * 判断文档是否存在
     */
    @Test
    public void existDocument() throws IOException {
        // 注意多条数据需要指定id
        GetRequest request = new GetRequest(ES_INDEX_NAME, "1");
        // 拉取文本资源,设置为false表示不获取上下文资源
        request = request.fetchSourceContext(new FetchSourceContext(false));
        // 获取一个存储的字段域
        request = request.storedFields("_name");
        boolean exists = restHighLevelClient.exists(request, RequestOptions.DEFAULT);
        System.out.println("exists:" + exists);
        System.out.println(request.toString());
    }

    /**
     * 获取文档信息
     */
    @Test
    public void getDocument() throws IOException {
        GetRequest request = new GetRequest(ES_INDEX_NAME, "2");
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        System.out.println("数据:" + response.getSourceAsString());
    }

    /**
     * 删除文档
     */
    @Test
    public void deleteDocument() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(ES_INDEX_NAME, "1");
        // 设置延时，直接写"1s"即可，内部有解析器
        deleteRequest.timeout("1s");
        // 删除获取响应
        DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println("删除结果:" + response.status());
    }

    /**
     * 批量添加文档数据
     */
    @Test
    public void bulkAddDocument() throws IOException {
        BulkRequest request = new BulkRequest();
        request.timeout("10s");
        List<User> users = new ArrayList<>();
        String name = "aaron-";
        for (int i = 1; i <= 10; i++) {
            users.add(new User(name + i, 20 + new Random().nextInt(5), "zju"));
        }
        // 分批次按照单个的添加
        int index = 3;
        for (User user : users) {
            request.add(new IndexRequest(ES_INDEX_NAME).id(index + "")
                    .source(JSON.toJSONString(user), XContentType.JSON));
            index++;
        }
        BulkResponse response = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        // 返回false表示全都成功
        System.out.println("是否有错误:" + response.hasFailures());
    }

    /**
     * 查询
     */
    @Test
    public void searchDocument() throws IOException {
        SearchRequest request = new SearchRequest(ES_INDEX_NAME);
        // 构建搜索条件
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 构建查询构造器
        FuzzyQueryBuilder queryBuilder = QueryBuilders.fuzzyQuery("name","aaron-1");
        builder.query(queryBuilder);
        builder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        request.source(builder);

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        System.out.println("结果:" + JSON.toJSONString(response.getHits()));
        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsMap());
        }
    }
}