# AI 可执行落地实施计划 — 通用日历系统（Java 云原生 / 支持多关联项）

> 本计划对应设计文档：`通用日历系统数据模型设计（支持多关联项：学习_任务_提醒_备忘）.md`
> 目标读者：执行型 AI Agent（Claude Code / Cowork / 其他自动化代理）
> 执行原则：**每一阶段 = 可执行命令 + 可验证断言 + 明确的回滚策略**。AI 在未通过本阶段 "验证步骤" 前，不得进入下一阶段。
> 工作根目录约定：`$PROJECT_ROOT = /Users/allenwang/build/longfeng/design/calendar-platform`

---

## 0. 执行总览（Roadmap）

| 阶段 | 名称 | 预计耗时 | 产出物 | 强依赖 |
|------|------|---------|--------|--------|
| P0 | 环境体检与工具安装 | 10 min | 环境检查报告 `env-check.log` | 无 |
| P1 | Maven 多模块骨架 | 20 min | 8 个模块 `pom.xml` | P0 |
| P2 | 基础设施本地化（docker-compose） | 20 min | MySQL/Redis/RocketMQ/Nacos 全部 healthy | P0 |
| P3 | 数据库 DDL + 初始化数据 | 30 min | Flyway 迁移全部 SUCCESS | P2 |
| P4 | 通用 common 模块（基类 / 工具 / 异常） | 40 min | `common-core` 构建通过 | P1 |
| P5 | Calendar Core Service（核心域） | 90 min | CRUD + 关联接口 e2e 通过 | P3, P4 |
| P6 | User Setting Service | 40 min | 用户偏好 / 时区接口通过 | P4 |
| P7 | Calendar Reminder Service + XXL-Job | 60 min | 定时扫描任务可跑通 | P5 |
| P8 | Notification Service（多渠道） | 40 min | Mock 渠道推送通过 | P5 |
| P9 | Spring Cloud Gateway | 30 min | 统一路由 + JWT + X-Timezone 透传 | P5~P8 |
| P10 | 国际化与时区全链路 | 30 min | i18n 单元测试 100% 通过 | P5 |
| P11 | 分层缓存（Caffeine + Redis） | 40 min | 缓存命中率测试 ≥ 90% | P5 |
| P12 | ShardingSphere 分库分表 | 50 min | 分片路由单测通过 | P3 |
| P13 | 集成测试 + 契约测试 | 60 min | Testcontainers 全绿 | P5~P9 |
| P14 | Kubernetes 部署清单 | 40 min | `kubectl apply --dry-run` 通过 | P9 |
| P15 | 可观测性（Prometheus/Grafana/ELK） | 30 min | `/actuator/prometheus` 返回指标 | P14 |
| P16 | 端到端冒烟用例 | 20 min | 冒烟脚本 `smoke.sh` 全部 PASS | 全部 |

**总里程碑**：P0→P16 全部 PASS 即视为 MVP 落地完成。

---

## P0. 环境体检与工具安装

### 目标
确认执行机上存在 JDK 17+、Maven 3.9+、Docker Desktop（含 Compose v2）、kubectl、Git，版本齐备。

### AI 执行步骤

1. 在 `$PROJECT_ROOT` 下创建目录：
   ```bash
   mkdir -p /Users/allenwang/build/longfeng/design/calendar-platform && cd $_
   ```
2. 写入环境检查脚本 `scripts/env-check.sh`：
   ```bash
   #!/usr/bin/env bash
   set -e
   {
     echo "=== JDK ===";        java -version 2>&1
     echo "=== Maven ===";      mvn -v
     echo "=== Docker ===";     docker --version
     echo "=== Compose ===";    docker compose version
     echo "=== kubectl ===";    kubectl version --client=true --output=yaml || true
     echo "=== Git ===";        git --version
   } | tee env-check.log
   ```
3. 执行：`bash scripts/env-check.sh`

