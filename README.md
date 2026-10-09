# 宠物用品销售网站

三级项目选题：**宠物用品销售网站**。
Java 17 + Spring Boot 3.2.2 + Maven，分层架构（Model / DAO / Service / Controller / View），
商品与订单数据用内存 List 模拟数据库。

> **本次完善**：第一版是一个**纯控制台程序**，外加一张 `index.html` 静态演示页 ——
> 但那张页面里的商品数据是硬编码在 JS 里的，搜索、筛选、购物车全在前端自己算，
> **一行代码都没调用 Java 后端**，等于「一个控制台程序 + 一张假网页」。
> 现在后端补上了 REST 接口层，页面通过 `fetch` 调用真实接口，前后端真正打通。

---

## 一、两种入口，同一套业务逻辑

| 入口 | 使用方式 | 表现层 | 说明 |
| --- | --- | --- | --- |
| **Web 版**（新增） | 浏览器访问 <http://localhost:8080/> | `api/` 包（REST + JSON） | 商城首页、搜索筛选、购物车、下单、商品管理 |
| **控制台版**（保留） | `mvn exec:java` | `view/` + `controller/` 包 | 原来的 8 项菜单功能，一行没少 |

两条路都往下走到**同一套 Service 和 DAO**，所以业务规则只有一份实现：

```
       浏览器 (index.html)                        控制台 (键盘输入)
              │ fetch /api/...                          │
              ▼                                          ▼
   api/PetProductApi、OrderApi              controller/PetProductController
              │                                          │
              └──────────────┬───────────────────────────┘
                             ▼
                  service/  IPetProductService、IOrderService（接口）
                            impl/  PetProductServiceImpl、OrderServiceImpl（业务规则）
                             ▼
                  dao/  PetProductDao、OrderDao（内存 List 模拟数据库）
                             ▼
                  model/  PetProduct、Order、OrderItem
```

> 分层的好处在这里体现得很直接：加一个 Web 入口，**没有重写任何业务规则**，
> 只是多了一个「面向浏览器的表现层」。

---

## 二、项目结构

```
s-c24001010417/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/neusoft/petshop/
    │   │   ├── PetShopApplication.java          Spring Boot 启动类（Web 版入口）
    │   │   ├── common/
    │   │   │   ├── Result.java                  统一响应体：code / message / data
    │   │   │   └── BusinessException.java       业务异常（带可直接展示的提示语）
    │   │   ├── model/
    │   │   │   ├── PetProduct.java              商品实体
    │   │   │   ├── Order.java                   订单实体
    │   │   │   └── OrderItem.java               订单明细（下单时的价格快照）
    │   │   ├── dto/
    │   │   │   └── CheckoutRequest.java         下单请求体（只收 id 和数量）
    │   │   ├── dao/
    │   │   │   ├── InitData.java                初始商品数据
    │   │   │   ├── PetProductDao.java           商品数据访问
    │   │   │   └── OrderDao.java                订单数据访问
    │   │   ├── service/
    │   │   │   ├── IPetProductService.java      商品业务接口
    │   │   │   ├── IOrderService.java           订单业务接口
    │   │   │   └── impl/                        两个业务实现（规则都在这里）
    │   │   ├── api/                             【新增】Web 表现层
    │   │   │   ├── PetProductApi.java           商品 REST 接口
    │   │   │   ├── OrderApi.java                订单 REST 接口
    │   │   │   └── GlobalExceptionHandler.java  全局异常 → 统一 Result
    │   │   ├── config/
    │   │   │   └── WebConfig.java               跨域配置
    │   │   ├── controller/
    │   │   │   └── PetProductController.java    控制台控制器（保留）
    │   │   └── view/
    │   │       └── PetShopView.java             控制台菜单（保留）
    │   └── resources/
    │       ├── application.yml                  端口 8080、日志
    │       └── static/index.html                商城首页（原仓库根目录的 index.html 移到这里）
    └── test/java/com/neusoft/petshop/service/impl/
        ├── PetProductServiceImplTest.java       商品业务规则（17 个用例）
        └── OrderServiceImplTest.java            下单与库存（8 个用例）
```

