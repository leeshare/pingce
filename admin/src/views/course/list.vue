<template>
  <div class="course-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
          <el-button v-if="userStore.hasPermission('course:edit')" type="primary" @click="openCreate">
            <el-icon><Plus /></el-icon> 新增课程
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="课程名称" clearable @keyup.enter="loadList(1)" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 140px">
            <el-option value="语文" label="语文" />
            <el-option value="数学" label="数学" />
            <el-option value="英语" label="英语" />
            <el-option value="面试技巧" label="面试技巧" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option :value="1" label="上架" />
            <el-option :value="0" label="下架" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-select v-model="query.free" placeholder="全部" clearable style="width: 120px">
            <el-option :value="true" label="免费" />
            <el-option :value="false" label="付费" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="tableLoading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="课程" min-width="260">
          <template #default="{ row }">
            <div class="course-cell">
              <el-image
                v-if="row.cover"
                :src="row.cover"
                class="cover"
                fit="cover"
              />
              <div v-else class="cover placeholder">
                <el-icon><VideoCamera /></el-icon>
              </div>
              <div class="course-info">
                <div class="title">{{ row.title }}</div>
                <div v-if="row.intro" class="intro text-muted">{{ row.intro }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.category" :type="categoryTagType(row.category)" size="small">{{ row.category }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="节数" width="80">
          <template #default="{ row }">
            <span class="num">{{ row.lessonCount || 0 }} 节</span>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110">
          <template #default="{ row }">
            <span v-if="!row.price || row.price === 0" class="free-tag">免费</span>
            <span v-else class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="teacher" label="授课老师" width="120">
          <template #default="{ row }">
            <span v-if="row.teacher">{{ row.teacher }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('course:edit')" type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              v-if="userStore.hasPermission('course:edit')"
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="toggleStatus(row)"
            >{{ row.status === 1 ? '下架' : '上架' }}</el-button>
            <el-button v-if="userStore.hasPermission('course:edit')" type="danger" link size="small" @click="deleteOne(row)">删除</el-button>
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
      :title="formMode === 'create' ? '新增课程' : '编辑课程'"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="title">
          <el-input v-model="form.title" maxlength="128" />
        </el-form-item>
        <el-form-item label="课程分类">
          <el-select v-model="form.category" placeholder="请选择" clearable>
            <el-option value="语文" label="语文" />
            <el-option value="数学" label="数学" />
            <el-option value="英语" label="英语" />
            <el-option value="面试技巧" label="面试技巧" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图 URL">
          <el-input v-model="form.cover" placeholder="课程封面图地址" />
        </el-form-item>
        <el-form-item label="课程介绍">
          <el-input v-model="form.intro" type="textarea" :rows="3" maxlength="1000" show-word-limit />
        </el-form-item>
        <el-form-item label="课程节数">
          <el-input-number v-model="form.lessonCount" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="课程价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
          <span class="hint">0 表示免费</span>
        </el-form-item>
        <el-form-item label="授课老师">
          <el-input v-model="form.teacher" maxlength="64" />
        </el-form-item>
        <el-form-item label="上课地点">
          <el-input v-model="form.location" maxlength="128" placeholder="线上课程可留空" />
        </el-form-item>
        <el-form-item label="开课日期">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
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
import { Plus, VideoCamera } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  pageCourses,
  getCourse,
  createCourse,
  updateCourse,
  deleteCourse,
  toggleCourseStatus,
} from '@/api/course'

const userStore = useUserStore()

const query = reactive({
  keyword: '',
  category: '',
  status: null,
  free: null,
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
  title: '',
  category: '',
  cover: '',
  intro: '',
  lessonCount: 0,
  price: 0,
  location: '',
  teacher: '',
  startDate: null,
  status: 1,
})
const rules = {
  title: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
}

function categoryTagType(c) {
  return ({ 语文: 'danger', 数学: 'info', 英语: 'warning', 面试技巧: 'success' })[c] || ''
}

async function loadList(page) {
  if (page) query.page = page
  tableLoading.value = true
  try {
    const res = await pageCourses(query)
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
  query.category = ''
  query.status = null
  query.free = null
  loadList(1)
}

function openCreate() {
  formMode.value = 'create'
  Object.assign(form, {
    id: null,
    title: '',
    category: '',
    cover: '',
    intro: '',
    lessonCount: 0,
    price: 0,
    location: '',
    teacher: '',
    startDate: null,
    status: 1,
  })
  formDialogVisible.value = true
}

async function openEdit(row) {
  try {
    const detail = await getCourse(row.id)
    Object.assign(form, {
      id: detail.id,
      title: detail.title || '',
      category: detail.category || '',
      cover: detail.cover || '',
      intro: detail.intro || '',
      lessonCount: detail.lessonCount || 0,
      price: detail.price || 0,
      location: detail.location || '',
      teacher: detail.teacher || '',
      startDate: detail.startDate || null,
      status: detail.status == null ? 1 : detail.status,
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
      await createCourse(payload)
      ElMessage.success('已新增课程')
    } else {
      await updateCourse(payload)
      ElMessage.success('已更新课程')
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
    await ElMessageBox.confirm(`确认删除课程「${row.title}」？`, '删除确认', { type: 'warning' })
    await deleteCourse(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    // ignore
  }
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  const text = next === 1 ? '上架' : '下架'
  try {
    await toggleCourseStatus(row.id, next)
    ElMessage.success(`已${text}`)
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
.course-list {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .pagination {
    margin-top: 16px;
    text-align: right;
  }

  .course-cell {
    display: flex;
    align-items: center;
    gap: 12px;

    .cover {
      width: 56px;
      height: 40px;
      border-radius: 6px;
      flex-shrink: 0;

      &.placeholder {
        background: #EDF2F7;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #A0AEC0;
        font-size: 20px;
      }
    }

    .course-info {
      flex: 1;
      min-width: 0;

      .title {
        font-weight: 600;
        color: #2D3748;
      }

      .intro {
        margin-top: 2px;
        font-size: 12px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
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

  .free-tag {
    color: #67c23a;
    font-weight: 600;
  }

  .price {
    color: #a51d26;
    font-weight: 700;
  }

  .hint {
    margin-left: 8px;
    color: #A0AEC0;
    font-size: 12px;
  }
}
</style>