### 验证步骤（AI 自校验断言）
- 断言 `env-check.log` 包含字符串：`openjdk version "17` 或更高、`Apache Maven 3.9`、`Docker version 24` 或更高、`Compose version v2`。
- 命令：
  ```bash
  grep -E "openjdk version \"(1[7-9]|[2-9][0-9])" env-check.log
  grep -E "Apache Maven (3\.9|[4-9])" env-check.log
  grep -E "Docker version (2[4-9]|[3-9][0-9])" env-check.log
  ```
- 任一 `grep` 返回非 0 即视为失败。

### 回滚
- 若 JDK/Maven 缺失：提示用户安装 SDKMAN `curl -s "https://get.sdkman.io" | bash && sdk install java 17.0.10-tem && sdk install maven 3.9.6`，AI 不可擅自 `sudo`。

---

## P1. Maven 多模块骨架

### 目标
生成符合 DDD 边界上下文的 8 模块工程：
```
calendar-platform/
├── pom.xml                        # 根 POM（dependencyManagement）
├── common-core/                   # 基类、异常、工具、TenantContext
├── common-api/                    # OpenFeign 客户端 + DTO
├── calendar-core-service/         # P5
├── user-setting-service/          # P6
├── calendar-reminder-service/     # P7
├── notification-service/          # P8
├── gateway-service/               # P9
└── integration-test/              # P13
```

### AI 执行步骤

1. 生成根 `pom.xml`：`<packaging>pom</packaging>`，`<modules>` 列出上面 8 个；`dependencyManagement` 中固定：
   - Spring Boot `3.2.5`
   - Spring Cloud `2023.0.1`
   - Spring Cloud Alibaba `2023.0.1.0`
   - MyBatis-Plus `3.5.5`
   - ShardingSphere-JDBC `5.4.1`
   - Redisson `3.27.0`
   - XXL-Job `2.4.1`
   - Testcontainers `1.19.7`
2. 每个子模块的 `pom.xml`：声明 `<parent>` 指向根 POM；禁止在子模块重复声明 Spring Boot 版本。
3. 在根目录建立 `.editorconfig`、`.gitignore`（忽略 `target/`、`*.iml`、`logs/`）、`README.md`（指向本计划文件）。

### 验证步骤
```bash
cd /Users/allenwang/build/longfeng/design/calendar-platform
mvn -q -DskipTests -N verify         # 根 POM 可解析
mvn -q -DskipTests validate          # 全部子模块结构合法
test -f common-core/pom.xml && test -f gateway-service/pom.xml
```
断言：8 个子模块 `pom.xml` 均存在、`mvn validate` 退出码为 0。

### 回滚
- 删除 `$PROJECT_ROOT` 并重跑 P1。

---

## P2. 基础设施本地化（docker-compose）

### 目标
一键拉起 MySQL 8.0（1 主 2 从）、Redis Cluster（3M3S）、RocketMQ（1 nameserver + 1 broker）、Nacos 2.3、XXL-Job-Admin、Elasticsearch 8、Kibana、Prometheus、Grafana，全部 healthy。

### AI 执行步骤

1. 写入 `infra/docker-compose.yml`；关键要点：
   - MySQL 容器使用 `server_id` 区分，从库启动 `CHANGE MASTER TO` 脚本。
   - Redis 使用 `redis:7.2` + `redis-cli --cluster create ...` 初始化。
   - Nacos `MODE=standalone`、`SPRING_DATASOURCE_PLATFORM=mysql` 指向 MySQL 主库。
   - 统一网络 `calendar-net`，所有端口 bind `127.0.0.1` 避免暴露。
2. 写入启动脚本 `infra/up.sh`：
   ```bash
   #!/usr/bin/env bash
   set -e
   docker compose -f infra/docker-compose.yml up -d
   echo "Waiting for MySQL..."
   for i in $(seq 1 30); do
     docker compose -f infra/docker-compose.yml exec -T mysql-master \
       mysqladmin ping -uroot -proot --silent && break
     sleep 2
   done
   echo "Waiting for Nacos..."
   for i in $(seq 1 60); do
     curl -sf http://127.0.0.1:8848/nacos/actuator/health >/dev/null && break
     sleep 2
   done
   ```