---

## 三、快速开始

### 1. 启动 Web 版

```bash
# 方式一：直接运行（开发时用）
mvn spring-boot:run

# 方式二：打包成可执行 jar 再运行
mvn clean package
java -jar target/pet-shop-1.0-SNAPSHOT.jar
```

也可以直接在 IDEA 里运行 `PetShopApplication` 的 `main` 方法。

然后浏览器打开 **<http://localhost:8080/>**

> 端口被占用时：`mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8099`

### 2. 启动控制台版（可选）

```bash
mvn compile exec:java
```

或 IDEA 中右键 `view/PetShopView.java` → Run。

> 加了 Spring 依赖之后，`java -cp target/classes` 这种跑法会缺 jar 包，
> 所以改用 `exec:java`（pom 里已经配好默认主类是 `PetShopView`）。

### 3. 跑单元测试

```bash
mvn test
```

---

## 四、接口清单

统一前缀 `/api`，统一响应结构：

```json
{ "code": 200, "message": "操作成功", "data": ... }
```

`code` 约定：`200` 成功 · `400` 参数或业务规则不满足 · `404` 数据不存在 · `500` 服务器内部错误。

### 商品

| 方法 | 地址 | 作用 |
| --- | --- | --- |
| GET | `/api/products` | 商品列表，支持组合筛选 |
| GET | `/api/products/categories` | 全部分类（前端筛选按钮由它生成） |
| GET | `/api/products/{id}` | 商品详情 |
| POST | `/api/products` | 新增商品（id 由后端分配） |
| PUT | `/api/products/{id}` | 修改商品 |
| DELETE | `/api/products/{id}` | 删除商品 |

列表的查询参数**都是可选的**，`name`/`category`/`minPrice`/`maxPrice` 可任意组合：

```
GET /api/products                                          全部商品
GET /api/products?category=食品                             按分类
GET /api/products?keyword=猫                                按名称模糊
GET /api/products?category=食品&maxPrice=100                分类 + 价格上限
GET /api/products?minPrice=100&maxPrice=20                  输反了会自动交换成 20~100
```

### 订单

| 方法 | 地址 | 作用 |
| --- | --- | --- |
| POST | `/api/orders` | 下单结算（校验库存 → 扣库存 → 生成订单） |
| GET | `/api/orders` | 订单列表（最新在前） |

```json
POST /api/orders
{ "items": [ { "productId": 1, "quantity": 2 }, { "productId": 3, "quantity": 1 } ] }
```

### 用 curl / Postman 试几个

```bash
curl http://localhost:8080/api/products
curl "http://localhost:8080/api/products?category=%E9%A3%9F%E5%93%81&maxPrice=100"
curl http://localhost:8080/api/products/999          # 看 404 的响应长什么样

curl -X POST http://localhost:8080/api/products \
     -H "Content-Type: application/json" \
     -d '{"name":"猫薄荷","category":"玩具","price":29.9,"stock":60}'

curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"items":[{"productId":1,"quantity":2}]}'
```

---

## 五、业务规则（都在 Service 层）

| 规则 | 位置 |
| --- | --- |
| 商品名称/分类不能为空，名称 ≤ 50 字 | `PetProductServiceImpl#validate` |
| 商品价格必须 > 0 且 ≤ 999999，库存不能为负 | 同上 |
| 名称、分类入库前去掉首尾空格（避免「皇家猫粮 」和「皇家猫粮」变成两个商品） | 同上 |
| 新增商品时 id 不可重复；Web 端不传 id 时自动分配 | `PetProductServiceImpl#createProduct` |
| 价格区间输反时自动交换 | `search` / `listByPriceRange` |
| 下单时购物车不能为空，数量必须 > 0 | `OrderServiceImpl#checkout` |
| 下单时库存不足要拦住，且**一件都不能扣**（两阶段校验） | 同上 |
| 订单明细保存下单那一刻的名称和价格（历史订单不受后续改价影响） | `OrderItem` + `checkout` |
| 同一个商品传成多行时自动合并数量 | `OrderServiceImpl#checkout` |

