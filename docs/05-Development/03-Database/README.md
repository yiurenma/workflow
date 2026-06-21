# Database 开发索引

## 我是谁？
数据库开发者（Database Developer），负责：
1. **Schema 设计** - 新表、字段、索引
2. **迁移脚本** - 版本化的 DDL/DML 脚本
3. **性能优化** - 索引调优、查询优化
4. **数据完整性** - 约束、外键、触发器

## 📊 当前状态

**当前 Schema：** [current-schema.sql](current-schema.sql) - 待生成

**最新变更：**
| 版本 | 标题 | 改动内容 | 状态 | 日期 |
|------|------|----------|------|------|
| v43.0 | （无数据库变更） | - | ⏸️ 跳过 | - |
| v42.0 | （无数据库变更） | - | ⏸️ 跳过 | - |
| v41.0 | （无数据库变更） | - | ⏸️ 跳过 | - |

## 📋 待开发 TODO

**当前无数据库变更任务**

查看完整待办 → [../../01-team/TODO.md](../../01-team/TODO.md)

## 🗂️ 工作日志说明

`work-logs/` 目录将存放每个版本的数据库变更日志。

**目前暂无日志** - 当有数据库变更时才会产生日志

## 📖 数据库工作日志格式

每个数据库日志（`work-logs/database-vX.0.md`）应包含：

1. **Schema 变更** - DDL 脚本
   ```sql
   CREATE TABLE new_table (...);
   ALTER TABLE existing_table ADD COLUMN ...;
   CREATE INDEX idx_name ON table(column);
   ```

2. **迁移脚本** - Flyway 文件名和位置
   - 文件名：`V43__description.sql`
   - 位置：哪个 repo 的 `db/migration/`

3. **数据迁移** - DML 脚本（如有）
   ```sql
   UPDATE table SET new_field = old_field WHERE ...;
   ```

4. **性能影响评估**
   - 是否需要停机迁移？
   - 索引创建时间估算
   - 对线上查询的影响

5. **验证步骤**
   ```sql
   SELECT COUNT(*) FROM new_table;
   ```

## 🗄️ 数据库架构

**共享数据库：** `workflow-operation-api` 和 `workflow-online-api` **共享同一个 Neon PostgreSQL 数据库**。

**迁移脚本位置：**
- `workflow-operation-api/src/main/resources/db/migration/` (Flyway)
- `workflow-online-api/src/main/resources/db/migration/` (Flyway)

**注意：** 两个服务共享数据库，但迁移脚本分开管理。需要在 Arch 文档中明确哪些表归哪个服务管理。

## 💻 技术栈

- **数据库：** Neon PostgreSQL
- **迁移工具：** Flyway (Spring Boot 集成)
- **连接池：** HikariCP

## 🔗 相关文档

- 架构设计 → [../../03-Architect/README.md](../../03-Architect/README.md)
- 后端实现 → [../02-Backend/README.md](../02-Backend/README.md)
