<template>
  <div class="page-container">
    <el-card>
      <div slot="header">
        <span>商品类别管理</span>
        <el-button style="float: right;" type="primary" @click="handleAdd">添加类别</el-button>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="name" label="类别名称"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="类别名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" :rows="4"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { categoryApi } from '@/api'

export default {
  name: 'CategoryList',
  data() {
    return {
      tableData: [],
      dialogVisible: false,
      dialogTitle: '添加类别',
      form: {
        id: null,
        name: '',
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入类别名称', trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    loadData() {
      categoryApi.getAllCategories().then(response => {
        this.tableData = response.data || []
      })
    },
    handleAdd() {
      this.dialogTitle = '添加类别'
      this.form = { id: null, name: '', description: '' }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑类别'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          if (this.form.id) {
            categoryApi.updateCategory(this.form.id, this.form).then(() => {
              this.$message.success('更新成功')
              this.dialogVisible = false
              this.loadData()
            })
          } else {
            categoryApi.createCategory(this.form).then(() => {
              this.$message.success('创建成功')
              this.dialogVisible = false
              this.loadData()
            })
          }
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该类别吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        categoryApi.deleteCategory(row.id).then(() => {
          this.$message.success('删除成功')
          this.loadData()
        })
      })
    }
  }
}
</script>