### 验证步骤
```bash
bash infra/up.sh
docker compose -f infra/docker-compose.yml ps --format json | \
  jq -r '.[] | select(.State!="running") | .Name' | \
  (! grep .)                             # 没有非 running 容器
curl -fsS http://127.0.0.1:8848/nacos/actuator/health | grep -q '"status":"UP"'
redis-cli -h 127.0.0.1 -p 7000 cluster info | grep -q 'cluster_state:ok'
curl -fsS http://127.0.0.1:9200 | grep -q '"tagline"'
```
全部断言通过 = P2 PASS。

### 回滚
`docker compose -f infra/docker-compose.yml down -v` 后重试；若仍失败，打印 `docker compose logs` 最后 200 行并停止。

---

## P3. 数据库 DDL 与 Flyway 初始化

### 目标
将设计文档第二章的 DDL 完整落地为 Flyway 迁移脚本，并在 MySQL 主库执行成功。

### AI 执行步骤

1. 在 `calendar-core-service/src/main/resources/db/migration/` 下创建：
   - `V1__create_calendar_main.sql` — 见设计文档 §2.2.1。
   - `V2__create_calendar_relation.sql` — §2.2.2。
   - `V3__create_business_tables.sql` — `kp_learn` / `task_info` / `remind_info` / `memo_info`。
   - `V4__create_calendar_reminder.sql` — 字段：`id, tenant_id, calendar_id, trigger_time, is_triggered, retry_count, ...`。
   - `V5__create_calendar_notify_record.sql` — 唯一键 `uk_tenant_calendar_channel`。
   - `V6__create_calendar_operation_log.sql` — 审计字段。
   - `V7__create_sys_user_and_setting.sql` — `user_setting`（`default_remind_minute`、`notify_channels`、`time_zone`、`locale`）。
2. 所有表：`ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`；时间列注释 `UTC+0`；必须包含 `is_deleted` / `create_time` / `update_time` / `create_by` / `last_modified_by`。
3. 在 `calendar-core-service/pom.xml` 引入 `flyway-core` + `flyway-mysql`。
4. 运行 Flyway：
   ```bash
   mvn -pl calendar-core-service flyway:migrate \
     -Dflyway.url=jdbc:mysql://127.0.0.1:3306/calendar_db \
     -Dflyway.user=root -Dflyway.password=root
   ```

### 验证步骤
```bash
docker compose -f infra/docker-compose.yml exec -T mysql-master mysql -uroot -proot -Ncalendar_db \
  -e "select table_name from information_schema.tables where table_schema='calendar_db' order by 1" \
  > /tmp/tables.txt
for t in calendar_main calendar_relation calendar_reminder calendar_notify_record \
         calendar_operation_log kp_learn task_info remind_info memo_info user_setting; do
  grep -qx "$t" /tmp/tables.txt || { echo "MISSING: $t"; exit 1; }
done
# 关键字段 & 索引抽检
docker compose -f infra/docker-compose.yml exec -T mysql-master mysql -uroot -proot -Ncalendar_db \
  -e "show index from calendar_main where Key_name='idx_tenant_user_date_status'" | grep -q calendar_date
```
全部断言通过 = P3 PASS。

### 回滚
`mvn -pl calendar-core-service flyway:clean -Dflyway.cleanDisabled=false`，修正 SQL 后重跑。

---

## P4. common-core 模块

### 目标
沉淀跨服务复用能力：`TenantAuditEntity` / `TenantContextHolder` / `TimeZoneContextHolder` / `BusinessException` / `ApiResponse<T>` / `IdGenerator`（雪花）/ `TimeUtils`（UTC↔Zone）/ `JsonUtils` / `RequestIdFilter`。

### AI 执行步骤

