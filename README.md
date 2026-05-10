# 🛍️ 大米商城 (Dami WebShopping) - 课程实验项目

> 《网络应用架构设计与开发》课程实验作业

## 👤 作者信息
* **姓名**：石德麒
* **学号**：202330451571
* **班级**：23网络工程
* **提交日期**：2025-12-31

## 🌐 在线演示 (Demo)
* **部署地址**：http://8.163.12.69

## 🛠️ 技术栈 (Tech Stack)
* **前端**：Vue 3, TypeScript, Vite, Element Plus, Axios
* **后端**：Spring Boot, MyBatis-Plus, MySQL 8.0, JWT
* **运维**：Aliyun ECS, Nginx, Linux (CentOS/Alibaba Cloud Linux)

## 📂 项目介绍
本项目是一个完整的前后端分离电子商务网站，包含以下核心功能：
1. **用户端**：浏览商品、加入购物车、下单、个人订单管理。
2. **管理端**：后台商品增删改查、订单状态管理、销售情况统计。
3. **部署**：已完成云服务器环境搭建与Nginx反向代理配置。

## 🚀 本地运行 
1. 克隆项目：`git clone https://github.com/yueluokongmen/dami-WebShoppingMall.git`
2. 导入 SQL 文件至 MySQL 数据库。
3. 后端：IDEA 打开 `mall-backend`，运行 Application。
4. 前端：VSCode 打开 `mall-frontend`，输入`npm install`安装依赖后再运行 `npm run dev`。

## ☁ 云端测试方法
1. 新用户直接点击登录即可。
2. 管理员在地址后输入/admin/login，再输入账号
3. 网站比较卡顿，点击以后请耐心等待几秒钟。
