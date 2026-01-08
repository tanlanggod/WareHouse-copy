<template>
  <div class="page-container">
    <el-card>
      <div slot="header">商品入库</div>
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="商品" prop="productId">
          <el-select v-model="form.productId" placeholder="请选择商品" filterable style="width: 100%">
            <el-option
              v-for="item in products"
              :key="item.id"
              :label="`${item.name} (${item.code})`"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option
              v-for="item in suppliers"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="入库时间" prop="inboundDate">
          <el-date-picker
            v-model="form.inboundDate"
            type="datetime"
            placeholder="选择入库时间"
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
import { inboundApi, productApi, supplierApi } from '@/api'
import { mapState } from 'vuex'

export default {
  name: 'InboundForm',
  data() {
    return {
      form: {
        productId: null,
        quantity: 1,
        supplierId: null,
        inboundDate: new Date().toISOString().slice(0, 19).replace('T', ' '),
        remark: ''
      },
      products: [],
      suppliers: [],
      rules: {
        productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
        quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapState('user', ['userInfo'])
  },
  mounted() {
    this.loadProducts()
    this.loadSuppliers()
  },
  methods: {
    loadProducts() {
      productApi.getProducts({ page: 1, size: 1000 }).then(response => {
        this.products = response.data.records || []
      })
    },
    loadSuppliers() {
      supplierApi.getAllSuppliers().then(response => {
        this.suppliers = response.data || []
      })
    },
    handleSubmit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          const data = {
            product: { id: this.form.productId },
            quantity: this.form.quantity,
            supplier: this.form.supplierId ? { id: this.form.supplierId } : null,
            inboundDate: this.form.inboundDate,
            operator: { id: this.userInfo.userId },
            remark: this.form.remark
          }
          inboundApi.createInbound(data).then(() => {
            this.$message.success('入库成功')
            this.$router.push('/app/inbounds')
          })
        }
      })
    },
    handleCancel() {
      this.$router.push('/app/inbounds')
    }
  }
}
</script>

