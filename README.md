# 宠物用品销售网站（控制台版）

三级项目选题：**宠物用品销售网站**。基于 Java 17 + Maven 的分层架构控制台程序，商品数据用内存 List 模拟数据库。

## 分层结构

```
src/main/java/com/neusoft/petshop/
├── model/         PetProduct          商品实体（id / 名称 / 分类 / 价格 / 库存）
├── dao/           InitData + PetProductDao     数据访问（List 模拟数据库）
├── service/       IPetProductService + impl/   业务规则（id 去重、价格区间交换）
├── controller/    PetProductController         转调 Service
└── view/          PetShopView                 菜单界面（Scanner）
```

调用链：`View → Controller → Service → DAO → Model`

## 功能菜单

| 选项 | 功能 |
| --- | --- |
| 1 | 查看全部商品 |
| 2 | 按 id 查看商品详情 |
| 3 | 按名称模糊搜索 |
| 4 | 按分类筛选（食品 / 玩具 / 用品） |
| 5 | 按价格区间查询（输反自动交换） |
| 6 | 新增商品（id 重复拦截） |
| 7 | 修改商品 |
| 8 | 删除商品 |
| 0 | 退出 |

## 运行

```bash
mvn compile
# 或 IDEA 中右键 view/PetShopView.java → Run
java -cp target/classes com.neusoft.petshop.view.PetShopView
```

## 业务规则

- 新增商品时 id 不可重复，重复返回 0；
- 价格区间查询若最低 > 最高，Service 自动交换两个参数；
- 输入非法数字有防崩溃校验。
