# 使用官方的 Node.js 16 作为基础镜像
FROM node:16-alpine as build-stage

# 设置工作目录
WORKDIR /app

# 复制 package.json 和 package-lock.json
COPY package*.json ./

# 安装依赖
RUN npm install --production=false

# 复制所有源代码
COPY . .

# 构建应用
RUN npm run build

# 生产阶段 - 使用 Nginx 来服务静态文件
FROM nginx:alpine as production-stage

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

# 从构建阶段复制构建产物
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]