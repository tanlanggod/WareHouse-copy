<template>
  <div class="page-container">
    <el-card>
      <div slot="header">{{ isEdit ? '编辑商品' : '添加商品' }}</div>
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="商品编号" prop="code">
          <el-input v-model="form.code" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="商品类别" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择类别" style="width: 100%">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="form.price" :precision="2" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="库存数量" prop="stockQty">
          <el-input-number v-model="form.stockQty" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="警戒线" prop="minStock">
          <el-input-number v-model="form.minStock" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit"></el-input>
        </el-form-item>
        <el-form-item label="条形码" prop="barcode">
          <el-input v-model="form.barcode"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" :rows="4"></el-input>
        </el-form-item>
        <el-form-item label="商品图片">
          <image-upload
            v-model="form.imageUrl"
            :action="uploadUrl"
            @success="handleImageSuccess"
            @delete="handleImageDelete">
          </image-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { productApi, categoryApi } from '@/api'
import ImageUpload from '@/components/ImageUpload.vue'

export default {
  name: 'ProductForm',
  data() {
    return {
      isEdit: false,
      form: {
        code: '',
        name: '',
        categoryId: null,
        price: 0,
        stockQty: 0,
        minStock: 0,
        unit: '件',
        barcode: '',
        description: '',
        imageUrl: '',
        version: null // 添加版本号字段
      },
      categories: [],
      currentVersion: null, // 保存当前版本号
      rules: {
        code: [{ required: true, message: '请输入商品编号', trigger: 'blur' }],
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        price: [{ required: true, message: '请输入单价', trigger: 'blur' }]
      }
    }
  },
  components: {
    ImageUpload
  },
  computed: {
    uploadUrl() {
      if (this.$route.params.id) {
        return `/api/products/${this.$route.params.id}/image`
      }
      return ''
    }
  },
  mounted() {
    this.loadCategories()
    if (this.$route.params.id) {
      this.isEdit = true
      this.loadProduct()
    }
  },
  methods: {
    loadCategories() {
      categoryApi.getAllCategories().then(response => {
        this.categories = response.data || []
      })
    },
    loadProduct() {
      productApi.getProductById(this.$route.params.id).then(response => {
        const product = response.data
        this.form = {
          code: product.code,
          name: product.name,
          categoryId: product.category ? product.category.id : null,
          price: product.price,
          stockQty: product.stockQty,
          minStock: product.minStock,
          unit: product.unit,
          barcode: product.barcode || '',
          description: product.description || '',
          imageUrl: product.imageUrl || '',
          version: product.version // 添加版本号
        }
        this.currentVersion = product.version // 保存当前版本号
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const data = {
            ...this.form,
            category: this.form.categoryId ? { id: this.form.categoryId } : null
          }
          if (this.isEdit) {
            productApi.updateProduct(this.$route.params.id, data).then(() => {
              this.$message.success('更新成功')
              this.$router.push('/app/products')
            })
          } else {
            productApi.createProduct(data).then(() => {
              this.$message.success('创建成功')
              this.$router.push('/app/products')
            })
          }
        }
      })
    },
    handleCancel() {
      this.$router.push('/app/products')
    },
    // 图片上传成功
    handleImageSuccess(response) {
      // 处理不同格式的响应数据
      let imageUrl = null;
      let version = null;

      if (typeof response === 'string') {
        // 旧格式：直接返回URL字符串
        imageUrl = response;
      } else if (response && response.imageUrl) {
        // 新格式：返回包含imageUrl和version的对象
        imageUrl = response.imageUrl;
        version = response.version;
      }

      if (imageUrl) {
        this.form.imageUrl = imageUrl;

        // 如果有版本号信息，更新版本号
        if (version !== null && version !== undefined) {
          this.form.version = version;
          this.currentVersion = version;
        }

        this.$message.success('图片上传成功');
      } else {
        console.error('图片上传响应处理失败:', response);
        this.$message.error('图片上传失败：响应格式错误');
      }
    },
    // 图片删除
    async handleImageDelete(response) {
      try {
        if (this.isEdit && this.$route.params.id) {
          // 调用删除图片API
          const res = await productApi.deleteProductImage(this.$route.params.id)
          if (res.code === 200) {
            this.form.imageUrl = ''

            // 更新版本号为最新值（如果API返回了版本号）
            if (res.data && res.data.version !== null && res.data.version !== undefined) {
              this.form.version = res.data.version
              this.currentVersion = res.data.version
            }

            this.$message.success('图片已删除')
          } else {
            this.$message.error(res.message || '图片删除失败')
          }
        } else {
          // 新增模式，直接清空图片URL
          this.form.imageUrl = ''
          this.$message.success('图片已删除')
        }
      } catch (error) {
        console.error('删除图片失败:', error)
        // 即使API失败也清空表单中的图片URL
        this.form.imageUrl = ''
        this.$message.success('图片已删除')
      }
    }
  }
}
</script>

