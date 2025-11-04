# Firebase示例数据导入指南

## 概述

本指南说明如何将示例数据导入到Firebase Firestore数据库。

## 文件位置

- **示例数据文件**: `information/firebase_sample_data.json`
- **导入工具类**: `app/src/main/java/com/group14/foodordering/util/FirebaseDataImporter.java`
- **详细日志**: `information/firebase_sample_data_import_log.txt`

## 快速开始

### 1. 准备环境

确保以下条件满足：
- Firebase项目已配置
- `google-services.json` 文件已添加到项目中
- 应用已连接到Firebase

### 2. 导入数据

1. 启动应用
2. 登录为管理员（使用MainActivity中的Admin Login）
3. 进入"Test Data"页面
4. 点击"导入完整示例数据"按钮
5. 等待导入完成（查看进度和结果）

### 3. 验证数据

在Firebase控制台检查以下集合：
- `users` - 应该包含5个用户
- `admins` - 应该包含3个管理员
- `branches` - 应该包含2个分店
- `tables` - 应该包含5个桌台
- `menuCategories` - 应该包含4个分类
- `menuItems` - 应该包含5个菜品
- `appConfig` - 应该包含1个配置文档

## 数据结构

### 用户 (users)
- 5个用户：2个客户，1个服务员，1个厨房员工，1个经理

### 管理员 (admins)
- ADMIN001: 经理（完整权限）
- ADMIN002: 服务员（订单和桌台管理）
- ADMIN003: 厨房员工（订单查看和更新）

### 分店 (branches)
- branch_001: West Street Branch
- branch_002: East Avenue Branch

### 桌台 (tables)
- A1, A2, A3 (容量4-6人)
- B1 (占用), B2 (需要清洁)

### 菜单分类 (menuCategories)
- Appetizer (开胃菜)
- Main Course (主菜)
- Dessert (甜品)
- Beverage (饮品)

### 菜单项 (menuItems)
- Peking Duck ($89)
- Claypot Rice ($49)
- Spring Rolls ($12)
- Mango Pudding ($15)
- Green Tea ($8)

### 应用配置 (appConfig)
- 税率: 8%
- 服务费: 10%
- 支持的支付方式: cash, credit_card, debit_card, mobile_wallet
- 货币: USD ($)

## 注意事项

1. **数据覆盖**: 导入会覆盖具有相同ID的现有文档
2. **网络要求**: 需要稳定的网络连接到Firebase
3. **权限**: 确保Firebase安全规则允许写入操作
4. **异步操作**: 导入是异步的，某些操作可能在后台完成

## 故障排除

### 导入失败
- 检查网络连接
- 验证Firebase配置
- 查看Logcat日志获取详细错误信息

### 数据不完整
- 在Firebase控制台手动检查
- 重新运行导入
- 检查JSON文件格式是否正确

## 技术支持

如有问题，请查看：
- 详细实现日志: `firebase_sample_data_import_log.txt`
- 代码注释
- Firebase官方文档

