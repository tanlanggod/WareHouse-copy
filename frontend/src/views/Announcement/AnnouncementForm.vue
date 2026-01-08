<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="dialogVisible"
    width="80%"
    :before-close="handleClose"
    @close="handleClose"
    class="announcement-form-dialog">

    <el-form
      ref="announcementForm"
      :model="formData"
      :rules="rules"
      label-width="120px"
      class="announcement-form">

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="公告标题" prop="title">
            <el-input
              v-model="formData.title"
              placeholder="请输入公告标题"
              maxlength="200"
              show-word-limit>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="公告类型" prop="type">
            <el-select
              v-model="formData.type"
              placeholder="请选择公告类型"
              style="width: 100%">
              <el-option
                v-for="type in announcementTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-select
              v-model="formData.priority"
              placeholder="请选择优先级"
              style="width: 100%">
              <el-option
                v-for="priority in announcementPriorities"
                :key="priority.value"
                :label="priority.label"
                :value="priority.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="目标受众" prop="targetAudience">
            <el-select
              v-model="formData.targetAudience"
              placeholder="请选择目标受众"
              style="width: 100%"
              @change="handleTargetAudienceChange">
              <el-option
                v-for="audience in targetAudiences"
                :key="audience.value"
                :label="audience.label"
                :value="audience.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20" v-if="formData.targetAudience === 'ROLE_BASED'">
        <el-col :span="12">
          <el-form-item label="目标角色" prop="targetRoles">
            <el-select
              v-model="formData.targetRoles"
              placeholder="请选择目标角色"
              style="width: 100%"
              multiple>
              <el-option
                v-for="role in userRoles"
                :key="role.value"
                :label="role.label"
                :value="role.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="显示类型" prop="displayType">
            <el-select
              v-model="formData.displayType"
              placeholder="请选择显示类型"
              style="width: 100%">
              <el-option
                v-for="displayType in displayTypes"
                :key="displayType.value"
                :label="displayType.label"
                :value="displayType.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="生效时间">
            <el-date-picker
              v-model="effectiveTimeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              @change="handleEffectiveTimeChange">
            </el-date-picker>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="是否置顶">
            <el-switch
              v-model="formData.isPinned"
              active-text="置顶"
              inactive-text="普通">
            </el-switch>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item label="发布状态" v-if="isEdit">
            <el-tag :type="getStatusTagType(formData.status)">
              {{ getStatusText(formData.status) }}
            </el-tag>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="公告内容" prop="content">
        <div class="editor-container">
          <div ref="editor" class="editor"></div>
        </div>
      </el-form-item>

      <el-form-item label="附件上传">
        <el-upload
          ref="upload"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :file-list="fileList"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :on-remove="handleFileRemove"
          :before-upload="beforeUpload"
          :limit="5"
          :on-exceed="handleExceed">
          <el-button size="small" type="primary">点击上传</el-button>
          <div slot="tip" class="el-upload__tip">
            只能上传 jpg/png/pdf/word/excel 文件，且不超过 10MB
          </div>
        </el-upload>
      </el-form-item>
    </el-form>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button
        v-if="!isEdit || isDraft"
        type="info"
        @click="handleSaveDraft"
        :loading="loading">
        保存草稿
      </el-button>
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="loading">
        {{ getSubmitButtonText() }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { announcementApi, userApi } from '@/api'
import { getToken } from '@/utils/auth'
import E from 'wangeditor'

export default {
  name: 'AnnouncementForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    announcementData: {
      type: Object,
      default: () => ({})
    },
    isEdit: {
      type: Boolean,
      default: false
    },
    isDraft: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      dialogVisible: false,
      loading: false,
      editor: null,
      effectiveTimeRange: [],
      fileList: [],
      uploadUrl: process.env.VUE_APP_BASE_API + '/files/announcements/upload',
      uploadHeaders: {
        Authorization: 'Bearer ' + getToken()
      },

      formData: {
        title: '',
        content: '',
        type: 'SYSTEM',
        priority: 'NORMAL',
        targetAudience: 'ALL',
        targetRoles: [],
        displayType: 'NOTIFICATION',
        isPinned: false,
        effectiveStartTime: null,
        effectiveEndTime: null,
        attachmentUrl: '',
        attachmentName: ''
      },

      rules: {
        title: [
          { required: true, message: '请输入公告标题', trigger: 'blur' },
          { max: 200, message: '标题长度不能超过200个字符', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '请输入公告内容', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请选择公告类型', trigger: 'change' }
        ],
        priority: [
          { required: true, message: '请选择优先级', trigger: 'change' }
        ],
        targetAudience: [
          { required: true, message: '请选择目标受众', trigger: 'change' }
        ],
        targetRoles: [
          { validator: this.validateTargetRoles, trigger: 'change' }
        ]
      },

      // 下拉选项数据
      announcementTypes: [],
      announcementPriorities: [],
      targetAudiences: [],
      displayTypes: [],
      userRoles: []
    }
  },
  computed: {
    dialogTitle() {
      if (this.isEdit) {
        return this.isDraft ? '编辑公告草稿' : '编辑公告'
      }
      return this.isDraft ? '创建公告草稿' : '发布公告'
    }
  },
  watch: {
    visible: {
      handler(val) {
        this.dialogVisible = val
        if (val) {
          this.initFormData()
          this.loadDropdownData()
          this.$nextTick(() => {
            this.initEditor()
          })
        }
      },
      immediate: true
    },
    dialogVisible(val) {
      this.$emit('update:visible', val)
      if (!val) {
        this.destroyEditor()
      }
    }
  },
  methods: {
    // 初始化表单数据
    initFormData() {
      if (this.isEdit && this.announcementData) {
        this.formData = {
          id: this.announcementData.id,
          title: this.announcementData.title || '',
          content: this.announcementData.content || '',
          type: this.announcementData.type || 'SYSTEM',
          priority: this.announcementData.priority || 'NORMAL',
          targetAudience: this.announcementData.targetAudience || 'ALL',
          targetRoles: this.announcementData.targetRolesList || [],
          displayType: this.announcementData.displayType || 'NOTIFICATION',
          isPinned: this.announcementData.isPinned || false,
          effectiveStartTime: this.announcementData.effectiveStartTime,
          effectiveEndTime: this.announcementData.effectiveEndTime,
          attachmentUrl: this.announcementData.attachmentUrl || '',
          attachmentName: this.announcementData.attachmentName || '',
          status: this.announcementData.status || 'DRAFT'
        }

        // 设置生效时间范围
        if (this.announcementData.effectiveStartTime && this.announcementData.effectiveEndTime) {
          this.effectiveTimeRange = [
            this.formatDateTimeForPicker(this.announcementData.effectiveStartTime),
            this.formatDateTimeForPicker(this.announcementData.effectiveEndTime)
          ]
        } else {
          this.effectiveTimeRange = []
        }

        // 设置附件
        if (this.announcementData.attachmentUrl && this.announcementData.attachmentName) {
          this.fileList = [{
            name: this.announcementData.attachmentName,
            url: this.announcementData.attachmentUrl
          }]
        } else {
          this.fileList = []
        }
      } else {
        this.formData = {
          title: '',
          content: '',
          type: 'SYSTEM',
          priority: 'NORMAL',
          targetAudience: 'ALL',
          targetRoles: [],
          displayType: 'NOTIFICATION',
          isPinned: false,
          effectiveStartTime: null,
          effectiveEndTime: null,
          attachmentUrl: '',
          attachmentName: ''
        }
        this.effectiveTimeRange = []
        this.fileList = []
      }
    },

    // 加载下拉选项数据
    async loadDropdownData() {
      try {
        const [typesRes, prioritiesRes, audiencesRes, displayTypesRes, rolesRes] = await Promise.all([
          announcementApi.getAnnouncementTypes(),
          announcementApi.getAnnouncementPriorities(),
          announcementApi.getTargetAudiences(),
          announcementApi.getDisplayTypes(),
          announcementApi.getUserRoles()
        ])

        if (typesRes.code === 200) this.announcementTypes = typesRes.data
        if (prioritiesRes.code === 200) this.announcementPriorities = prioritiesRes.data
        if (audiencesRes.code === 200) this.targetAudiences = audiencesRes.data
        if (displayTypesRes.code === 200) this.displayTypes = displayTypesRes.data
        if (rolesRes.code === 200) this.userRoles = rolesRes.data
      } catch (error) {
        console.error('加载下拉选项失败:', error)
      }
    },

    // 初始化富文本编辑器
    initEditor() {
      if (this.editor) {
        this.editor.destroy()
      }

      this.editor = new E(this.$refs.editor)

      // 配置编辑器
      this.editor.config.height = 300
      this.editor.config.placeholder = '请输入公告内容...'

      // 配置菜单
      this.editor.config.menus = [
        'head', // 标题
        'bold', // 粗体
        'fontSize', // 字号
        'fontName', // 字体
        'italic', // 斜体
        'underline', // 下划线
        'strikeThrough', // 删除线
        'foreColor', // 文字颜色
        'backColor', // 背景颜色
        'link', // 插入链接
        'list', // 列表
        'justify', // 对齐方式
        'quote', // 引用
        'emoticon', // 表情
        'image', // 插入图片
        'table', // 表格
        'video', // 插入视频
        'code', // 插入代码
        'undo', // 撤销
        'redo' // 重复
      ]

      // 配置上传图片
      this.editor.config.uploadImgShowBase64 = false
      this.editor.config.uploadImgServer = this.uploadUrl
      this.editor.config.uploadImgHeaders = this.uploadHeaders
      this.editor.config.uploadImgMaxSize = 5 * 1024 * 1024 // 5M
      this.editor.config.uploadImgAccept = ['jpg', 'jpeg', 'png', 'gif']
      this.editor.config.uploadFileName = 'file'

      // 设置内容
      this.editor.config.onchange = (html) => {
        this.formData.content = html
      }

      // 创建编辑器
      this.editor.create()

      // 设置初始内容
      if (this.formData.content) {
        this.editor.txt.html(this.formData.content)
      }
    },

    // 销毁编辑器
    destroyEditor() {
      if (this.editor) {
        this.editor.destroy()
        this.editor = null
      }
    },

    // 目标受众变化处理
    handleTargetAudienceChange(value) {
      if (value !== 'ROLE_BASED') {
        this.formData.targetRoles = []
      }
    },

    // 生效时间变化处理
    handleEffectiveTimeChange(value) {
      if (value && value.length === 2) {
        this.formData.effectiveStartTime = value[0]
        this.formData.effectiveEndTime = value[1]
      } else {
        this.formData.effectiveStartTime = null
        this.formData.effectiveEndTime = null
      }
    },

    // 验证目标角色
    validateTargetRoles(rule, value, callback) {
      if (this.formData.targetAudience === 'ROLE_BASED') {
        if (!value || value.length === 0) {
          callback(new Error('请选择目标角色'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    },

    // 保存草稿
    async handleSaveDraft() {
      this.formData.status = 'DRAFT'
      await this.submitForm(false)
    },

    // 提交表单
    async handleSubmit() {
      this.formData.status = 'PUBLISHED'
      await this.submitForm(true)
    },

    // 提交表单公共逻辑
    async submitForm(isPublish) {
      try {
        await this.$refs.announcementForm.validate()

        // 验证内容
        if (!this.formData.content || this.formData.content.trim() === '<p><br></p>') {
          this.$message.error('请输入公告内容')
          return
        }

        this.loading = true

        const submitData = {
          title: this.formData.title,
          content: this.formData.content,
          type: this.formData.type,
          priority: this.formData.priority,
          targetAudience: this.formData.targetAudience,
          targetRoles: this.formData.targetRoles,
          displayType: this.formData.displayType,
          isPinned: this.formData.isPinned,
          effectiveStartTime: this.formData.effectiveStartTime,
          effectiveEndTime: this.formData.effectiveEndTime,
          attachmentUrl: this.formData.attachmentUrl,
          attachmentName: this.formData.attachmentName
        }

        let response
        if (this.isEdit) {
          // 编辑公告
          response = await announcementApi.updateAnnouncement(this.formData.id, submitData)
        } else {
          // 创建公告
          if (isPublish) {
            response = await announcementApi.publishAnnouncement(submitData)
          } else {
            response = await announcementApi.createDraft(submitData)
          }
        }

        if (response.code === 200) {
          this.$message.success(
            this.isEdit ?
            (isPublish ? '公告更新成功' : '草稿更新成功') :
            (isPublish ? '公告发布成功' : '草稿创建成功')
          )
          this.$emit('success')
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        if (error !== false) {
          this.$message.error('操作失败，请重试')
        }
      } finally {
        this.loading = false
      }
    },

    // 文件上传前的验证
    beforeUpload(file) {
      const allowedTypes = [
        'image/jpeg', 'image/jpg', 'image/png', 'image/gif',
        'application/pdf', 'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      ]

      const isAllowedType = allowedTypes.includes(file.type)
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isAllowedType) {
        this.$message.error('只支持 jpg/png/pdf/word/excel 格式的文件!')
        return false
      }
      if (!isLt10M) {
        this.$message.error('文件大小不能超过 10MB!')
        return false
      }

      return true
    },

    // 文件上传成功
    handleUploadSuccess(response, file) {
      if (response.code === 200) {
        this.formData.attachmentUrl = response.data.url
        this.formData.attachmentName = response.data.originalName
        this.$message.success('文件上传成功')
      } else {
        this.$message.error(response.message || '文件上传失败')
        this.$refs.upload.removeFile(file)
      }
    },

    // 文件上传失败
    handleUploadError(error, file) {
      console.error('文件上传失败:', error)
      this.$message.error('文件上传失败')
      this.$refs.upload.removeFile(file)
    },

    // 移除文件
    handleFileRemove(file) {
      this.formData.attachmentUrl = ''
      this.formData.attachmentName = ''
    },

    // 文件数量超限
    handleExceed(files, fileList) {
      this.$message.warning('最多只能上传 5 个文件!')
    },

    // 关闭弹窗
    handleClose() {
      this.$refs.announcementForm?.clearValidate()
      this.dialogVisible = false
    },

    // 获取提交按钮文本
    getSubmitButtonText() {
      if (this.isEdit) {
        return this.isDraft ? '更新并发布' : '更新公告'
      }
      return '发布公告'
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        'DRAFT': '草稿',
        'PUBLISHED': '已发布',
        'EXPIRED': '已过期',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    },

    // 获取状态标签类型
    getStatusTagType(status) {
      const statusMap = {
        'DRAFT': 'info',
        'PUBLISHED': 'success',
        'EXPIRED': 'warning',
        'CANCELLED': 'danger'
      }
      return statusMap[status] || 'info'
    },

    // 格式化日期时间用于选择器
    formatDateTimeForPicker(dateTime) {
      if (!dateTime) return null
      const date = new Date(dateTime)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
  }
}
</script>

<style scoped>
.announcement-form-dialog .el-dialog {
  min-height: 600px;
}

.announcement-form {
  padding: 0 20px;
}

.dialog-footer {
  text-align: right;
}

.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.editor {
  width: 100%;
}

:deep(.w-e-toolbar) {
  border-bottom: 1px solid #dcdfe6;
}

:deep(.w-e-text-container) {
  background-color: #fff;
}

:deep(.w-e-text) {
  min-height: 300px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcement-form-dialog .el-dialog {
    width: 95% !important;
    margin: 0 auto;
  }

  .el-form-item {
    margin-bottom: 18px;
  }
}

/* 调整表单项间距 */
.el-form-item {
  margin-bottom: 22px;
}

.el-upload__tip {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}
</style>