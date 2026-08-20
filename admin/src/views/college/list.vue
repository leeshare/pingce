<template>
  <div class="college-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>院校管理</span>
          <el-button v-if="userStore.hasPermission('college:edit')" type="primary" @click="openCreate">
            <el-icon><Plus /></el-icon> 新增院校
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="院校名称" clearable @keyup.enter="loadList(1)" />
        </el-form-item>
        <el-form-item label="办学性质">
          <el-select v-model="query.nature" placeholder="全部" clearable style="width: 120px">
            <el-option value="公办" label="公办" />
            <el-option value="民办" label="民办" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="query.city" placeholder="如 西安市" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="双高计划">
          <el-select v-model="query.isDoubleHigh" placeholder="全部" clearable style="width: 120px">
            <el-option :value="1" label="是" />
            <el-option :value="0" label="否" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="tableLoading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="院校" min-width="240">
          <template #default="{ row }">
            <div class="college-cell">
              <el-avatar :size="36" :src="row.logo" shape="square">
                <el-icon><School /></el-icon>
              </el-avatar>
              <div class="college-info">
                <div class="name">{{ row.name }}</div>
                <div v-if="row.code" class="text-muted">代码：{{ row.code }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="办学性质" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.nature" :type="row.nature === '公办' ? 'danger' : 'warning'" size="small">{{ row.nature }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <span v-if="row.type">{{ row.type }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="层次" width="90">
          <template #default="{ row }">
            <span v-if="row.level">{{ row.level }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="双高" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isDoubleHigh === 1" type="success" size="small">双高</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="地区" min-width="140">
          <template #default="{ row }">
            <span v-if="row.province || row.city">
              {{ [row.province, row.city].filter(Boolean).join(' / ') }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="majorCount" label="专业数" width="90">
          <template #default="{ row }">
            <span class="num">{{ row.majorCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('college:edit')" type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="userStore.hasPermission('college:edit')" type="danger" link size="small" @click="deleteOne(row)">删除</el-button>
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
      :title="formMode === 'create' ? '新增院校' : '编辑院校'"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="院校名称" prop="name">
          <el-input v-model="form.name" maxlength="128" />
        </el-form-item>
        <el-form-item label="院校代码">
          <el-input v-model="form.code" maxlength="32" />
        </el-form-item>
        <el-form-item label="办学性质">
          <el-radio-group v-model="form.nature">
            <el-radio value="公办">公办</el-radio>
            <el-radio value="民办">民办</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="院校类型">
          <el-select v-model="form.type" placeholder="请选择" clearable>
            <el-option value="综合" label="综合" />
            <el-option value="理工" label="理工" />
            <el-option value="师范" label="师范" />
            <el-option value="财经" label="财经" />
            <el-option value="医药" label="医药" />
            <el-option value="政法" label="政法" />
            <el-option value="艺术" label="艺术" />
            <el-option value="农林" label="农林" />
          </el-select>
        </el-form-item>
        <el-form-item label="层次">
          <el-radio-group v-model="form.level">
            <el-radio value="本科">本科</el-radio>
            <el-radio value="专科">专科</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="双高计划">
          <el-switch v-model="form.isDoubleHigh" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="如 陕西省" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" placeholder="如 西安市" />
        </el-form-item>
        <el-form-item label="Logo URL">
          <el-input v-model="form.logo" placeholder="院校 logo 图片地址" />
        </el-form-item>
        <el-form-item label="院校简介">
          <el-input v-model="form.intro" type="textarea" :rows="4" maxlength="2000" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, School } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  pageColleges,
  getCollege,
  createCollege,
  updateCollege,
  deleteCollege,
} from '@/api/college'

const userStore = useUserStore()

const query = reactive({
  keyword: '',
  nature: '',
  city: '',
  isDoubleHigh: null,
  page: 1,
  size: 10,
})
const list = ref([])
const total = ref(0)
const tableLoading = ref(false)

const formDialogVisible = ref(false)
const formMode = ref('create')
const formRef = ref(null)
const formLoading = ref(false)
const form = reactive({
  id: null,
  name: '',
  code: '',
  nature: '公办',
  type: '',
  level: '专科',
  isDoubleHigh: 0,
  province: '陕西省',
  city: '',
  logo: '',
  intro: '',
})
const rules = {
  name: [{ required: true, message: '请输入院校名称', trigger: 'blur' }],
}

async function loadList(page) {
  if (page) query.page = page
  tableLoading.value = true
  try {
    const res = await pageColleges(query)
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
  query.nature = ''
  query.city = ''
  query.isDoubleHigh = null
  loadList(1)
}

function openCreate() {
  formMode.value = 'create'
  Object.assign(form, {
    id: null,
    name: '',
    code: '',
    nature: '公办',
    type: '',
    level: '专科',
    isDoubleHigh: 0,
    province: '陕西省',
    city: '',
    logo: '',
    intro: '',
  })
  formDialogVisible.value = true
}

async function openEdit(row) {
  try {
    const detail = await getCollege(row.id)
    Object.assign(form, {
      id: detail.id,
      name: detail.name || '',
      code: detail.code || '',
      nature: detail.nature || '公办',
      type: detail.type || '',
      level: detail.level || '专科',
      isDoubleHigh: detail.isDoubleHigh || 0,
      province: detail.province || '陕西省',
      city: detail.city || '',
      logo: detail.logo || '',
      intro: detail.intro || '',
    })
    formMode.value = 'edit'
    formDialogVisible.value = true
  } catch (e) {
    // ignore
  }
}

async function submitForm() {
  formLoading.value = true
  try {
    await formRef.value.validate()
    const payload = { ...form }
    if (formMode.value === 'create') {
      await createCollege(payload)
      ElMessage.success('已新增院校')
    } else {
      await updateCollege(payload)
      ElMessage.success('已更新院校')
    }
    formDialogVisible.value = false
    loadList()
  } catch (e) {
    // ignore
  } finally {
    formLoading.value = false
  }
}

async function deleteOne(row) {
  try {
    await ElMessageBox.confirm(`确认删除院校「${row.name}」？`, '删除确认', { type: 'warning' })
    await deleteCollege(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    // ignore
  }
}

onMounted(() => {
  loadList(1)
})
</script>

<style lang="scss" scoped>
.college-list {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .pagination {
    margin-top: 16px;
    text-align: right;
  }

  .college-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .college-info {
      .name {
        font-weight: 600;
        color: #2D3748;
      }
    }
  }

  .text-muted {
    color: #A0AEC0;
    font-size: 12px;
  }

  .num {
    font-weight: 600;
    color: #2C5282;
  }
}
</style>
