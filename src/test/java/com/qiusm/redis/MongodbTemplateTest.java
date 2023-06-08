package com.qiusm.redis;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.qiusm.redis.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.DbCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author qiushengming
 */
@Slf4j
public class MongodbTemplateTest extends RedisApplicationTests {
    @Resource
    private MongoTemplate mongoTemplate;


    @Test
    public void getMongoTemplate() {
        log.info("{}", mongoTemplate);
    }

    @Test
    public void template() {
        IndexOperations ops = mongoTemplate.indexOps(UserEntity.class);
    }

    /**
     * 1.使用save函数里，如果原来的对象不存在，那他们都可以向collection里插入数据。
     * 如果已经存在，save会调用update更新里面的记录，而insert则会忽略操作。<br>
     * <p>
     * 2. insert可以一次性插入一个列表，而不用遍历，效率高， save则需要遍历列表，一个个插入。 <br>
     */
    @Test
    public void insert() {
        String[] username = new String[]{"qiusm", "luyun"};
        List<UserEntity> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserEntity user = new UserEntity();
            user.setId((long) i);
            user.setUsername(username[i % username.length]);
            user.setPassword(user.getUsername());
            user.setCreateTime(LocalDateTime.now());
            users.add(user);
        }
        // insert 时当主键已存在则会报重复键的错。效率高
        // UserEntity result = mongoTemplate.insert(user);
        // save 时当主键已存在则回去更新。效率低，因为存在检查是否存在的操作
        // UserEntity result = mongoTemplate.insert(user);
        mongoTemplate.dropCollection(UserEntity.class);
        mongoTemplate.createCollection(UserEntity.class);
        Collection<UserEntity> result = mongoTemplate.insertAll(users);
        log.info("{}", result);
    }

    /**
     * 聚合操作
     */
    @Test
    public void agg() {
        /*
        // 匹配条件
        Aggregation.match(Criteria.where("start_time")
                .gte(startTime.toEpochSecond(ZoneOffset.of("+8")))
                .lt(endTime.toEpochSecond(ZoneOffset.of("+8")))
                .and("gender").is(1)
                .and("test_code").in("qqqq", "wwww")),

        // 关联查询
        // .lookup(String fromCollectionName, String localFeild, String foreginFeild, String as)
        // 关联集合名称、主表关联字段、从表关联字段、别名
        Aggregation.lookup("collection_two", "test_code", "test_code", "detail"),

        // 分组查询
        // .first()、.last()、.min()、max()、.avg()、.count() 后需调用 .as() 对结果值进行重命名
        Aggregation.group("test_code")   // 分组条件
                .first("test_code").as("test_code")   // 获取分组后查询到的该字段的第一个值
                .first("detail.test_name").as("test_name")
                .sum("age").as("age")   // 按分组对该字段求和
                .min("age").as("min_age")   // 按分组求该字段最小值
                .max("age").as("max_age")   // 按分组求该字段最大值
                .avg("age").as("avg_age")   // 按分组求该字段平均值
                .first("start_time").as("start_time")
                .last("end_time").as("end_time")   // 获取分组后查询到的该字段的最后一个值
                .first("gender").as("gender")
                .count().as("count"),   // 统计分组后组内条数

        Aggregation.sort(Sort.Direction.DESC, "avg_age"),   // 排序

        Aggregation.skip(0L),   // 分页
        Aggregation.limit(10L),

        // 输出的列
        Aggregation.project("test_code", "test_name", "start_time", "end_time", "age",
                        "min_age", "max_age", "avg_age", "gender", "count")
        * */
        Aggregation aggregation = Aggregation.newAggregation(
                UserEntity.class,
                // Aggregation.match(Criteria.where("username").is("qiusm")),
                Aggregation.sort(Sort.Direction.ASC, "password"),
                Aggregation.group("username")
                        .last("username").as("username")
                        .last("password").as("pw")
                        // .addToSet("password").as("pw_set")
                        .last("id").as("last_id")
                        .last("createTime").as("last_ct")
                ,
                Aggregation.sort(Sort.Direction.ASC, "username"),
                Aggregation.skip(0L),
                Aggregation.limit(10L)
                // Aggregation.project("id", "maxId", "username", "password", "createTime")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "user", Map.class);
        log.info("{}", JSONObject.toJSONString(results.getMappedResults()));


    }

    @Test
    public void agg1() {
        LookupOperation lookup = Aggregation.lookup("indicator", "indicator_list_json.id", "id", "items");
        LookupOperation lookup1 = Aggregation.lookup("indicator", "indicator_list_json.version", "items.version",
                "items1");

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("version").is("3")),
                Aggregation.unwind("indicator_list_json"),
                lookup,
//                lookup1
                Aggregation.unwind("items"),
                lookup1

        );

        AggregationResults<Map> r = mongoTemplate.aggregate(agg, "indicator_publish", Map.class);
        log.info("1");

    }

    @Test
    void agg2() {
        Bson g1 = Filters.eq("version", "3");
        BsonDocument bsonMatch = BsonDocument.parse("{$match: {version: \"3\"}}");
        BsonDocument bsonUnwind = BsonDocument.parse("{$unwind: \"$indicator_list_json\"}");
        BsonDocument bsonLockup = BsonDocument.parse("{$lookup: {from: \"indicator\", let: {pid: " +
                "'$indicator_list_json.id', pversion: '$indicator_list_json.version'}, pipeline: [{$match: {$expr: " +
                "{$and: [{$eq: [{$toString: '$id'}, {$toString: '$$pid'}]}, {$eq: [{$toString: '$version'}, " +
                "{$toString: '$$pversion'}]}]}}}], as: \"items\"}}");
        BsonDocument bsonUnwindItems = BsonDocument.parse("{$unwind: \"$items\"}");
        AggregateIterable<Map> r = mongoTemplate.getDb().getCollection("indicator_publish").aggregate(
                Arrays.asList(bsonMatch, bsonUnwind, bsonLockup, bsonUnwindItems),
                Map.class
        );
        for (Map next : r) {
            log.info("{}", next);
            Object id = ((Document) next.get("indicator_list_json")).get("id");
            Object version = ((Document) next.get("indicator_list_json")).get("version");

            Object pid = ((Document) next.get("items")).get("id");
            Object pversion = ((Document) next.get("items")).get("version");

            log.info("id:pid => {}:{}; version:pversion => {}:{}", id, pid, version, pversion);

        }
    }

    /**
     * 执行mongo原生脚本
     */
    @Test
    void bson() {
        String s = "[\n" +
                "    {$match: {version: \"3\"}},\n" +
                "    {$unwind: \"$indicator_list_json\"},\n" +
                "    {$lookup: {from: \"indicator\", let: {pid: '$indicator_list_json.id', pversion: " +
                "'$indicator_list_json.version'}, pipeline: [{$match: {$expr: {$and: [{$eq: [{$toString: '$id'}, " +
                "{$toString: '$$pid'}]}, {$eq: [{$toString: '$version'}, {$toString: '$$pversion'}]}]}}}], as: " +
                "\"items\"}}, {\n" +
                "        $unwind: \"$items\"\n" +
                "    }]";
        JSONArray array = JSONObject.parseArray(s);
        List<Bson> list = new ArrayList<>();
        array.forEach(o -> {
            list.add(BsonDocument.parse(JSONObject.toJSONString(o)));
        });
        BasicQuery basicQuery = new BasicQuery(s);
        // mongoTemplate.getDb().getCollection("indicator_publish").a`

        AggregateIterable<Map> r = mongoTemplate.getDb().getCollection("indicator_publish").aggregate(
                list,
                Map.class
        );
        for (Map next : r) {
            log.info("{}", next);
            Object id = ((Document) next.get("indicator_list_json")).get("id");
            Object version = ((Document) next.get("indicator_list_json")).get("version");

            Object pid = ((Document) next.get("items")).get("id");
            Object pversion = ((Document) next.get("items")).get("version");

            log.info("id:pid => {}:{}; version:pversion => {}:{}", id, pid, version, pversion);

        }
    }

    @Test
    void bulkOps() {
        String[] username = new String[]{"limengya", "qiusm", "luyun", "guobailu", "zhangyiwen", "zhouziyi"};
        List<UserEntity> users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserEntity user = new UserEntity();
            user.setId((long) i);
            user.setUsername(username[i % username.length]);
            user.setPassword(user.getUsername());
            user.setCreateTime(LocalDateTime.now());
            users.add(user);
        }
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "user1");
        bulkOps.insert(users);
        bulkOps.execute();
    }

    @Test
    void findAndModify() {
        Update update = new Update().inc("age", 29);
        UserEntity r = mongoTemplate.update(UserEntity.class)
                .matching(Criteria.where("username").is("luyun"))
                .apply(update)
                .findAndModifyValue(); // return's old person object
        log.info("r:{}", JSONObject.toJSONString(r));
    }

    @Test
    void mongoOperationsMethodTests() {
        // 创建集合，可以通过Class，或者集合名称进行创建
        // 集合配置：CollectionOptions
        Long size = 100L;
        Long maxDocumets = 100L;
        // 可选）如果为 true，则创建固定集合。固定集合是指有着固定大小的集合，当达到最大值时，它会自动覆盖最早的文档。当该值为 true 时，必须指定 size 参数。
        Boolean capped = true;
        CollectionOptions collectionOptions = new CollectionOptions(size, maxDocumets, capped);
        mongoTemplate.createCollection("");

        // ----
        // https://docs.spring.io/spring-data/mongodb/docs/3.4.0-RC1/reference/html/#mongo.executioncallback
        mongoTemplate.execute(new DbCallback<Object>() {

            @Override
            public Object doInDB(MongoDatabase db) throws MongoException, DataAccessException {
                db.getCollection("user");
                return null;
            }
        });

        // insert
        mongoTemplate.insert(null);

        // find
        mongoTemplate.findById(null, null);
        BasicQuery query = new BasicQuery("{}");
        // mongoTemplate.findø(query, UserEntity.class);
        // update
        // mongoTemplate.updateFirst()
    }

    private static final String tableValue = "vd_human_code,vd_code,vds_code,vds_name,vd_cname,vd_ename," +
            "vd_source_code,vd_source_name,is_standard,vd_ns_name,remarks,value_list_json,version,status,update_time," +
            "submit_org_code,submit_org_name,submit_time,audit_org_code,audit_org_name,audit_time,preserving_hash," +
            "descn";
    private static final String[] fields = StringUtils.split(tableValue, ",");

    /**
     * 排序增强
     * <a href="https://docs.spring.io/spring-data/mongodb/docs/3.4.0-RC1/reference/html/#mongo.core">refernce 11.6.7</a>
     * <br>
     */
    @Test
    void sortSuppor() {
        Collation VERSION_COLLATION = Collation.of("zh").numericOrderingEnabled();
        Document document = new Document();
        AggregationOptions options = AggregationOptions.builder()
                .collation(VERSION_COLLATION)
                // 可选的。允许写入临时文件。当设置为 时true，聚合阶段可以将数据写入_tmp目录中的子目录 dbPath。
                //.allowDiskUse(true)
                //可选的。指定处理游标操作的时间限制（以毫秒为单位）。
                //如果您不指定 maxTimeMS 的值，则操作不会超时。值0明确指定默认的无界行为。
                //.maxTime(Duration.ZERO)
                //可选的。指定返回有关管道处理的信息。 理论上应该是跟mysql的执行计划类似
                //.explain(true)
                //定义一个注释表示当前运行的内容是什么
                //.comment("test")
                //.cursor(document)
                .build();
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.project(fields));
        operations.add(Aggregation.sort(Sort.Direction.DESC, "version"));
