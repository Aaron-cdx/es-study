package com.duanxi.jd.service;

import com.alibaba.fastjson.JSON;
import com.duanxi.jd.entity.Good;
import com.duanxi.jd.utils.JsoupUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author caoduanxi
 * @Date 2021/1/9 13:51
 * @Motto Keep thinking, keep coding!
 */
@Service
public class ContentService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    // 将数据解析到es中
    public boolean parseContent(String keywords) throws IOException {
        BulkRequest request = new BulkRequest();
        request.timeout("2m");
        List<Good> jdGoods = JsoupUtils.getJDGoods(keywords);
        for (int i = 0; i < jdGoods.size(); i++) {
            request.add(new IndexRequest("jd_good")
//                    .id(String.valueOf(i + 1))
                    .source(JSON.toJSONString(jdGoods.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return bulk.hasFailures();
    }

    /**
     * 查询
     */
    public List<Map<String, Object>> searchElasticSearch(int pageNo, int pageSize, String keyword) throws IOException {
        SearchRequest searchRequest = new SearchRequest("jd_good");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 分页
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);

        // 精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.query(termQuery);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 执行搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String, Object>> lists = new ArrayList<>();
        for (SearchHit documentFields : search.getHits().getHits()) {
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            lists.add(sourceAsMap);
        }
        return lists;
    }

    /**
     * 高亮搜索
     */
    public List<Map<String, Object>> highlightSearchElasticSearch(int pageNo, int pageSize, String keyword) throws IOException {
        SearchRequest searchRequest = new SearchRequest("jd_good");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 分页
        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);

        // 精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        searchSourceBuilder.query(termQuery);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 构造高亮的查询构造器
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false);
        // 指定高亮字段,添加了之后，会将第一个高亮的字段加上这个标签
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);


        // 执行搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String, Object>> lists = new ArrayList<>();
        for (SearchHit documentFields : search.getHits().getHits()) {
            // 需要解析高亮的字段,将高亮的字段进行替换即可
            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            if(title != null){
                // 获取到高亮的片段,知道哪些字段是需要高亮的，这里的话将这个高亮字段取出来，此时字段已经加上了需要高亮的部分
                // 此时对其进行拼接即可
                Text[] fragments = title.fragments();
                StringBuilder new_title = new StringBuilder();
                for (Text fragment : fragments) {
                    new_title.append(fragment);
                }
                sourceAsMap.put("title",new_title);
            }
            lists.add(sourceAsMap);
        }
        return lists;
    }
}
