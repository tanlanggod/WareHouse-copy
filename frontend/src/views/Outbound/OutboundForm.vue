<template>
  <div class="page-container">
    <el-card>
      <div slot="header">商品出库</div>
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width: 100%">
            <el-option
              v-for="item in products"
              :key="item.id"
              :label="`${item.name} (${item.code}) - 库存: ${item.stockQty}`"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="请选择客户" style="width: 100%">
            <el-option
              v-for="item in customers"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="出库时间" prop="outboundDate">
          <el-date-picker
            v-model="form.outboundDate"
            type="datetime"
            placeholder="选择出库时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input type="textarea" v-model="form.remark" :rows="4"></el-input>
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
import { outboundApi, productApi, customerApi } from '@/api'
import { mapState } from 'vuex'

export default {
  name: 'OutboundForm',
  data() {
    return {
      form: {
        productId: null,
        quantity: 1,
        customerId: null,
        outboundDate: new Date().toISOString().slice(0, 19).replace('T', ' '),
        remark: ''
      },
      products: [],
      customers: [],
      rules: {
        productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
        quantity: [{ required: true, message: '请输入出库数量', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapState('user', ['userInfo'])
  },
  mounted() {
    this.loadProducts()
    this.loadCustomers()
  },
  methods: {
    loadProducts() {
      productApi.getProducts({ page: 1, size: 1000 }).then(response => {
        this.products = response.data.records || []
      })
    },
    loadCustomers() {
      customerApi.getAllCustomers().then(response => {
        this.customers = response.data || []
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const data = {
            product: { id: this.form.productId },
            quantity: this.form.quantity,
            customer: this.form.customerId ? { id: this.form.customerId } : null,
            outboundDate: this.form.outboundDate,
            operator: { id: this.userInfo.userId },
            remark: this.form.remark
          }
          outboundApi.createOutbound(data).then(() => {
            this.$message.success('出库成功')
            this.$router.push('/app/outbounds')
          }).catch(error => {
            this.$message.error(error.message || '出库失败')
          })
        }
      })
    },
    handleCancel() {
      this.$router.push('/app/outbounds')
    }
  }
}
</script>

