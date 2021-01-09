## 写在前面
> 如果要使用项目的话，注意修改具体的ip地址，默认是localhost

# ElasticSearch学习（2021-1-7 21:47:34）

> `ElasticSearch`中的相关原理，比如其中的`Lucene`搜索引擎，还有一些常用的搜索命令，需要重视，在实习的时候用到了很多，但是基本都没有去仔细的系统的学过，这个在企业中用来查找服务器的日志还是非常有用的，而且在`Kafka`中日志收集之后就是使用的`es`进行日志的查询，从而快速找出其中的问题点，修复`bug`。

![Lucene Logo](https://lucene.apache.org/theme/images/lucene/lucene_logo_green_300.png)

ES中使用的搜索引擎主要是Lucene，而Lucene使用到的关键技术是倒排索引技术。

**索引的过程**

* 获取内容
* 建立文档
    * 获取原始内容之后，需要对内容进行索引，必须首先将这些内容转成文档document，以供搜索引擎使用，文章主要是包括几个带值的域，比如标题、正文、摘要、作者和链接等
* 文档分析
    * 搜索引擎不能够直接对文本进行索引：即必须将文本分割成一系列称为语汇单元的独立的原子元素，每一个词汇单元大致与语言中的单词对应起来了
* 文档索引
    * 在索引步骤中，文档被加入到索引列表

**搜索组件**

搜索里过程就是从索引总查找单词，从而找到包含该单词的文档，搜索质量主要由查准率和查全率来衡量，查全率用来衡量搜索系统查找相关文档的能力，而查准率用来衡量搜索系统过过滤非相关文档的能力。

* 用户搜索界面
* 建立查询
    * 用户从搜索界面提交请求之后，以HTML表单或者Ajax请求的形式由浏览器提交到你的搜索服务引擎服务器，然后将这个请求转换成搜索引擎使用的查询对象格式
* 搜索查询
    * 查询检索并返回与查询语句匹配的文档，结果返回时按照查询请求来排序
* 展现结果

## 1. 倒排索引

> 倒排索引的概念是根据正向索引的技术反向得到的，正向索引的话，即一般按照我们的常规思维，假设我们需要查找一个词，那么如果我们有多篇文章，一般就是读入文章，然后将词挨个遍历，直到找到为止，实际上这个即简历了文档与词语之间的索引连接。
>
> 倒排索引的话即是建立了词与文章之间的关系，即将文章进行分词之后，通过将词语与文章进行关联得到倒排索引。

**Lucene核心的API**

**索引过程核心类**

* Document文档：承载数据的实体，是抽象的概念，一条数据经过索引之后，以Document的形式存储在索引文件中。
* Field域：Field索引中的每一个Document对象都包含一个或者多个不同的域，域是由域名name和域值value对组成，每一个域都包含一段相应的数据信息。
* IndexWriter：索引过程的核心组件，这个类用于创建一个新的索引并且把文档加入到已有的索引中去，即写入操作
* Directory：索引的存放位置，抽象类，具体的子类提供特定的存储索引的地址。FSDirectory将索引存放在指定的磁盘中，RAMDirectory将索引存放在内存中。
* Analyzer：分词器，在文本被索引之前，需要经过分词器处理，分词器负责从被索引的文档中提取词汇单元，并剔除剩下的无用信息，分词器十分关键，不同的分词器，解析相同的文本，差异度会比较大。

**搜索过程核心类**

* IndexSearcher：调用search，用于搜索IndexWriter所创建的索引
* Term：Term适用于搜索一个基本单元
* Query：QueryLucene中含有多种查询Query的子类，他们用于查询条件的限定其中TermQuery是Lucene提供的最为基本的查询类型，主要用来匹配在指定的域Field中包含了特定项Term的文档
* TermQuery：Query的下一个子类单词条查询
* TopDocs：是一个存放有序搜索结果指针的简单容器，在这里搜索的结果是指匹配一个查询条件的一系列的文档

> ElasticSearch实现了对Lucene的封装，提供了REST API的操作接口，开箱即用。

## 2. ElasticSearch

### 2.1 ElasticSeach简介

> 百度百科：
>
> “Elasticsearch是一个基于[Lucene](https://baike.baidu.com/item/Lucene/6753302)的搜索服务器。它提供了一个分布式多用户能力的全文搜索引擎，基于RESTful web接口。”
>
> Elasticsearch 是一个分布式、高扩展、高实时的搜索与数据分析引擎。它能很方便的使大量数据具有搜索、分析和探索的能力。充分利用Elasticsearch的水平伸缩性，能使数据在生产环境变得更有价值。Elasticsearch 的实现原理主要分为以下几个步骤，首先用户将数据提交到Elasticsearch 数据库中，再通过分词控制器去将对应的语句分词，将其权重和分词结果一并存入数据，当用户搜索数据时候，再根据权重将结果排名，打分，再将返回结果呈现给用户。

### 2.2 ElasticSearch安装

```shell
# -e表示设置环境变量参数
docker run --name elasticsearch -p 9200:9200 -p 9300:9300  -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms64m -Xmx128m" -v /mydata/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml -v /mydata/elasticsearch/data:/usr/share/elasticsearch/data -v /mydata/elasticsearch/plugins:/usr/share/elasticsearch/plugins -d elasticsearch:7.6.2

其中elasticsearch.yml是挂载的配置文件，data是挂载的数据，plugins是es的插件，如ik，而数据挂载需要权限，需要设置data文件的权限为可读可写,需要下边的指令。
chmod -R 777 要修改的路径

-e "discovery.type=single-node" 设置为单节点
特别注意：
-e ES_JAVA_OPTS="-Xms256m -Xmx256m" \ 测试环境下，设置ES的初始内存和最大内存，否则导致过大启动不了ES

docker run --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms64m -Xmx128m" -v /data/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml -v /data/elasticsearch/data:/usr/share/elasticsearch/data -v /data/elasticsearch/plugins:/usr/share/elasticsearch/plugins -d elasticsearch:7.6.2 
```

#### **安装采用docker拉取镜像安装**

```shell
# 查找
docker search elasticsearch
# 拉取
docker pull elasticsearch
# 创建需要与容器关联的容器卷文件夹(也可以在运行的时候让docker其自动创建)
mkdir -p /data/elasticsearch/data
mkdir -p /data/elasticsearch/config
mkdir -p /data/elasticsearch/plugins
# 给文件夹赋予权限
chmod -R 777 elasticsearch
# 提供host，以便任意机器可以访问，注意，正式工作上线需要限制ip地址
echo “http.host: 0.0.0.0" >> /data/elasticsearch/config/elasticsearch.yml
# 启动elasticsearch容器,--name设置容器启动后的名称 -p设置端口对应，-e设置环境变量参数，-v设置对应的容器卷，-d表示静默运行
docker run --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e ES_JAVA_OPTS="-Xms64m -Xms128m" -v /data/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml -v /data/elasticsearch/data:/usr/share/elasticsearch/data -v /data/elasticsearch/plugins:/usr/share/elasticsearch/plugins -d elasticsearch:7.6.2
# 启动之后可以使用docker ps查看当前运行的容器
docker ps
# 进入到运行的elasticsearch中
docker exec -it bc6f9a91865e /bin/bash
```

**进入到elasticsearch容器中之后**

```shell
# 可以查看到具体的信息即表示安装成功
curl http://localhost:9200
```

![img](https://gitee.com/Caoduanxi/picture/raw/master/2021/1/1-8-19-42-38.jpg)

**安装kibana**

> Kibana是一个可视化的elasticsearch插件，通过kibana可以更好的操作elasticsearch

```shell
# docker直接pull拉取
docker pull kibana
# 运行kibana，这里本来是要建立容器卷kibana.yml与本地文件关联的，不知道为啥就是关联不起来！
docker run -it --name kibana -p 5601:5601 -e "ELASTICSEARCH_HOSTS=http://localhost:9200" -d kibana:7.6.2
# 进入到kibana中
docker exec -it xxxxid /bin/bash
# 修改kibana.yml配置
vim kibana.yml
```

```yml
# 主要的修改配置
elasticsearch.url: 'http://ip地址:9200'
# 修改完成之后重启容器即可
docker ps -l
docker restart 容器id
# 通过服务器访问http://ip:5601即可获取到具体页面
```

**注意(低版本的才会有这种情况,第一次直接docker pull拉到的是5.6.12，后面改成了7.6.2就不存在下列的问题)**

> 上面修改`kibana.yml`文件的时候会报`vim或者vi`命令，此时需要`apt-get update`进行升级，然后`apt-get install vim`进行安装。(这里网速可能会比较慢，只能慢慢等待了-_-||)

![img](https://gitee.com/Caoduanxi/picture/raw/master/2021/1/1-8-20-40-34.jpg)

#### **安装ik分词器**

```shell
# 在plugins下面创建文件夹
mkdir ik-analyzer
# 首先下载ik分词器（建议使用wget，不然直接去github上下载很慢）
wget https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.6.2/elasticsearch-analysis-ik-7.6.2.zip
# unzip解压缩
unzip elasticsearch-analysis-ik-7.6.2.zip
# 重启es即可
docker restart [容器id]
```

```shell
# 这个主要有两种模式，一个ik_smart模式，一个ik_max_word
GET _analyze
{
  "analyzer": "ik_max_word",
  "text": "我是中国人"
}

GET _analyze
{
  "analyzer": "ik_smart",
  "text": ["我是中国人","我叫曹端喜"]
}
```

### 2.3 ElasticSearch具体操作

> ElasticSearch支持PUT、POST、GET、DELETE等操作

**ES核心概念**

* **索引**：库，用来存放多个文档（数据）
* **字段类型**：即数据类型的映射，最好是提前定义数据类型
* **文档**：记录
* **分片**：每个分片实质是一个Lucene索引，Lucene是倒排索引

ES是面向文档的，一切皆为JSON，与传统关系型数据库对比

| 关系型数据库   | ElasticSearch |
| -------------- | ------------- |
| 数据库Database | 索引index     |
| 表table        | 类型type      |
| 行row          | 文档document  |
| 字段column     | 域field       |

**document**

> 一个索引可以包含多个文档，文档是表格中的一条条数据，文档对应着数据库中的行，即一条完整的对象的记录。
>
> **文档的重要属性**
>
> * 自我包含：一篇文档同时存在包含字段和对应的值，即同时包含key:value
> * 层次性：文档包含自文档，实质即一个json对象
> * 灵活的结构：文档不依赖预定义的模型，在关系型数据库中，提前定义字段才可以用，ES中字段可以被忽略，或者使用的时候添加即可。

**type**

> 即数据库的表table，表格是行的容器。

**index**

> 索引，即数据库，索引是映射类型的容器，es中的索引是一个非常大的文档集合，索引中存储了映射类型的字段和其他设置，数据被存储到各个分片上。

```json
# 向customer库中的duanxi表中存放一条id=2的数据
PUT customer/duanxi/2
{
  "name":"zhangsan",
  "age":24,
  "school":"njupt"
}
# 获取到的结果
{
  "_index": "customer",
  "_type": "duanxi",
  "_id": "2",
  "_version": 1,
  "found": true,
  "_source": {
    "name": "zhangsan",
    "age": 24,
    "school": "njupt"
  }
}
```

#### **PUT&POST操作**

```json
// 指定了id的话，此时会直接插入在id=2的位置
PUT customer/duanxi/2
{
  "name":"zhangsan",
  "age":24,
  "school":"njupt"
}
// 不指定id的话,如果没有提前建立索引的话,此时的id会随机生成
POST customer/duanxi/
{
  "name":"chenxiang",
  "age":25,
  "school":"NJUPT"
}
```

> `PUT`和`POST`如果当前数据之前不存在则`created`，如果之前存在则为`updated`状态。

#### **GET操作**

```json
GET customer/duanxi/3
// 获取到的结果
{
  "_index": "customer",
  "_type": "duanxi",
  "_id": "3",
  "_version": 2,
  "found": true,
  "_source": {
    "name": "zhoumin",
    "age": 22,
    "school": "hlg"
  }
}
```

#### **DELETE操作**

```json
DELETE customer/duanxi/3
// 删除返回的结果
{
  "found": true,
  "_index": "customer",
  "_type": "duanxi",
  "_id": "3",
  "_version": 3,
  "result": "deleted",
  "_shards": { // 分片
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}
```

****

#### 批量操作数据

> 批量操作数据，一般用于添加`_bulk`表示使用批量操作

```json
// _bulk后面接着index表示插入在哪里，index后面即为需要插入的数据
POST customer/duanxi/_bulk
{"index":{"_id":1}}
{"name":"11111"}
{"index":{"_id":2}}
{"name":"2222"}
```

#### 查询

> 查询可以像关系型数据库一样，数据全部查询，也可以按照条件过滤查询

**查询所有**

```json
GET customer/duanxi/_search
// 结果
{
  "took": 1,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 1,
    "hits": [
      ....省略
    ]
  }
}
```

**按照条件过滤**

```json
GET customer/duanxi/_search
{
  "query": {
    "match": {
      "name": "11111"
    }
  }
}
// 结果
{
  "took": 4,
  "timed_out": false,
  "_shards": {
    "total": 5,
    "successful": 5,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": 1,
    "max_score": 0.2876821,
    "hits": [
      {
        "_index": "customer",
        "_type": "duanxi",
        "_id": "1",
        "_score": 0.2876821,
        "_source": {
          "name": "11111"
        }
      }
    ]
  }
}
```

```json
# sort表示可以按照某个字段进行排序
# match_phrase表示使用精确匹配
# multi_match表示多个字段匹配
// 表示多个字段匹配查询，查询的值为hlg,字段可以从name或者school字段获取
GET customer/duanxi/_search
{
  "query": {
    "multi_match": {
      "query": "hlg",
      "fields": ["name","school"]
    }
  }
}
```

**bool可以开启复合查询**

```json
# 下列查询表示查询的name=周敏&&school=hlg&&age=22
GET customer/duanxi/_search
{
  "query": {
    "bool": {
      "must": [
        {"match": {
          "name": "zhoumin"
        }},
        {
          "match": {
            "school": "hlg"
          }
        }
      ],
      "should": [
        {"match": {
          "age": "22"
        }}
      ]
    }
  }
}
```

**filter过滤查询**

```json
# 查询过滤的结果，结果中的10≤age≤30
GET customer/duanxi/_search
{
  "query": {
    "bool": {
      "filter": {
        "range": {
          "age": {
            "gte": 10,
            "lte": 30
          }
        }
      }
    }
  }
}
```

**term查询**

> 精准字段使用`term`查询

```json
# 精确查询年龄字段值为25的数据
GET customer/duanxi/_search
{
  "query": {
    "term": {
      "age": {
        "value": "25"
      }
    }
  }
}
```

**keyword**

```json
# 有keyword的时候，会执行精确查找，如果没有，此值会当成一个关键字
GET customer/duanxi/_search
{
  "query": {
    "match": {
      "name.keyword": "chenxiang"
    }
  }
}
```

#### 聚合查询

> `MySQL`中的聚合函数使用的是`group by`等对某个字段进行聚合操作。而在`ES`中，也存在具体的聚合函数，使用`aggs`表示使用聚合函数，`terms`表示为结果求和，`age`表示求平均值，`size`设置为0，则表示不显示详细信息

```json
# 表示对学校为njupt的所有数据中的age年龄进行聚合，求和并且取平均值，最后省略数据信息，只输出聚合的相关信息
GET customer/duanxi/_search
{
  "query": {
    "match": {
      "school": "njupt"
    }
  },
  "aggs": {
    "ageagg": {
      "terms": {
        "field": "age",
        "size": 10
      }
    },
    "ageavg":{
      "avg": {
        "field": "age"
      }
    }
  },
  "size": 0
}
```

**聚合中的子聚合函数**

```json
# 表示对查询出的age中的乘积做平均值操作
GET customer/duanxi/_search
{
  "query": {
    "match": {
      "school": "njupt"
    }
  },
  "aggs": {
    "ageagg": {
      "terms": {
        "field": "age",
        "size": 10
      },
      "aggs": {
        "ageAvg": {
          "avg": {
            "field": "grade"
          }
        }
      }
    }
  }
}
```

```json
// 可以获取到具体的一个索引库信息
GET _cat/indices?v
```

`keyword`类型是默认的类型，这种不会被分词器解析，而非`keyword`的类型是会被分词器分割解析。

> 查询的时候需要注意，使用查询的时候无论是使用`match`还是`term`注意，如果不限定当前的字段为`keyword`会被默认分割，导致只要是其中的字段均会被查询出来

## 3. Java整合ElasticSearch

> 能力还是差距很大啊，很多东西自己没有能够沉下心去好好看，只想着图速度快，总想着可以快速学完一些东西，但是实际上这些快的速度正是自己学习的障碍，不仅要速度，还要效率啊，没有知识转换比，一切都是没有用的！加油！2021年1月8日22:43:37 有感而发

> 如果Springboot访问不到static下的文件，建议使用maven的`clean`然后再进行项目启动即可

Springboot整合ElasticSearch的话

```xml
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>7.6.2</version>
</dependency>
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-elasticsearch</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.68</version>
</dependency>
```

构造获取配置的具体类

```java
@Configuration
public class ElasticSearchClientConfig {
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ));
    }
}
```

利用`@Test`进行测试

> 注意利用`Junit`进行测试的时候需要注解`@SpringBootTest @RunWith(SpringRunner.class)`

```java
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
```

## 4. 仿查询高亮显示

> 仿查询高亮显示，成果如下所示。
>
> 具体使用的技术是：SpringBoot+ElasticSearch+Vue+Thymeleaf技术，利用ajax去异步调用后台服务实现数据的获取。数据爬取自京东商城。

![img](https://gitee.com/Caoduanxi/picture/raw/master/2021/1/1-9-16-37-4.jpg)

**高亮部分代码**

```java
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
```

**Jsoup爬取网页内容代码**

```java
public static List<Good> getJDGoods(String kewWord) throws IOException {
    List<Good> list = new ArrayList<>();
    Document document = Jsoup.parse(new URL("http://search.jd.com/Search?keyword=" + kewWord), 30000);
    // 操作与css操作基本一致
    Element element = document.getElementById("J_goodsList");
    Elements lis = element.getElementsByTag("li");
    for (Element el : lis) {
        String image = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
        String title = el.getElementsByClass("p-name").eq(0).text();
        String price = el.getElementsByClass("p-price").eq(0).text();
        list.add(new Good(image, title, price));
    }
    return list;
}
```

## 5. 小结

之前粗略的学习过一次基本没有留下任何的印象，这次再学，虽然有些急躁，在很多地方出现了很多的问题，但也对其中的具体使用有些印象了。ElasticSearch主要是基于Lucene引擎实现的，底层是倒排索引的原理所以速度很快，可以达到实时查询的速度。安装的话采用Docker安装，也遇到了不少的坑，不过也复习了一下Docker知识。后面的话就是ES的具体的增删改查操作，现在官方建议直接就是index，不需要所谓的type，即现在只需要库了，消除了表，直接传入数据即文本document了，查询的话，可以使用query搭配match等相关标签，需要注意`keyword`单词，如果是`name.keyword`表示当前查询是不可被分割的，必须查到一样的。但是如果没有使用`keyword`则表示可以拆分，只要查到部分，都可以将结果作为展示部分。既然是查询就可以进行聚合操作`aggs`，有主聚合和子聚合，作用的位置有所区别。

最后是通过一个小的搜索页面将后端与前端关联起来了，对结果的展示进行了一个可视化的展示。

> Keep thinking, keep coding! 2021-1-9 16:51:44写于南京 加油！