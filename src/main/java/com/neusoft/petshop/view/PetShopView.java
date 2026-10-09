package com.neusoft.petshop.view;

import com.neusoft.petshop.controller.PetProductController;
import com.neusoft.petshop.model.PetProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * 视图层：只负责打印菜单、读键盘、显示结果
 * 调用顺序：View → Controller → Service → DAO → Model
 * 运行入口：右键本类 → Run 'PetShopView.main()'
 */
public class PetShopView {

    private PetProductController controller = new PetProductController();
    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new PetShopView().start();
    }

    public void start() {
        System.out.println("========== 欢迎来到萌宠用品小店 ==========");
        while (true) {
            printMenu();
            int choice = readInt("请输入选项：");
            switch (choice) {
                case 1:
                    listAll();
                    break;
                case 2:
                    getById();
                    break;
                case 3:
                    searchByName();
                    break;
                case 4:
                    listByCategory();
                    break;
                case 5:
                    listByPriceRange();
                    break;
                case 6:
                    addProduct();
                    break;
                case 7:
                    updateProduct();
                    break;
                case 8:
                    deleteProduct();
                    break;
                case 0:
                    System.out.println("感谢光临，再见！");
                    return;
                default:
                    System.out.println("输入无效，请重新选择。");
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=========================================");
        System.out.println("            萌宠用品小店 菜单");
        System.out.println("            1. 查看全部商品");
        System.out.println("            2. 按 id 查看商品详情");
        System.out.println("            3. 按名称搜索商品");
        System.out.println("            4. 按分类筛选商品");
        System.out.println("            5. 按价格区间查询商品");
        System.out.println("            6. 新增商品");
        System.out.println("            7. 修改商品");
        System.out.println("            8. 删除商品");
        System.out.println("            0. 退出系统");
        System.out.println("=========================================");
    }

    /** 1. 全部商品 */
    private void listAll() {
        System.out.println("【全部商品】");
        printList(controller.listAll());
    }

    /** 2. 按 id 查详情 */
    private void getById() {
        System.out.println("【按 id 查询】");
        int id = readInt("请输入商品 id：");
        PetProduct p = controller.getById(id);
        if (p == null) {
            System.out.println("未找到 id=" + id + " 的商品。");
        } else {
            printProduct(p);
        }
    }

    /** 3. 按名称模糊搜索 */
    private void searchByName() {
        System.out.println("【按名称搜索】");
        String name = readLine("请输入商品名称（支持模糊）：");
        List<PetProduct> list = controller.searchByName(name);
        if (list.isEmpty()) {
            System.out.println("未找到名称包含「" + name + "」的商品。");
        } else {
            printList(list);
        }
    }

    /** 4. 按分类筛选 */
    private void listByCategory() {
        System.out.println("【按分类筛选】");
        System.out.println("可选分类：食品 / 玩具 / 用品");
        String category = readLine("请输入分类：");
        List<PetProduct> list = controller.listByCategory(category);
        if (list.isEmpty()) {
            System.out.println("分类「" + category + "」下没有商品。");
        } else {
            printList(list);
        }
    }

    /** 5. 按价格区间查询 */
    private void listByPriceRange() {
        System.out.println("【按价格区间查询】");
        BigDecimal min = readPrice("请输入最低价格：");
        BigDecimal max = readPrice("请输入最高价格：");

        // 修掉的 bug：Service 里「最低价 > 最高价会自动交换」，
        // 但第一版的标题直接打印用户输入的两个数，于是输反时会显示成
        //   === 价格 100 ~ 20 的商品 ===
        // 而实际查出来的是 20~100 的商品，看着自相矛盾。
        // 这里先排好序再打印，标题就和真实查询区间一致了。
        BigDecimal low = min.min(max);
        BigDecimal high = min.max(max);
        boolean swapped = low.compareTo(min) != 0;

        List<PetProduct> list = controller.listByPriceRange(min, max);
        if (list.isEmpty()) {
            System.out.println("该价格区间没有商品。");
        } else {
            System.out.println("=== 价格 " + low + " ~ " + high + " 的商品（共 " + list.size() + " 件）===");
            if (swapped) {
                System.out.println("（提示：最低价大于最高价，已自动按 " + low + " ~ " + high + " 查询）");
            }
            printList(list);
        }
    }

    /** 6. 新增商品 */
    private void addProduct() {
        System.out.println("【新增商品】");
        int id = readInt("请输入商品 id：");
        String name = readLine("请输入商品名称：");
        String category = readLine("请输入商品分类（食品/玩具/用品）：");
        BigDecimal price = readPrice("请输入商品价格：");
        int stock = readInt("请输入商品库存：");
        PetProduct product = new PetProduct(id, name, category, price, stock);
        int result = controller.addProduct(product);
        if (result == 1) {
            System.out.println("新增成功！");
        } else {
            System.out.println("新增失败：该 id 已存在。");
        }
    }

    /** 7. 修改商品 */
    private void updateProduct() {
        System.out.println("【修改商品】");
        int id = readInt("请输入要修改的商品 id：");
        PetProduct old = controller.getById(id);
        if (old == null) {
            System.out.println("未找到 id=" + id + " 的商品，无法修改。");
            return;
        }
        System.out.println("当前商品：" + old);
        String name = readLine("请输入新的商品名称：");
        String category = readLine("请输入新的商品分类：");
        BigDecimal price = readPrice("请输入新的商品价格：");
        int stock = readInt("请输入新的商品库存：");
        int result = controller.updateProduct(new PetProduct(id, name, category, price, stock));
        if (result == 1) {
            System.out.println("修改成功！");
        } else {
            System.out.println("修改失败。");
        }
    }

    /** 8. 删除商品 */
    private void deleteProduct() {
        System.out.println("【删除商品】");
        int id = readInt("请输入要删除的商品 id：");
        int result = controller.deleteProduct(id);
        if (result == 1) {
            System.out.println("删除成功！");
        } else {
            System.out.println("删除失败：未找到 id=" + id + " 的商品。");
        }
    }

    /** 打印一条商品（对齐输出，方便看） */
    private void printProduct(PetProduct p) {
        System.out.println("id=" + p.getId() + " | " + p.getName()
                + " | 分类=" + p.getCategory()
                + " | 价格=" + p.getPrice() + " 元"
                + " | 库存=" + p.getStock());
    }

    /** 打印商品列表 */
    private void printList(List<PetProduct> list) {
        for (PetProduct p : list) {
            printProduct(p);
        }
        System.out.println("共 " + list.size() + " 件商品。");
    }

    private int readInt(String tip) {
        System.out.print(tip);
        while (!scanner.hasNextInt()) {
            System.out.println("请输入数字！");
            scanner.next();
            System.out.print(tip);
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private String readLine(String tip) {
        System.out.print(tip);
        return scanner.nextLine().trim();
    }

    private BigDecimal readPrice(String tip) {
        System.out.print(tip);
        while (!scanner.hasNextBigDecimal()) {
            System.out.println("请输入正确的价格！");
            scanner.next();
            System.out.print(tip);
        }
        BigDecimal value = scanner.nextBigDecimal();
        scanner.nextLine();
        return value;
    }
}