//        operations.add(Aggregation.group("vd_human_code", "vd_code")
//                .first("vd_cname").as("vd_cname")
//                .first("version").as("version"));
        //每个id，version找出最新一条，因为只有最新版本的数据才有业务意义，其他的版本只能作为日志
        // operations.add(Aggregation.sort(Sort.Direction.DESC, "version"));
        operations.add(Aggregation.limit(10L));
        //过滤最新的版本的数据找到需要的
        Aggregation agg = Aggregation.newAggregation(operations).withOptions(options);
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, "crf_info", Map.class);
        log.info("排序增强");
    }

    /**
     * 伪随机从结果中选择结果中的指定数量的数据
     */
    @Test
    void sample() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.sample(5)
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(agg, "crf_info", Map.class);
        log.info("{}", JSONObject.toJSONString(results.getMappedResults()));
    }

    /**
     * 强类型校验。
     * <a href="https://docs.spring.io/spring-data/mongodb/docs/3.4.0-RC1/reference/html/#mongo.core">refernce 11.6.7 JSON 模式</a>
     * <br>
     * 例如必填字段、字段类型。<br>
     */
    @Test
    void jsonSchema() {

    }

    @Test
    void count() {
        long r = mongoTemplate.estimatedCount(UserEntity.class);
        log.info("{}", r);
    }

    @Test
    void findUser() {
        List<UserEntity> var1 = mongoTemplate.find(Query.query(Criteria.where("username").is("limengya")),
                UserEntity.class);
        mongoTemplate.getDb()
                .withReadPreference(ReadPreference
                        .primary()
                        .withTagSet(new TagSet(new Tag("", ""))))
                .withReadConcern(ReadConcern.AVAILABLE);
        log.info("{}", JSONObject.toJSONString(var1));

    }

    @Test
    void timeSeries() {
        mongoTemplate.createCollection("a", CollectionOptions.timeSeries("timestamp"));
    }
}