1. `TenantAuditEntity`：严格按设计文档 §2.1.1 实现，使用 JPA 注解；Hibernate Filter `tenantFilter` 在 `AbstractJpaRepository` 中统一 `enableFilter`。
2. `TenantContextHolder` / `TimeZoneContextHolder`：基于 `TransmittableThreadLocal`（com.alibaba:transmittable-thread-local:2.14.5），保证异步线程可透传。
3. 在 `spring.factories`（或 `@AutoConfiguration`）注册自动配置，业务服务引入依赖即可生效。
4. 提供 `TenantFilter` Servlet 过滤器：从 JWT 里取 `tenantId`、`userId`、`timeZone`、`locale`，写入上下文；`finally` 中 `clear()`。

### 验证步骤
```bash
mvn -pl common-core -am test
# 断言关键类存在
for c in TenantAuditEntity TenantContextHolder TimeZoneContextHolder \
         ApiResponse BusinessException RequestIdFilter; do
  find common-core/src -name "$c.java" | grep -q . || { echo "MISSING: $c"; exit 1; }
done
```
同时编写单元测试覆盖：UTC → `Asia/Shanghai` 转换、ThreadLocal 在 `CompletableFuture.runAsync` 中透传。覆盖率 ≥ 80%（JaCoCo）。

### 回滚
修正后重跑 `mvn test`；若 JPA 依赖冲突，收敛到根 POM `dependencyManagement`。

---

## P5. Calendar Core Service（核心领域服务）

### 目标
实现设计文档 §5.1/5.2/5.3 的 3 大流程：多关联项接入、日历可视化、动态调整。

### AI 执行步骤（子阶段）

**P5.1 领域层**
- 实体：`CalendarMain`、`CalendarRelation`、`KpLearn`、`TaskInfo`、`RemindInfo`、`MemoInfo`。
- 值对象：`RelationType`（枚举 1-4）、`CalendarStatus`（枚举 0-4）。
- 领域事件：`CalendarEventCreatedEvent`、`CalendarEventUpdatedEvent`。

**P5.2 仓储层（MyBatis-Plus + JPA 混用策略）**
- 写路径走 JPA（自动审计/Filter）；读路径走 MyBatis-Plus（分页/动态 SQL 更顺手）。
- `CalendarMainRepository#findListByDateRange(tenantId, userId, start, end)` 使用覆盖索引 `idx_tenant_user_date_status`。

**P5.3 应用层（Service）**
- `CalendarEventAppService.createEvent(CreateEventCmd)`：
  1. 入参校验（`@Valid`）。
  2. 幂等：读取请求头 `Request-Id`，先查 `calendar_operation_log`。
  3. 写业务子表 → `@PostPersist` 发 `CalendarEventCreatedEvent`。
  4. 监听器写 `calendar_main` + `calendar_relation`（同一本地事务）。
  5. 发送 RocketMQ `calendar-event-created` 事件。
- `CalendarEventAppService.changeStatus(ChangeStatusCmd)`：Redisson 锁 `calendar:lock:{id}` + 状态机校验。

**P5.4 接口层（REST）**
- `POST /api/v1/calendar/events`（创建）
- `GET  /api/v1/calendar/events?startDate&endDate&view=day|week|month`
- `PATCH /api/v1/calendar/events/{id}/status`
- `DELETE /api/v1/calendar/events/{id}`（软删）
- 统一响应 `ApiResponse<T>`；错误码分段：`CAL-1xxx`（参数）、`CAL-2xxx`（业务）、`CAL-5xxx`（系统）。

### 验证步骤

1. 单元测试：`mvn -pl calendar-core-service test`，覆盖率 ≥ 75%。
2. SpringBootTest + Testcontainers（MySQL + Redis + RocketMQ）端到端：
   ```java
   @Test void createLearnEvent_shouldGenerateCalendarAndRelation() { ... }
   @Test void changeStatus_concurrentUpdate_onlyOneWins() { ... } // 用 CountDownLatch
   @Test void dayView_returnsUserTimezoneTime() { ... }          // Asia/Shanghai
   ```
