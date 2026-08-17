<template>
  <div class="admin-user-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>管理员列表</span>
          <div>
            <el-button type="primary" @click="openCreate">
              <el-icon><Plus /></el-icon> 新增管理员
            </el-button>
            <el-button type="info" @click="openMyPermissions">
              <el-icon><InfoFilled /></el-icon> 权限清单
            </el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="用户名/昵称" clearable @keyup.enter="loadList(1)" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.isSuper" placeholder="全部" clearable style="width: 120px">
            <el-option :value="1" label="超管" />
            <el-option :value="0" label="普通" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="tableLoading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="160" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isSuper === 1" type="danger" size="small" effect="dark">超管</el-tag>
            <el-tag v-else type="info" size="small">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限" min-width="280">
          <template #default="{ row }">
            <template v-if="row.isSuper === 1">
              <el-tag type="warning" size="small">全部权限</el-tag>
            </template>
            <template v-else-if="row.permissions && row.permissions.length">
              <el-tag
                v-for="p in row.permissions"
                :key="p"
                size="small"
                class="perm-tag"
              >{{ permissionLabel(p) }}</el-tag>
            </template>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" width="170" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="row.isSuper !== 1 && userStore.isSuper"
              type="warning"
              link
              size="small"
              @click="openPermission(row)"
            >分配权限</el-button>
            <el-button
              v-if="row.isSuper !== 1 && userStore.isSuper && row.id !== userStore.userInfo.id"
              type="danger"
              link
              size="small"
              @click="deleteOne(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadList()"
        @size-change="loadList(1)"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formMode === 'create' ? '新增管理员' : '编辑管理员'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            :disabled="formMode === 'edit'"
            placeholder="字母/数字/下划线"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item :label="formMode === 'create' ? '初始密码' : '重置密码'" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="formMode === 'create' ? '请输入初始密码' : '留空表示不修改密码'"
            maxlength="64"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" maxlength="64" />
        </el-form-item>
        <el-form-item label="头像">
          <el-input v-model="form.avatar" placeholder="可选，URL" maxlength="512" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="userStore.isSuper && formMode === 'create'" label="是否超管">
          <el-radio-group v-model="form.isSuper">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是（拥有全部权限）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="userStore.isSuper && form.isSuper === 0 && formMode === 'create'" label="初始权限">
          <el-checkbox-group v-model="form.permissionList">
            <el-checkbox
              v-for="opt in permissionOptions"
              :key="opt.code"
              :value="opt.code"
            >{{ opt.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限 弹窗 -->
    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="600px">
      <div v-if="permissionTarget" class="perm-assign">
        <div class="perm-target">
          <span class="label">目标账号：</span>
          <span>{{ permissionTarget.username }}</span>
          <span v-if="permissionTarget.nickname" class="text-muted">（{{ permissionTarget.nickname }}）</span>
        </div>
        <el-checkbox-group v-model="permissionForm.permissions">
          <div v-for="group in groupedPermissions" :key="group.name" class="perm-group">
            <div class="group-title">{{ group.name }}</div>
            <el-checkbox
              v-for="opt in group.items"
              :key="opt.code"
              :value="opt.code"
            >{{ opt.name }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionLoading" @click="submitPermission">保存</el-button>
      </template>
    </el-dialog>

    <!-- 权限清单 弹窗 -->
    <el-dialog v-model="permissionListDialogVisible" title="系统权限清单" width="600px">
      <div v-for="group in groupedPermissions" :key="group.name" class="perm-group">
        <div class="group-title">{{ group.name }}</div>
        <div class="perm-list">
          <el-tag v-for="opt in group.items" :key="opt.code" size="small" class="perm-tag">
            {{ opt.name }} <span class="perm-code">{{ opt.code }}</span>
          </el-tag>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  pageAdminUsers,
  createAdminUser,
  updateAdminUser,
  deleteAdminUser,
  assignPermissions,
  listPermissionOptions,
} from '@/api/admin-user'

const userStore = useUserStore()

const query = reactive({
  keyword: '',
  status: null,
  isSuper: null,
  page: 1,
  size: 10,
})
const list = ref([])
const total = ref(0)
const tableLoading = ref(false)

const formDialogVisible = ref(false)
const formMode = ref('create')
const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  password: '',
  nickname: '',
  avatar: '',
  status: 1,
  isSuper: 0,
  permissionList: [],
})
const formLoading = ref(false)
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字、下划线', trigger: 'blur' },
  ],
  password: [
    {
      validator: (rule, value, callback) => {
        if (formMode.value === 'create' && !value) {
          callback(new Error('请输入初始密码'))
        } else if (value && value.length < 6) {
          callback(new Error('密码至少 6 位'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const permissionOptions = ref([])

const groupedPermissions = computed(() => {
  const map = new Map()
  permissionOptions.value.forEach((opt) => {
    if (!map.has(opt.group)) map.set(opt.group, [])
    map.get(opt.group).push(opt)
  })
  return Array.from(map.entries()).map(([name, items]) => ({ name, items }))
})

function permissionLabel(code) {
  const opt = permissionOptions.value.find((o) => o.code === code)
  return opt ? opt.name : code
}

const permissionDialogVisible = ref(false)
const permissionTarget = ref(null)
const permissionForm = reactive({ permissions: [] })
const permissionLoading = ref(false)

const permissionListDialogVisible = ref(false)

function openMyPermissions() {
  permissionListDialogVisible.value = true
}

async function loadOptions() {
  try {
    permissionOptions.value = await listPermissionOptions()
  } catch (e) {
    // ignore
  }
}

async function loadList(page) {
  if (page) query.page = page
  tableLoading.value = true
  try {
    const res = await pageAdminUsers(query)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    // ignore
  } finally {
    tableLoading.value = false
  }
}

function resetQuery() {
  query.keyword = ''
  query.status = null
  query.isSuper = null
  loadList(1)
}

function openCreate() {
  formMode.value = 'create'
  Object.assign(form, {
    id: null,
    username: '',
    password: '',
    nickname: '',
    avatar: '',
    status: 1,
    isSuper: 0,
    permissionList: [],
  })
  formDialogVisible.value = true
}

function openEdit(row) {
  formMode.value = 'edit'
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    nickname: row.nickname || '',
    avatar: row.avatar || '',
    status: row.status,
    isSuper: row.isSuper,
    permissionList: row.permissions || [],
  })
  formDialogVisible.value = true
}

async function submitForm() {
  formLoading.value = true
  try {
    // validate 校验失败会 reject，必须放进 try 否则会冒泡触发 dev-server overlay
    await formRef.value.validate()
    const payload = {
      username: form.username,
      password: form.password || undefined,
      nickname: form.nickname || undefined,
      avatar: form.avatar || undefined,
      status: form.status,
      isSuper: form.isSuper,
      permissions: form.isSuper === 0 ? form.permissionList.join(',') : undefined,
    }
    if (formMode.value === 'create') {
      await createAdminUser(payload)
      ElMessage.success('已创建管理员')
    } else {
      await updateAdminUser(form.id, payload)
      ElMessage.success('已更新管理员')
      // 如果修改的是当前登录账号，刷新一下本地信息
      if (form.id === userStore.userInfo?.id) {
        await userStore.refreshInfo()
      }
    }
    formDialogVisible.value = false
    loadList()
  } catch (e) {
    // 校验失败：由 Element Plus 在表单项上标红，无需额外弹窗
    // API 错误：由 request.js 拦截器统一 ElMessage.error 提示
  } finally {
    formLoading.value = false
  }
}

async function deleteOne(row) {
  try {
    // confirm 取消时会 reject 'cancel'，必须放进 try 否则会冒泡触发 dev-server overlay
    await ElMessageBox.confirm(`确认删除管理员 ${row.username}？`, '删除确认', { type: 'warning' })
    await deleteAdminUser(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    // 用户取消：无需处理；API 错误由 request.js 拦截器提示
  }
}

function openPermission(row) {
  permissionTarget.value = row
  permissionForm.permissions = [...(row.permissions || [])]
  permissionDialogVisible.value = true
}

async function submitPermission() {
  permissionLoading.value = true
  try {
    await assignPermissions(permissionTarget.value.id, permissionForm.permissions)
    ElMessage.success('权限已更新')
    permissionDialogVisible.value = false
    loadList()
  } catch (e) {
    // ignore
  } finally {
    permissionLoading.value = false
  }
}

onMounted(() => {
  loadOptions()
  loadList(1)
})
</script>

<style lang="scss" scoped>
.admin-user-list {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .pagination {
    margin-top: 16px;
    text-align: right;
  }

  .perm-tag {
    margin: 0 6px 6px 0;
  }

  .text-muted {
    color: #A0AEC0;
    font-size: 13px;
  }

  .perm-assign {
    .perm-target {
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px dashed #E2E8F0;

      .label {
        color: #4A5568;
      }
    }

    .perm-group {
      margin-bottom: 16px;

      .group-title {
        font-weight: 600;
        color: #2C5282;
        margin-bottom: 8px;
      }
    }
  }

  .perm-list {
    display: flex;
    flex-wrap: wrap;

    .perm-tag {
      display: inline-flex;
      align-items: center;
      margin: 0 8px 8px 0;

      .perm-code {
        margin-left: 6px;
        color: #718096;
        font-size: 12px;
      }
    }
  }
}
</style>
