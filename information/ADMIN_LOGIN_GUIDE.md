# 管理员登录指南

## 问题描述

无法使用示例数据中的管理员账户登录。

## 解决方案

### 1. 确保数据已导入

首先，确保示例数据已经正确导入到Firebase：

1. 启动应用
2. 登录为管理员（如果已有管理员账户）
3. 进入"Test Data"页面
4. 点击"导入完整示例数据"按钮
5. 等待导入完成
6. 点击"验证管理员数据"按钮，检查数据是否正确导入

### 2. 登录凭据

示例数据包含以下3个管理员账户：

#### ADMIN001 - 经理（Manager）
- **Admin ID**: `ADMIN001`
- **电话号码**: `+1234567894`
- **姓名**: Charlie Wang
- **邮箱**: manager01@restaurant.com
- **权限**: menu_edit, report_view, inventory_manage, order_manage, user_manage

#### ADMIN002 - 服务员（Server）
- **Admin ID**: `ADMIN002`
- **电话号码**: `+1234567892`
- **姓名**: Alice Johnson
- **邮箱**: server01@restaurant.com
- **权限**: order_manage, table_manage

#### ADMIN003 - 厨房员工（Kitchen）
- **Admin ID**: `ADMIN003`
- **电话号码**: `+1234567893`
- **姓名**: Bob Chen
- **邮箱**: chef01@restaurant.com
- **权限**: order_view, order_update

### 3. 登录步骤

1. 在MainActivity中，点击"Admin Login"按钮
2. 在弹出对话框中输入：
   - **Staff ID or Phone**: 输入 `ADMIN001`、`ADMIN002` 或 `ADMIN003`
   - 或者输入对应的电话号码（如 `+1234567894`）
3. **Password**: 目前密码字段是可选的（尚未实现密码验证）
4. 点击"Login"按钮

### 4. 登录方式

系统支持两种登录方式：

1. **通过Admin ID登录**：
   - 输入: `ADMIN001`
   - 输入: `ADMIN002`
   - 输入: `ADMIN003`

2. **通过电话号码登录**：
   - 输入: `+1234567894` (Manager)
   - 输入: `+1234567892` (Server)
   - 输入: `+1234567893` (Kitchen)

### 5. 故障排除

#### 问题1: "Admin not found" 错误

**可能原因**:
- 数据未正确导入
- Admin ID或电话号码输入错误
- Firebase安全规则阻止了读取操作

**解决方案**:
1. 检查数据是否已导入：
   - 在TestDataActivity中点击"验证管理员数据"
   - 查看Firebase控制台的`admins`集合
2. 确认输入正确：
   - Admin ID必须完全匹配（大小写敏感）
   - 电话号码必须包含`+`号
3. 检查Firebase安全规则：
   ```javascript
   match /admins/{adminId} {
     allow read: if true; // 开发阶段允许读取
   }
   ```

#### 问题2: "Admin account is inactive" 错误

**可能原因**:
- 管理员账户的`isActive`字段为`false`

**解决方案**:
1. 在Firebase控制台检查`admins`集合
2. 确保`isActive`字段为`true`
3. 或者重新导入示例数据

#### 问题3: 数据导入失败

**可能原因**:
- JSON文件路径错误
- Firebase连接问题
- 权限问题

**解决方案**:
1. 确认`information/firebase_sample_data.json`文件存在
2. 检查网络连接
3. 查看Logcat日志获取详细错误信息
4. 检查Firebase项目配置

### 6. 验证数据

使用TestDataActivity中的"验证管理员数据"功能：

1. 进入TestDataActivity
2. 点击"验证管理员数据"按钮
3. 系统会：
   - 尝试通过Admin ID查找每个管理员
   - 尝试通过电话号码查找每个管理员
   - 显示详细的验证结果
   - 提供登录说明

### 7. 技术细节

#### 登录流程

1. 用户输入Staff ID或电话号码
2. 系统首先尝试通过`adminId`（文档ID）查找
3. 如果未找到，则通过`phone`字段查询
4. 检查管理员是否存在且`isActive`为`true`
5. 如果通过验证，设置`isAdminLoggedIn = true`

#### 数据模型

Admin模型包含以下字段：
- `adminId`: 管理员ID（文档ID）
- `userId`: 关联的用户ID
- `email`: 邮箱地址
- `name`: 姓名
- `phone`: 电话号码
- `permissions`: 权限数组（转换为List存储到Firestore）
- `isActive`: 是否激活
- `createdAt`: 创建时间
- `updatedAt`: 更新时间

### 8. 常见问题

**Q: 为什么密码字段是可选的？**
A: 当前版本尚未实现密码验证功能。密码字段保留用于未来实现。

**Q: 可以使用邮箱登录吗？**
A: 目前不支持。只支持Admin ID或电话号码登录。如需支持邮箱登录，需要修改`getAdminByStaffIdOrPhone`方法。

**Q: 如何添加新的管理员？**
A: 可以通过TestDataActivity的"测试创建管理员"按钮，或在代码中调用`createOrUpdateAdmin`方法。

**Q: 如何修改管理员权限？**
A: 在Firebase控制台直接编辑`admins`集合中的文档，或通过代码更新。

## 总结

如果遇到登录问题：

1. ✅ 确认数据已导入（使用"验证管理员数据"功能）
2. ✅ 使用正确的Admin ID或电话号码
3. ✅ 检查Firebase安全规则
4. ✅ 查看Logcat日志获取详细错误信息
5. ✅ 在Firebase控制台验证数据存在

如果问题仍然存在，请检查：
- Firebase项目配置
- 网络连接
- Firestore数据库状态

