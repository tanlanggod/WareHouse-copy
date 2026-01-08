<template>
  <div class="image-upload">
    <!-- 图片展示区域 -->
    <div class="image-preview" v-if="imageUrl">
      <img :src="imageUrl" :alt="alt" class="preview-image" @error="handleImageError" />
      <div class="image-actions">
        <el-button size="mini" type="danger" @click="handleDelete" :disabled="disabled">
          <i class="el-icon-delete"></i> 删除
        </el-button>
        <el-button size="mini" type="primary" @click="handleReplace" :disabled="disabled">
          <i class="el-icon-edit"></i> 替换
        </el-button>
      </div>
    </div>

    <!-- 上传区域 -->
    <div class="upload-area" v-else>
      <el-upload
        ref="upload"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :before-upload="beforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :show-file-list="false"
        :disabled="disabled"
        accept="image/*"
        :drag="drag">
        <div class="upload-content">
          <i class="el-icon-upload upload-icon"></i>
          <div class="upload-text">
            <p>{{ drag ? '将文件拖到此处，或' : '点击' }}上传</p>
            <p class="upload-tip">支持 jpg、png、gif、webp 格式，大小不超过 5MB</p>
          </div>
        </div>
      </el-upload>
    </div>

    <!-- 进度条 -->
    <div class="upload-progress" v-if="uploading">
      <el-progress :percentage="uploadPercentage" :status="uploadStatus"></el-progress>
    </div>
  </div>
</template>

<script>
import store from '@/store'

export default {
  name: 'ImageUpload',
  props: {
    // 图片URL
    value: {
      type: String,
      default: ''
    },
    // 上传接口URL
    action: {
      type: String,
      required: true
    },
    // 是否可拖拽
    drag: {
      type: Boolean,
      default: true
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    },
    // 图片alt属性
    alt: {
      type: String,
      default: '图片'
    },
    // 最大文件大小（MB）
    maxSize: {
      type: Number,
      default: 5
    }
  },
  data() {
    return {
      imageUrl: this.value,
      uploading: false,
      uploadPercentage: 0,
      uploadStatus: ''
    }
  },
  computed: {
    uploadUrl() {
      return this.action
    },
    uploadHeaders() {
      const token = store.state.user.token
      return token ? { Authorization: `Bearer ${token}` } : {}
    }
  },
  watch: {
    value(newVal) {
      this.imageUrl = newVal
    },
    imageUrl(newVal) {
      this.$emit('input', newVal)
    }
  },
  methods: {
    // 上传前验证
    beforeUpload(file) {
      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        this.$message.error('只能上传图片文件!')
        return false
      }

      // 检查文件大小
      const isLtMaxSize = file.size / 1024 / 1024 < this.maxSize
      if (!isLtMaxSize) {
        this.$message.error(`图片大小不能超过 ${this.maxSize}MB!`)
        return false
      }

      // 开始上传
      this.uploading = true
      this.uploadPercentage = 0
      this.uploadStatus = ''

      return true
    },

    // 上传成功
    handleSuccess(response) {
      this.uploading = false
      this.uploadPercentage = 100
      this.uploadStatus = 'success'

      if (response.code === 200) {
        // 兼容多种响应格式
        if (typeof response.data === 'string') {
          // 旧格式：直接返回URL字符串
          this.imageUrl = response.data
          this.$message.success('图片上传成功')
          this.$emit('success', response.data)
        } else if (response.data && response.data.imageUrl) {
          // 新格式：返回包含imageUrl和version的对象
          this.imageUrl = response.data.imageUrl
          this.$message.success('图片上传成功')
          this.$emit('success', response.data) // 传递完整响应，包含版本号信息
        } else if (response.data && typeof response.data === 'object' && Object.keys(response.data).length > 0) {
          // 尝试从完整商品对象中获取imageUrl（为了向后兼容）
          if (response.data.imageUrl) {
            this.imageUrl = response.data.imageUrl
            this.$message.success('图片上传成功')
            this.$emit('success', {
              imageUrl: response.data.imageUrl,
              version: response.data.version
            })
          } else {
            console.error('图片上传响应缺少imageUrl字段:', response.data)
            this.$message.error('图片上传失败：响应格式错误，缺少图片URL')
          }
        } else {
          console.error('图片上传响应格式错误:', response.data)
          this.$message.error('图片上传失败：响应格式错误')
        }
      } else {
        console.error('图片上传失败:', response.message)
        this.$message.error(response.message || '图片上传失败')
      }
    },

    // 上传失败
    handleError(error) {
      this.uploading = false
      this.uploadStatus = 'exception'
      this.$message.error('图片上传失败')
      console.error('图片上传失败:', error)
    },

    // 删除图片
    handleDelete() {
      this.$confirm('确定要删除这张图片吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.imageUrl = ''
        this.$emit('delete')
        this.$message.success('图片已删除')
      }).catch(() => {
        // 用户取消删除
      })
    },

    // 替换图片
    handleReplace() {
      this.$refs.upload.$el.querySelector('input[type=file]').click()
    },

    // 图片加载错误
    handleImageError() {
      this.$message.error('图片加载失败')
    }
  }
}
</script>

<style scoped>
.image-upload {
  width: 100%;
}

.image-preview {
  position: relative;
  display: inline-block;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.preview-image {
  display: block;
  max-width: 200px;
  max-height: 200px;
  object-fit: contain;
}

.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview:hover .image-actions {
  opacity: 1;
}

.upload-area {
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  background-color: #fafafa;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
}

.upload-content {
  padding: 40px 20px;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.upload-text p {
  margin: 0;
  line-height: 1.5;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.upload-progress {
  margin-top: 16px;
}

/* 上传组件样式重写 */
.image-upload ::v-deep .el-upload {
  width: 100%;
}

.image-upload ::v-deep .el-upload-dragger {
  width: 100%;
  height: auto;
  border: none;
  background: transparent;
}

.image-upload ::v-deep .el-upload-dragger .el-icon-upload {
  font-size: 48px;
  color: #c0c4cc;
  margin: 0 0 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .preview-image {
    max-width: 150px;
    max-height: 150px;
  }

  .upload-content {
    padding: 30px 15px;
  }

  .upload-icon {
    font-size: 36px;
  }
}
</style>