3. 启动服务：
   ```bash
   mvn -pl calendar-core-service spring-boot:run &
   sleep 20
   curl -fsS -XPOST http://127.0.0.1:8081/api/v1/calendar/events \
     -H "Content-Type: application/json" -H "Request-Id: t-001" \
     -H "Authorization: Bearer <test-jwt>" \
     -d '{"title":"复习Java","relationType":1,"calendarDate":"2026-04-20","calendarTime":"14:30:00","timeZone":"Asia/Shanghai"}' \
     | jq -e '.code=="0"'
   ```

### 回滚
保留 Flyway 记录，清空业务表：`truncate calendar_main; truncate calendar_relation; truncate kp_learn;`。

---

## P6. User Setting Service

### 目标
管理用户的 `time_zone` / `locale` / `default_remind_minute` / `notify_channels`，供其他服务 Feign 调用。

### AI 执行步骤
1. 领域：`UserSetting`（1:1 与 `sys_user`）。
2. 接口：
   - `GET /api/v1/user-settings/{userId}`
   - `PUT /api/v1/user-settings/{userId}`
3. 暴露 Feign 客户端到 `common-api`：`UserSettingFeignClient`，带 Sentinel fallback（返回系统默认值）。
4. 内置缓存：Caffeine（10 min）+ Redis（24h），Key `user:setting:{tenantId}:{userId}`。

### 验证步骤
- 单测：更新偏好后 fallback 不生效，缓存命中。
- 端到端：
  ```bash
  curl -fsS http://127.0.0.1:8082/api/v1/user-settings/456 | jq -e '.data.timeZone'
  ```

---

## P7. Calendar Reminder Service + XXL-Job

### 目标
实现设计文档 §5.4 精准提醒流程：每分钟扫描，分片、幂等、重试、多渠道。

### AI 执行步骤
1. 引入 `xxl-job-core`，配置 `xxl.job.admin.addresses=http://127.0.0.1:8089/xxl-job-admin`。
2. `ReminderScanJobHandler`：
   - `@XxlJob("reminderScanJob")`
   - 分片参数：`XxlJobHelper.getShardIndex()` / `getShardTotal()`，SQL where `mod(crc32(tenant_id), #{total}) = #{index}`。
   - Redisson 锁 `reminder:lock:{reminderId}`（TTL 10s）。
   - 幂等：`update ... where is_triggered=0`（affected rows = 1 才继续）。
3. 调用 `NotificationFeignClient.push(...)`。

### 验证步骤
```bash
# 在 XXL-Job Admin UI 中新增执行器 + 任务，或通过 API：
curl -XPOST http://127.0.0.1:8089/xxl-job-admin/jobinfo/add -d '...'
# 手动触发一次
curl -XPOST 'http://127.0.0.1:8089/xxl-job-admin/jobinfo/trigger?id=1'
# 断言：提醒记录 is_triggered 变为 1
mysql ... -e "select count(*) from calendar_reminder where is_triggered=1" | grep -qv '^0$'
```

---

## P8. Notification Service（多渠道）

### 目标
统一封装 SMS / APP / EMAIL / INBOX 四通道，支持 Mock 模式便于本地验证。

### AI 执行步骤
1. 策略模式：`ChannelPusher` 接口 + 4 个实现，`@ConditionalOnProperty` 控制；所有实现发送失败时抛 `NotifyException`，由外层重试 3 次（Spring Retry）。
2. Mock 模式：写入本地文件 `logs/mock-notify.log`，便于 P16 E2E 断言。
3. 防重推：`calendar_notify_record` 唯一键兜底 + Redis `SETNX ntf:{cid}:{ch}`。

### 验证步骤
```bash
curl -XPOST http://127.0.0.1:8084/api/v1/notify/push \
  -H 'Content-Type: application/json' \
  -d '{"calendarId":1,"channels":["sms","app"],"content":"test"}' | jq -e '.code=="0"'
grep -c "calendarId=1" logs/mock-notify.log | grep -qv '^0$'
```

---

## P9. Spring Cloud Gateway

### 目标
统一入口，承担：路由、JWT 校验、`X-Timezone` 透传、Sentinel 限流、CORS。

