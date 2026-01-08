<template>
  <div class="page-container">
    <el-card>
      <div slot="header">库存调整</div>
      <el-form :model="form" :rules="rules" ref="form" label-width="120px" style="max-width: 600px;">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width: 100%">
            <el-option
              v-for="item in products"
              :key="item.id"
              :label="`${item.name} (当前库存: ${item.stockQty})`"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="调整类型" prop="adjustmentType">
          <el-radio-group v-model="form.adjustmentType">
            <el-radio label="INCREASE">增加</el-radio>
            <el-radio label="DECREASE">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="调整理由" prop="reason">
          <el-input type="textarea" v-model="form.reason" :rows="4" placeholder="请输入调整理由"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { stockAdjustmentApi, productApi } from '@/api'
import { mapState } from 'vuex'

export default {
  name: 'StockAdjustment',
  data() {
    return {
      form: {
        productId: null,
        adjustmentType: 'INCREASE',
        quantity: 1,
        reason: ''
      },
      products: [],
      rules: {
        productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
        adjustmentType: [{ required: true, message: '请选择调整类型', trigger: 'change' }],
        quantity: [{ required: true, message: '请输入调整数量', trigger: 'blur' }],
        reason: [{ required: true, message: '请输入调整理由', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapState('user', ['userInfo'])
  },
  mounted() {
    this.loadProducts()
  },
  methods: {
    loadProducts() {
      productApi.getProducts({ page: 1, size: 1000 }).then(response => {
        this.products = response.data.records || []
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const data = {
            product: { id: this.form.productId },
            adjustmentType: this.form.adjustmentType,
            quantity: this.form.quantity,
            reason: this.form.reason,
            operator: { id: this.userInfo.userId }
          }
          stockAdjustmentApi.createAdjustment(data).then(() => {
            this.$message.success('库存调整成功')
            this.handleReset()
            this.loadProducts()
          }).catch(error => {
            this.$message.error(error.message || '库存调整失败')
          })
        }
      })
    },
    handleReset() {
      this.form = {
        productId: null,
        adjustmentType: 'INCREASE',
        quantity: 1,
        reason: ''
      }
      this.$refs.form.clearValidate()
    }
  }
}
</script>

