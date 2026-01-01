# UniBuy API 测试用例文档

本文档包含 UniBuy 后端系统的完整 API 接口说明，用于手动进行接口测试 (如使用 Postman, Apifox 或 HTTP Client)。

**基础信息**
- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json`

---

## 1. 用户认证模块 (Auth)

### 1.1 学生注册
- **接口**: `/auth/register/student`
- **方法**: `POST`
- **描述**: 注册一个新的学生账号。
- **请求参数**:
```json
{
  "username": "student001",
  "password": "password123",
  "phone": "13800000001",
  "schoolId": "2024001",
  "nickname": "测试学生"
}
```
- **预期响应**:
```json
{
  "code": 200,
  "msg": "Register success",
  "data": null
}
```

### 1.2 商家注册
- **接口**: `/auth/register/merchant`
- **方法**: `POST`
- **描述**: 注册一个新的商家账号 (注册后默认为待审核状态)。
- **请求参数**:
```json
{
  "username": "merchant001",
  "password": "password123",
  "phone": "13900000001",
  "shopName": "山下小卖部",
  "pickupLocation": "一食堂门口"
}
```
- **预期响应**:
```json
{
  "code": 200,
  "msg": "Register success, please wait for audit",
  "data": null
}
```

### 1.3 用户登录
- **接口**: `/auth/login`
- **方法**: `POST`
- **描述**: 支持学生、商家、管理员登录。商家需审核通过后才能登录。
- **请求参数 (示例)**:
```json
{
  "username": "student001",
  "password": "password123",
  "role": "student" 
}
```
> role 可选值: `student`, `merchant`, `admin`

- **预期响应**:
```json
{
  "code": 200,
  "msg": "Success",
  "data": {
    "role": "student",
    "user": { "studentId": 1, "username": "student001", ... }
  }
}
```

---

## 2. 管理员模块 (Admin)

### 2.1 获取待审核商家列表
- **接口**: `/admin/merchant/pending`
- **方法**: `GET`
- **请求参数**: 无
- **预期响应**:
```json
{
  "code": 200,
  "data": {
    "records": [
      { "merchantId": 1, "username": "merchant001", "auditStatus": 0, ... }
    ],
    ...
  }
}
```

### 2.2 审核商家
- **接口**: `/admin/audit/merchant`
- **方法**: `POST`
- **描述**: 通过或驳回商家注册申请。
- **请求参数 (Query Params)**:
  - `merchantId`: 1 (商家ID)
  - `status`: 1 (1:通过, 2:驳回)
- **示例 URL**: `/admin/audit/merchant?merchantId=1&status=1`
- **预期响应**:
```json
{ "code": 200, "msg": "Merchant audit completed" }
```

---

## 3. 需求驱动模块 (Request) - **核心业务**

### 3.1 发布代购需求 (学生)
- **接口**: `/request/publish`
- **方法**: `POST`
- **请求参数**:
```json
{
  "studentId": 1,
  "productName": "PS5游戏机",
  "category": "数码产品",
  "description": "急需一台PS5，最好是国行",
  "expectedPrice": 3500.00,
  "urgencyLevel": 3,
  "minJoinUsers": 1
}
```
> urgencyLevel: 1(低), 2(中), 3(高/24h)

### 3.2 查看需求广场
- **接口**: `/request/square`
- **方法**: `GET`
- **预期响应**: 返回状态为 Pending(0) 或 Responding(1) 的需求列表。

### 3.3 响应需求报价 (商家)
- **接口**: `/request/respond`
- **方法**: `POST`
- **请求参数**:
```json
{
  "requestId": 1,
  "merchantId": 1,
  "quotedPrice": 3600.00,
  "responseNote": "我有现货，可以马上送"
}
```

### 3.4 确认选标 (学生) -> **自动转商品**
- **接口**: `/request/confirm`
- **方法**: `POST`
- **描述**: 学生选中某个商家的报价，系统自动生成对应的商品（状态为待审核）。
- **请求参数 (Query Params)**:
  - `requestId`: 1
  - `responseId`: 1
- **示例 URL**: `/request/confirm?requestId=1&responseId=1`

---

## 4. 商品模块 (Product)

### 4.1 获取商家商品列表
- **接口**: `/product/merchant/list`
- **方法**: `GET`
- **请求参数**: `merchantId=1`
- **描述**: 商家可查看自己发布的商品（包括由需求自动转化生成的商品）。

### 4.2 审核商品 (管理员)
- **接口**: `/product/audit`
- **方法**: `POST`
- **描述**: 需求转化的商品默认是未审核状态，需管理员审核通过后学生才能购买。
- **请求参数 (Query Params)**:
  - `productId`: 1
  - `status`: 1 (1:通过/上架, 2:驳回)

### 4.3 商品列表 (学生端)
- **接口**: `/product/list`
- **方法**: `GET`
- **请求参数**: `keyword=PS5` (可选)

---

## 5. 订单模块 (Order)

### 5.1 创建订单
- **接口**: `/order/create`
- **方法**: `POST`
- **请求参数**:
```json
{
  "order": {
    "studentId": 1,
    "merchantId": 1,
    "totalPrice": 3600.00
  },
  "items": [
    {
      "productId": 1,
      "quantity": 1,
      "price": 3600.00
    }
  ]
}
```

### 5.2 支付订单 (模拟)
- **接口**: `/order/pay`
- **方法**: `POST`
- **请求参数**: `orderId=1`

### 5.3 商家接单
- **接口**: `/order/accept`
- **方法**: `POST`
- **请求参数**: `orderId=1`

### 5.4 上传代购凭证 (商家)
- **接口**: `/order/voucher`
- **方法**: `POST`
- **请求参数**:
  - `orderId`: 1
  - `url`: `http://example.com/ticket.jpg`

### 5.5 完成订单
- **接口**: `/order/finish`
- **方法**: `POST`
- **请求参数**: `orderId=1`

---

## 6. 辅助模块

### 6.1 发送站内信
- **接口**: `/aux/chat/send`
- **方法**: `POST`
- **请求参数**:
```json
{
  "orderId": 1,
  "senderId": 1,
  "senderRole": 1,
  "receiverId": 1,
  "content": "请问什么时候能送到？",
  "type": 0
}
```
> senderRole: 1(学生), 2(商家)

### 6.2 提交评价
- **接口**: `/aux/review/submit`
- **方法**: `POST`
- **请求参数**:
```json
{
  "orderId": 1,
  "studentId": 1,
  "merchantId": 1,
  "scoreAttitude": 5,
  "scoreQuality": 5,
  "scoreSpeed": 4,
  "comment": "代购速度很快！"
}
```