### AI 执行步骤
1. 路由：
   - `/api/v1/calendar/**` → `calendar-core-service`
   - `/api/v1/user-settings/**` → `user-setting-service`
   - `/api/v1/notify/**` → `notification-service`
2. 全局 Filter `JwtAuthGlobalFilter`：解析 JWT，将 `tenantId/userId/timeZone/locale` 注入下游 header。
3. Sentinel：`spring-cloud-starter-alibaba-sentinel-gateway`，规则通过 Nacos 下发。

### 验证步骤
```bash
curl -fsS -H "Authorization: Bearer <jwt>" http://127.0.0.1:8080/api/v1/calendar/events?startDate=2026-04-20&endDate=2026-04-20
# 未带 token 应返回 401
curl -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/api/v1/calendar/events | grep -q 401
```

---

## P10. 国际化与时区全链路

### 目标
全链路 UTC 存储 + 动态时区展示 + i18n 文案。

### AI 执行步骤
1. `i18n/messages_{zh_CN,en_US,ja_JP}.properties` 至少覆盖：`calendar.title`、`calendar.status.{0..4}`、错误码 `CAL-1xxx~CAL-5xxx`。
2. `MessageSource` 配置 `basename=i18n/messages, encoding=UTF-8, cache-duration=3600s`。
3. `LocaleResolver`：基于 `Accept-Language` 头 + 用户偏好兜底。
4. Jackson `ObjectMapper` 配置：序列化 `ZonedDateTime` 为 ISO-8601，反序列化支持多种格式。

### 验证步骤
```java
@Test void format_asiaShanghai_returnsLocal() { /* ... */ }
@Test void format_americaNewYork_returnsLocal() { /* ... */ }
@Test void i18n_switchLocale_returnsTranslated() { /* ... */ }
```
```bash
mvn -pl calendar-core-service -Dtest='I18nAndTimezoneIT' test
```

---

## P11. 分层缓存

### 目标
Caffeine（JVM）+ Redis Cluster 双层 + 主动失效 + 缓存预热 + 防穿透/击穿/雪崩。

### AI 执行步骤
1. `@Cacheable(cacheManager="twoLevelCacheManager")`，Key `tenantId:user:{uid}:date:yyyyMMdd`。
2. 空值缓存（value 占位 `__NULL__`，TTL 60s）防穿透。
3. 热点 Key 互斥锁（Redisson `tryLock(3, 10, SECONDS)`）防击穿。
4. TTL 随机抖动 `24h ± 30min` 防雪崩。
5. `ApplicationReadyEvent` 时为最近 7 天活跃租户预热。

### 验证步骤
- JMH 或简单压测脚本：1000 次查询命中率 ≥ 90%。
- 单元测试：状态变更后缓存被删除（`cache.get(key) == null`）。

---

## P12. ShardingSphere 分库分表

### 目标
`tenant_id` 一致性哈希分 8 库；`calendar_main` 按 `calendar_date` 月份分表。

### AI 执行步骤
1. 引入 `shardingsphere-jdbc-core`。
2. `application-sharding.yml`：
   ```yaml
   rules:
     - !SHARDING
       tables:
         calendar_main:
           actualDataNodes: ds_${0..7}.calendar_main_${202601..202712}
           databaseStrategy: { standard: { shardingColumn: tenant_id, shardingAlgorithmName: db-consistent-hash } }
           tableStrategy:    { standard: { shardingColumn: calendar_date, shardingAlgorithmName: tbl-month } }
       shardingAlgorithms:
         db-consistent-hash:
           type: CLASS_BASED
           props: { strategy: STANDARD, algorithmClassName: com.lf.calendar.sharding.ConsistentHashAlgorithm }
         tbl-month:
           type: INTERVAL
           props: { datetime-pattern: yyyy-MM-dd, datetime-lower: 2026-01-01, datetime-upper: 2027-12-31, sharding-suffix-pattern: yyyyMM, datetime-interval-amount: 1, datetime-interval-unit: MONTHS }
   ```
3. 保证 `calendar_relation`、`calendar_reminder` 的 `binding-tables` 配置，避免笛卡尔积。