**两阶段校验** 是订单里最值得看的一段：先把所有明细检查完，全部通过了才去扣库存。
如果边检查边扣，购物车里 3 件商品第 2 件库存不足时，第 1 件的库存就白白少了。

---

## 六、本次完善的内容

| | 第一版 | 现在 |
| --- | --- | --- |
| 形态 | 纯控制台程序 | Spring Boot Web 应用 + 控制台双入口 |
| 页面数据 | JS 里硬编码 8 条商品 | 全部来自 `GET /api/products` |
| 搜索 / 筛选 | 前端自己算 | 后端 SQL 式条件查询（`keyword`/`category`/`minPrice`/`maxPrice`） |
| 分类筛选按钮 | 写死「食品/玩具/用品」 | 由 `GET /api/products/categories` 返回，加分类不用改前端 |
| 下单 | 弹个「下单成功（演示）」，库存不减 | 真调 `POST /api/orders`，校验库存、扣库存、生成订单号 |
| 增删改 | 只有控制台能做 | 页面「商品管理」面板可直接增删改 |
| 错误提示 | `return 0`，不知道哪错了 | `BusinessException` + 全局异常处理，返回中文原因 |
| 参数校验 | 基本没有 | 名称/分类/价格/库存全套校验 |
| 线程安全 | 无（单线程控制台无所谓） | DAO 方法 `synchronized`，防并发超卖 |
| 单元测试 | 无 | 25 个用例（`mvn test`） |
| 跑法 | `java -cp target/classes` | `mvn spring-boot:run` / `java -jar`（控制台用 `mvn exec:java`） |

顺带修掉一个 bug：控制台价格区间输反时会打印 `=== 价格 100 ~ 20 的商品 ===`
（实际查的是 20~100），现在会显示正确区间并提示已自动交换。

---

## 七、常见问题

**页面能打开，但商品列表一直空的 / 提示无法连接后端**
后端没启动，或页面是用 `file://` 直接双击打开的。
正常请用 <http://localhost:8080/> 访问（页面和后端同一个服务，不存在跨域）。
如果你确实想双击打开，`WebConfig` 里已经配了跨域，页面会自动改用 `http://localhost:8080/api`。

**改了数据，重启后又变回 8 件商品**
商品和订单都存在内存 List 里（`dao/` 包），应用一重启就复原。
这是题目「用内存模拟数据库」的要求；下一步可以接 MySQL + MyBatis 做真正持久化。

**`mvn test` 里为什么不用启动 Spring**
`PetProductServiceImpl` 的依赖是从构造器传进来的，测试里直接 `new` 一个真 DAO 塞进去就行，
不需要容器，跑得快也不会互相干扰。这正是构造器注入比字段注入好测试的地方。

**接口返回 200 但 code 是 400，算失败吗**
算失败。本项目用的是国内常见的「统一响应体」约定：HTTP 状态码保持 200，
成败看响应体里的 `code`。前端只需判断 `body.code === 200`。

---

## 八、下一步可以做什么

- 接 MySQL + MyBatis/MyBatis-Plus，把 `dao/` 里的内存 List 换成真实数据库表
- 用户注册登录 + 会话/令牌，让「我的订单」真正属于某个人
- 商品图片上传与展示（现在分类图标是内联 SVG）
- 分页（商品变多之后列表接口需要 `pageNum` / `pageSize`）
- 订单状态流转（待支付 → 已支付 → 已发货 → 已完成）
