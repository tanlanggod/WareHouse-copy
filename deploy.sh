#!/bin/bash

# 仓库管理系统一键部署脚本
# 适用于 Ubuntu 20.04+ 和 CentOS 7+

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查系统信息
check_system() {
    log_step "检查系统信息..."

    # 检查操作系统
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if [ -f /etc/os-release ]; then
            . /etc/os-release
            OS=$NAME
            VER=$VERSION_ID
            log_info "检测到操作系统: $OS $VER"
        else
            log_error "无法检测操作系统版本"
            exit 1
        fi
    else
        log_error "此脚本仅支持 Linux 系统"
        exit 1
    fi

    # 检查架构
    ARCH=$(uname -m)
    log_info "系统架构: $ARCH"

    # 检查可用磁盘空间
    AVAILABLE_SPACE=$(df -BG . | awk 'NR==2 {print $4}' | sed 's/G//')
    if [ "$AVAILABLE_SPACE" -lt 5 ]; then
        log_error "可用磁盘空间不足，至少需要5GB空间"
        exit 1
    fi
    log_info "可用磁盘空间: ${AVAILABLE_SPACE}GB"
}

# 安装 Docker 和 Docker Compose
install_docker() {
    log_step "安装 Docker 和 Docker Compose..."

    # 检查是否已安装 Docker
    if command -v docker &> /dev/null; then
        log_info "Docker 已安装，版本: $(docker --version)"
    else
        log_info "安装 Docker..."

        # 更新包索引
        sudo apt update

        # 安装依赖
        sudo apt install -y apt-transport-https ca-certificates curl gnupg lsb-release

        # 添加 Docker 官方 GPG 密钥
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

        # 设置稳定版仓库
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

        # 安装 Docker Engine
        sudo apt update
        sudo apt install -y docker-ce docker-ce-cli containerd.io

        # 启动 Docker 服务
        sudo systemctl start docker
        sudo systemctl enable docker

        # 将当前用户添加到 docker 组
        sudo usermod -aG docker $USER

        log_info "Docker 安装完成"
    fi

    # 检查 Docker Compose
    if command -v docker-compose &> /dev/null; then
        log_info "Docker Compose 已安装，版本: $(docker-compose --version)"
    else
        log_info "安装 Docker Compose..."
        sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose
        log_info "Docker Compose 安装完成"
    fi
}

# 安装依赖工具
install_dependencies() {
    log_step "安装必要的依赖工具..."

    # 安装基本工具
    sudo apt update
    sudo apt install -y curl wget unzip htop tree vim git

    # 安装用于健康检查的 curl
    if ! command -v curl &> /dev/null; then
        sudo apt install -y curl
    fi

    log_info "依赖工具安装完成"
}

# 创建必要的目录
create_directories() {
    log_step "创建必要的目录结构..."

    # 创建日志目录
    mkdir -p logs nginx/logs

    # 创建上传目录
    mkdir -p uploads

    # 创建 MySQL 配置目录
    mkdir -p mysql/conf

    # 创建 SSL 证书目录（可选）
    mkdir -p nginx/ssl

    # 设置权限
    chmod -R 755 logs nginx uploads
    chmod -R 755 mysql nginx

    log_info "目录结构创建完成"
}

# 配置环境变量
configure_environment() {
    log_step "配置环境变量..."

    # 如果不存在 .env 文件，则从模板创建
    if [ ! -f .env ]; then
        if [ -f .env.example ]; then
            cp .env.example .env
            log_info "已从 .env.example 创建 .env 文件"
            log_warn "请根据实际情况修改 .env 文件中的配置"
        else
            log_warn "未找到 .env.example，创建默认 .env 文件"
            cat > .env << EOF
# 数据库配置
MYSQL_ROOT_PASSWORD=warehouse_secure_2024
MYSQL_USER=warehouse_user
MYSQL_PASSWORD=warehouse_pass_2024
DATABASE_URL=jdbc:mysql://mysql:3306/warehouse_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true

# JWT配置
JWT_SECRET=warehouse_management_jwt_secret_2024_secure_key
JWT_EXPIRATION=86400000

# 应用配置
SPRING_PROFILES_ACTIVE=docker
EOF
        fi
    else
        log_info ".env 文件已存在，跳过创建"
    fi
}