### 验证步骤
- 单测：插入 tenantId=`t-001`、date=`2026-04-20` → 实际落到 `ds_X.calendar_main_202604`（通过 `SELECT * FROM` hint 或 SQL Hint 拦截验证）。
- 命令：
  ```bash
  mvn -pl calendar-core-service -Dtest='ShardingRouteTest' test
  ```

---

## P13. 集成测试（Testcontainers）

### 目标
用 `integration-test` 模块跑全链路场景，不依赖本地 docker-compose。

### AI 执行步骤
1. Testcontainers：MySQLContainer、GenericContainer(Redis)、RocketMQ 自研 container 或 Nacos embedded。
2. 场景用例：
   - C1：创建学习项 → 自动生成日历 + 关联 + 提醒记录。
   - C2：跨时区（Asia/Shanghai、America/New_York）查询日视图，时间正确。
   - C3：并发更新状态，只有一次成功。
   - C4：提醒定时任务触发 → Mock Notification 写入 log。
   - C5：软删后查询不可见；带 `includeDeleted=true` 参数可见。

### 验证步骤
```bash
mvn -pl integration-test verify
# surefire/failsafe 报告：0 failure, 0 error
```

---

## P14. Kubernetes 部署清单

### 目标
每个微服务：`Deployment` + `Service`（ClusterIP）+ `HPA` + `ConfigMap` + `Secret`；对外：`Ingress`。

### AI 执行步骤
1. 生成 `k8s/` 目录，按服务拆分：`calendar-core.yaml` 等。
2. 资源配额：`requests: cpu=1, memory=2Gi`；`limits: cpu=2, memory=4Gi`。
3. 健康检查：`livenessProbe` / `readinessProbe` 均指向 `/actuator/health`，`initialDelaySeconds=30/5`。
4. HPA：`targetCPUUtilizationPercentage=70`，`minReplicas=2, maxReplicas=10`。
5. `Ingress`：`nginx.ingress.kubernetes.io/ssl-redirect: "true"`。

### 验证步骤
```bash
kubectl apply --dry-run=client -f k8s/              # 全部 created (dry run)
kubeval k8s/                                       # schema 校验
conftest test k8s/ --policy policies/              # OPA 策略（可选）
```

### 回滚
修正 YAML，`dry-run` 通过即视为 PASS。无需真正 apply。

---

## P15. 可观测性

### 目标
`/actuator/prometheus` 暴露业务指标；Grafana 仪表盘有 QPS/RT/错误率/缓存命中率。

### AI 执行步骤
1. 引入 `spring-boot-starter-actuator` + `micrometer-registry-prometheus`。
2. 自定义指标：`calendar.event.create.count`、`calendar.cache.hit`、`reminder.push.fail`。
3. `prometheus.yml` 拉取目标：`calendar-core-service:8081/actuator/prometheus` 等。
4. Grafana 导入 JSON 模板（随方案交付 `infra/grafana/calendar-dashboard.json`）。

### 验证步骤
```bash
curl -fsS http://127.0.0.1:8081/actuator/prometheus | grep -q 'calendar_event_create_count'
curl -fsS http://127.0.0.1:9090/api/v1/targets | jq -e '.data.activeTargets | map(.health) | all(.=="up")'
```

---

## P16. 端到端冒烟（MUST PASS）

### 目标
一条命令跑完：创建事件 → 查询日视图 → 触发提醒 → 状态变更 → 缓存失效 → 多租户隔离。

