module.exports = {
    devServer: {
        port: 8081,
        // 👇 关键：允许所有 Host（包括 ngrok 的域名）
        allowedHosts: 'all',
        proxy: {
            '/api': {
                // target: 'http://192.168.19.128:8080',
                target: 'http://192.168.19.128:8080',
                changeOrigin: true,
                pathRewrite: {
                    '^/api': '/api'
                }
            }
        }
    },
    lintOnSave: false
}