# 构建和启动服务
build_and_deploy() {
    log_step "构建和启动服务..."

    # 检查 docker-compose.yml 文件
    if [ ! -f docker-compose.yml ]; then
        log_error "未找到 docker-compose.yml 文件"
        exit 1
    fi

    # 拉取最新代码（如果是 Git 仓库）
    if [ -d .git ]; then
        log_info "拉取最新代码..."
        git pull
    fi

    # 停止可能存在的容器
    log_info "停止现有容器..."
    docker-compose down

    # 构建镜像
    log_info "构建 Docker 镜像..."
    docker-compose build --no-cache

    # 启动服务
    log_info "启动服务..."
    docker-compose up -d

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30

    # 检查服务状态
    check_services
}

# 检查服务状态
check_services() {
    log_step "检查服务状态..."

    # 检查容器状态
    log_info "容器状态:"
    docker-compose ps

    # 检查数据库连接
    log_info "检查数据库连接..."
    if docker-compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD:-warehouse_secure_2024} -e "SELECT 1;" > /dev/null 2>&1; then
        log_info "数据库连接正常"
    else
        log_warn "数据库连接失败，请检查配置"
    fi

    # 检查后端健康状态
    log_info "检查后端服务..."
    if curl -f http://localhost:8083/api/actuator/health > /dev/null 2>&1; then
        log_info "后端服务健康检查通过"
    else
        log_warn "后端服务健康检查失败，可能还在启动中"
    fi

    # 检查前端服务
    log_info "检查前端服务..."
    if curl -f http://localhost > /dev/null 2>&1; then
        log_info "前端服务正常"
    else
        log_warn "前端服务检查失败，可能还在启动中"
    fi
}

# 显示访问信息
show_access_info() {
    log_step "部署完成！"

    # 获取本机IP地址
    LOCAL_IP=$(hostname -I | awk '{print $1}')

    echo ""
    echo "====================================="
    echo "🎉 仓库管理系统部署成功！"
    echo "====================================="
    echo ""
    echo "📱 访问地址："
    echo "   本地访问: http://localhost"
    echo "   网络访问: http://$LOCAL_IP"
    echo ""
    echo "🔧 服务状态："
    echo "   前端服务: http://localhost"
    echo "   后端服务: http://localhost:8083/api"
    echo "   数据库: localhost:3306"
    echo ""
    echo "📊 管理命令："
    echo "   查看日志: docker-compose logs -f [frontend|backend|mysql]"
    echo "   重启服务: docker-compose restart [frontend|backend|mysql]"
    echo "   停止服务: docker-compose down"
    echo "   查看状态: docker-compose ps"
    echo ""
    echo "📁 重要目录："
    echo "   日志目录: ./logs/"
    echo "   上传目录: ./uploads/"
    echo "   数据目录: Docker Volume (mysql_data)"
    echo ""
    echo "🔐 默认数据库信息："
    echo "   数据库: warehouse_management"
    echo "   用户名: root"
    echo "   密码: warehouse_secure_2024"
    echo ""
    echo "⚠️  重要提醒："
    echo "   1. 请及时修改 .env 文件中的密码"
    echo "   2. 建议配置 HTTPS 证书"
    echo "   3. 定期备份数据库"
    echo "   4. 监控系统资源使用情况"
    echo ""
    echo "====================================="
}

# 主函数
main() {
    echo "=========================================="
    echo "🚀 仓库管理系统一键部署脚本"
    echo "=========================================="
    echo ""

    # 检查是否为 root 用户
    if [[ $EUID -eq 0 ]]; then
        log_warn "不建议以 root 用户运行此脚本"
        read -p "是否继续? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi

    # 执行部署步骤
    check_system
    install_docker
    install_dependencies
    create_directories
    configure_environment
    build_and_deploy
    show_access_info

    log_info "部署脚本执行完成！"
}

# 错误处理
trap 'log_error "脚本执行失败，请检查错误信息"; exit 1' ERR

# 运行主函数
main "$@"