### AI 执行步骤（脚本 `scripts/smoke.sh`）
```bash
#!/usr/bin/env bash
set -euo pipefail
BASE=http://127.0.0.1:8080
T1_JWT=$(./scripts/gen-jwt.sh tenant-A user-1 Asia/Shanghai zh_CN)
T2_JWT=$(./scripts/gen-jwt.sh tenant-B user-2 America/New_York en_US)

# 1. 租户A 创建学习事件
CID=$(curl -fsS -XPOST $BASE/api/v1/calendar/events \
  -H "Authorization: Bearer $T1_JWT" -H "Request-Id: smk-1" \
  -H "Content-Type: application/json" \
  -d '{"title":"Hibernate 复习","relationType":1,"calendarDate":"2026-04-20","calendarTime":"14:30:00"}' \
  | jq -r '.data.id')

# 2. 日视图查询（上海时区 14:30）
curl -fsS "$BASE/api/v1/calendar/events?startDate=2026-04-20&endDate=2026-04-20" \
  -H "Authorization: Bearer $T1_JWT" | jq -e '.data[0].calendarTime=="14:30:00"'

# 3. 租户B 不应看到 CID
curl -fsS "$BASE/api/v1/calendar/events?startDate=2026-04-20&endDate=2026-04-20" \
  -H "Authorization: Bearer $T2_JWT" | jq -e ".data | map(.id==$CID) | any | not"

# 4. 标记完成
curl -fsS -XPATCH $BASE/api/v1/calendar/events/$CID/status \
  -H "Authorization: Bearer $T1_JWT" -H "Request-Id: smk-2" \
  -H "Content-Type: application/json" -d '{"newStatus":1}' | jq -e '.code=="0"'

# 5. 手动触发提醒扫描
curl -XPOST 'http://127.0.0.1:8089/xxl-job-admin/jobinfo/trigger?id=1' >/dev/null
sleep 3
grep -q "calendarId=$CID" logs/mock-notify.log

echo "SMOKE PASS"
```

### 验证步骤
```bash
bash scripts/smoke.sh && echo "P16 OK"
```
**退出码非 0 = 全部计划失败，必须从失败阶段回退重试。**

---

## 附录 A：目录最终结构

```
calendar-platform/
├── AI落地实施计划_通用日历系统.md   ← 本文件
├── pom.xml
├── common-core/
├── common-api/
├── calendar-core-service/
├── user-setting-service/
├── calendar-reminder-service/
├── notification-service/
├── gateway-service/
├── integration-test/
├── infra/
│   ├── docker-compose.yml
│   ├── up.sh
│   └── grafana/calendar-dashboard.json
├── k8s/
│   ├── calendar-core.yaml
│   ├── user-setting.yaml
│   ├── calendar-reminder.yaml
│   ├── notification.yaml
│   ├── gateway.yaml
│   └── ingress.yaml
├── scripts/
│   ├── env-check.sh
│   ├── smoke.sh
│   └── gen-jwt.sh
└── logs/                 ← .gitignore
```

## 附录 B：失败处理通用原则

1. **阶段级隔离**：每阶段成功后必须 `git commit`，便于 `git reset --hard <tag>` 回退。打标 `v0.P0`、`v0.P1` … `v0.P16`。
2. **错误预算**：单阶段重试不超过 3 次，累计失败 2 个阶段即暂停并输出诊断报告给用户。
3. **AI 严格遵循**：任何偏离计划的"创造性"改动需先在 PR 描述里写清理由，否则拒绝合入。
4. **禁止跳过验证步骤**：AI 不能以"预期会通过"代替实际执行验证命令；每一步验证命令必须真实执行并将 stdout/stderr 附在阶段交付物中。

## 附录 C：AI 执行契约（必须遵守）

- 所有命令均以非交互方式执行（`-y`、`--batch`、`--no-tty` 等）。
- 所有阻塞等待使用 `for/sleep` + 探测，最多 5 分钟；超时即判失败。
- 不得调用 `sudo`；需要提权的操作一律记录为"人工介入"并停止。
- 不得读取或上传用户真实敏感数据；测试数据使用 `tenant-A/B` 占位。
- 每阶段完成后写入 `docs/progress/P{n}.md`：包含命令、stdout 摘要、验证结论、耗时。

---

**本方案一共 17 个阶段（P0–P16），每阶段均有可执行命令 + 可验证断言 + 失败回滚。AI 按序执行并在每阶段通过后方可进入下一阶段，最终通过 P16 冒烟测试视为 MVP 落地完成。**
