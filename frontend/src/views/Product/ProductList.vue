<template>
  <div class="page-container">
    <el-card>
      <div slot="header">
        <span>商品管理</span>
        <div style="float: right;">
          <el-upload
            ref="upload"
            action=""
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImport"
            accept=".xlsx,.xls"
            style="display: inline-block; margin-right: 10px;">
            <el-button type="success" size="small">导入Excel</el-button>
          </el-upload>
          <el-button type="warning" size="small" @click="handleExport" style="margin-right: 10px;">导出Excel</el-button>
          <el-button type="primary" @click="handleAdd">添加商品</el-button>
        </div>
      </div>
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="商品名称">
            <el-input v-model="searchForm.name" placeholder="请输入商品名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="类别">
            <el-select v-model="searchForm.categoryId" placeholder="请选择类别" clearable>
              <el-option
                v-for="item in categories"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="code" label="商品编号" width="120"></el-table-column>
        <el-table-column label="商品图片" width="100">
          <template slot-scope="scope">
            <div class="product-image">
              <img
                v-if="scope.row.imageUrl"
                :src="scope.row.imageUrl"
                :alt="scope.row.name"
                class="product-thumbnail"
                @error="handleImageError"
              />
              <div v-else class="no-image">
                <i class="el-icon-picture-outline"></i>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称"></el-table-column>
        <el-table-column prop="category.name" label="类别" width="120"></el-table-column>
        <el-table-column prop="price" label="单价" width="100">
          <template slot-scope="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stockQty" label="库存数量" width="100"></el-table-column>
        <el-table-column prop="minStock" label="警戒线" width="100"></el-table-column>
        <el-table-column prop="unit" label="单位" width="80"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        style="margin-top: 20px; text-align: right;">
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
import { productApi, categoryApi } from '@/api'

export default {
  name: 'ProductList',
  data() {
    return {
      searchForm: {
        name: '',
        categoryId: null
      },
      tableData: [],
      categories: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      }
    }
  },
  mounted() {
    this.loadCategories()
    this.loadData()
  },
  methods: {
    loadCategories() {
      categoryApi.getAllCategories().then(response => {
        this.categories = response.data || []
      })
    },
    loadData() {
      const params = {
        page: this.pagination.page,
        size: this.pagination.size,
        name: this.searchForm.name || undefined,
        categoryId: this.searchForm.categoryId || undefined
      }
      productApi.getProducts(params).then(response => {
        this.tableData = response.data.records || []
        this.pagination.total = response.data.total || 0
      })
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = {
        name: '',
        categoryId: null
      }
      this.handleSearch()
    },
    handleAdd() {
      this.$router.push('/app/products/add')
    },
    handleEdit(row) {
      this.$router.push(`/app/products/edit/${row.id}`)
    },
    handleDelete(row) {
      this.$confirm('确定要删除该商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        productApi.deleteProduct(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadData()
        })
      })
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
    },
    handleExport() {
      this.$confirm('确定要导出所有商品数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        const loading = this.$loading({
          lock: true,
          text: '导出中...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        })
        productApi.exportProducts().then(response => {
          const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
          const url = window.URL.createObjectURL(blob)
          const link = document.createElement('a')
          link.href = url
          link.download = '商品数据_' + new Date().getTime() + '.xlsx'
          link.click()
          window.URL.revokeObjectURL(url)
          loading.close()
          this.$message.success('导出成功')
        }).catch(() => {
          loading.close()
          this.$message.error('导出失败')
        })
      })
    },
    handleImport(file) {
      const loading = this.$loading({
        lock: true,
        text: '导入中...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      productApi.importProducts(file.raw).then(response => {
        loading.close()
        if (response.data && response.data.length > 0) {
          const errorMsg = response.data.join('<br/>')
          this.$alert(errorMsg, '导入结果', {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '确定'
          })
        } else {
          this.$message.success('导入成功')
        }
        this.loadData()
      }).catch(() => {
        loading.close()
        this.$message.error('导入失败')
      })
    },
    // 图片加载错误处理
    handleImageError() {
      // 图片加载失败时的处理逻辑
      console.log('图片加载失败')
    }
  }
}
</script>

<style scoped>
/* 商品图片样式 */
.product-image {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
}

.product-thumbnail {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.no-image {
  width: 60px;
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  color: #c0c4cc;
  font-size: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-image {
    width: 40px;
    height: 40px;
  }

  .product-thumbnail {
    width: 40px;
    height: 40px;
  }

  .no-image {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
}
</style>

