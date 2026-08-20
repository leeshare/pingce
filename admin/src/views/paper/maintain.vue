<template>
  <div class="paper-maintain">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>真卷维护</span>
          <el-button type="primary" :icon="Plus" @click="openCreate">新增试卷</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="业务分区">
          <el-select v-model="query.bizSection" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="o in bizSectionOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="categoryCascader"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
            placeholder="全部"
            clearable
            style="width: 160px"
            @change="onCategoryChange"
          />
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number v-model="query.year" :min="2000" :max="2099" :controls="false" style="width: 110px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option :value="0" label="草稿" />
            <el-option :value="1" label="已发布" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="试卷名称" clearable @keyup.enter="loadList(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="tableLoading" border>
        <el-table-column prop="title" label="试卷名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="业务分区" width="90">
          <template #default="{ row }">{{ bizSectionText(row.bizSection) }}</template>
        </el-table-column>
        <el-table-column label="分类" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ categoryMap[row.categoryId] || row.categoryId }}</template>
        </el-table-column>
        <el-table-column prop="year" label="年份" width="70" />
        <el-table-column prop="source" label="来源" width="150" show-overflow-tooltip />
        <el-table-column prop="duration" label="时长(分)" width="80" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column prop="passScore" label="及格分" width="80" />
        <el-table-column label="题数" width="70">
          <template #default="{ row }">{{ countIds(row.questionIds) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="remove(row)">删除</el-button>
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

    <!-- 试卷编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑试卷' : '新增试卷'" width="760px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="试卷名称" prop="title">
          <el-input v-model="form.title" placeholder="如 2026年单招语文真题" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="业务分区" prop="bizSection">
          <el-select v-model="form.bizSection" style="width: 200px">
            <el-option v-for="o in bizSectionOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-cascader
            v-model="formCategoryCascader"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
            placeholder="请选择分类"
            style="width: 260px"
            @change="onFormCategoryChange"
          />
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number v-model="form.year" :min="2000" :max="2099" :controls="false" style="width: 120px" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="form.source" placeholder="如 2025年乙(A)试卷" maxlength="128" show-word-limit style="width: 320px" />
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="300" :controls="false" style="width: 120px" />
          <span class="unit">分钟</span>
        </el-form-item>
        <el-form-item label="总分" prop="totalScore">
          <el-input-number v-model="form.totalScore" :min="1" :max="500" :controls="false" style="width: 120px" />
        </el-form-item>
        <el-form-item label="及格分" prop="passScore">
          <el-input-number v-model="form.passScore" :min="0" :max="500" :controls="false" style="width: 120px" />
        </el-form-item>
        <el-form-item label="试卷说明">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="选填" maxlength="512" show-word-limit />
        </el-form-item>
        <el-form-item label="题目组成">
          <div class="question-compose">
            <el-input
              v-model="form.questionIds"
              type="textarea"
              :rows="2"
              placeholder="题目ID，逗号分隔；可点击右侧按钮从题库选择"
            />
            <div class="compose-actions">
              <el-button type="primary" plain size="small" @click="openPicker">从题库选题</el-button>
              <span class="compose-count">已选 {{ countIds(form.questionIds) }} 题</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 从题库选题弹窗 -->
    <el-dialog v-model="pickerVisible" title="从题库选题" width="900px" :close-on-click-modal="false" append-to-body>
      <el-form :inline="true" :model="pickerQuery">
        <el-form-item label="分类">
          <el-cascader
            v-model="pickerCategoryCascader"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
            placeholder="全部"
            clearable
            style="width: 160px"
            @change="onPickerCategoryChange"
          />
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number v-model="pickerQuery.year" :min="2000" :max="2099" :controls="false" style="width: 110px" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="pickerQuery.type" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="pickerQuery.keyword" placeholder="题干" clearable @keyup.enter="loadPicker(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPicker(1)">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table
        ref="pickerTableRef"
        :data="pickerList"
        v-loading="pickerLoading"
        border
        row-key="id"
        @selection-change="onPickerSelectionChange"
      >
        <el-table-column type="selection" width="45" :reserve-selection="true" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="题型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 7 ? 'danger' : ''" size="small">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ categoryMap[row.categoryId] || row.categoryId }}</template>
        </el-table-column>
        <el-table-column prop="content" label="题干" min-width="280" show-overflow-tooltip />
        <el-table-column prop="year" label="年份" width="70" />
      </el-table>
      <el-pagination
        v-model:current-page="pickerQuery.page"
        v-model:page-size="pickerQuery.size"
        :total="pickerTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadPicker()"
        @size-change="loadPicker(1)"
        class="pagination"
      />
      <div class="picker-summary">当前已勾选 {{ selectedIds.length }} 题</div>
      <template #footer>
        <el-button @click="pickerVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPick">确认选择</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  listPapers,
  createPaper,
  updatePaper,
  deletePaper,
} from '@/api/paper'
import { listForEdit, listCategories } from '@/api/question'

const bizSectionOptions = [
  { value: 1, label: '单招' },
  { value: 2, label: '普通' },
  { value: 3, label: '中考' },
  { value: 4, label: '高考' },
  { value: 5, label: '考研' },
]
const typeOptions = [
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
  { value: 4, label: '填空题' },
  { value: 5, label: '简答题' },
  { value: 6, label: '计算题' },
  { value: 7, label: '复合题' },
]

function bizSectionText(v) {
  return ({ 1: '单招', 2: '普通', 3: '中考', 4: '高考', 5: '考研' })[v] || '-'
}
function typeText(v) {
  return ({ 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题', 6: '计算题', 7: '复合题' })[v] || '-'
}
function countIds(ids) {
  if (!ids) return 0
  return ids.split(',').filter((s) => s && s.trim()).length
}

// ==================== 列表 ====================
const query = reactive({
  bizSection: null,
  categoryId: null,
  year: null,
  status: null,
  keyword: '',
  page: 1,
  size: 10,
})
const list = ref([])
const total = ref(0)
const tableLoading = ref(false)
const categoryCascader = ref(null)
const categoryTree = ref([])

function onCategoryChange(val) {
  query.categoryId = val || null
}
function resetQuery() {
  query.bizSection = null
  query.categoryId = null
  query.year = null
  query.status = null
  query.keyword = ''
  categoryCascader.value = null
  loadList(1)
}

async function loadList(page) {
  if (page) query.page = page
  tableLoading.value = true
  try {
    const res = await listPapers(query)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    tableLoading.value = false
  }
}

// ==================== 分类树 ====================
function buildTree(list) {
  const map = {}
  const roots = []
  list.forEach((i) => {
    map[i.id] = { id: i.id, name: i.name, parentId: i.parentId, sort: i.sort, children: [] }
  })
  list.forEach((i) => {
    const node = map[i.id]
    if (!i.parentId || i.parentId === 0) roots.push(node)
    else if (map[i.parentId]) map[i.parentId].children.push(node)
    else roots.push(node)
  })
  const sortRec = (arr) => {
    arr.sort((a, b) => (a.sort || 0) - (b.sort || 0))
    arr.forEach((n) => {
      if (n.children && n.children.length === 0) delete n.children
      else if (n.children) sortRec(n.children)
    })
  }
  sortRec(roots)
  return roots
}
async function loadCategories() {
  try {
    const list = await listCategories()
    categoryTree.value = buildTree(list || [])
  } catch (e) {
    // ignore
  }
}
const categoryMap = computed(() => {
  const map = {}
  const walk = (nodes) => {
    ;(nodes || []).forEach((n) => {
      map[n.id] = n.name
      if (n.children) walk(n.children)
    })
  }
  walk(categoryTree.value)
  return map
})

// ==================== 编辑弹窗 ====================
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const formCategoryCascader = ref(null)
const defaultForm = () => ({
  id: null,
  bizSection: 1,
  title: '',
  categoryId: null,
  year: null,
  source: '',
  description: '',
  duration: 90,
  totalScore: 100,
  passScore: 60,
  questionIds: '',
  status: 0,
})
const form = reactive(defaultForm())
const rules = {
  title: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  bizSection: [{ required: true, message: '请选择业务分区', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  duration: [{ required: true, message: '请填写考试时长', trigger: 'blur' }],
  totalScore: [{ required: true, message: '请填写总分', trigger: 'blur' }],
}
function onFormCategoryChange(val) {
  form.categoryId = val || null
}
function openCreate() {
  Object.assign(form, defaultForm())
  formCategoryCascader.value = null
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}
function openEdit(row) {
  Object.assign(form, defaultForm(), row)
  formCategoryCascader.value = row.categoryId || null
  dialogVisible.value = true
  nextTick(() => formRef.value && formRef.value.clearValidate())
}
async function submit() {
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const payload = { ...form }
    if (payload.id) {
      await updatePaper(payload)
      ElMessage.success('已保存')
    } else {
      delete payload.id
      await createPaper(payload)
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // ignore
  } finally {
    submitting.value = false
  }
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除试卷「${row.title}」？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await deletePaper(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    // ignore
  }
}

// ==================== 选题弹窗 ====================
const pickerVisible = ref(false)
const pickerTableRef = ref(null)
const pickerLoading = ref(false)
const pickerCategoryCascader = ref(null)
const pickerQuery = reactive({
  bizSection: 1,
  categoryId: null,
  type: null,
  year: null,
  keyword: '',
  status: 2, // 仅已通过
  parentId: 0, // 仅独立题+复合题大题
  page: 1,
  size: 10,
})
const pickerList = ref([])
const pickerTotal = ref(0)
const selectedIds = ref([])

function onPickerCategoryChange(val) {
  pickerQuery.categoryId = val || null
}
function onPickerSelectionChange(rows) {
  selectedIds.value = rows.map((r) => r.id)
}
function openPicker() {
  // 打开时按当前试卷的业务分区/分类预置查询
  pickerQuery.bizSection = form.bizSection || 1
  pickerQuery.categoryId = form.categoryId || null
  pickerCategoryCascader.value = form.categoryId || null
  pickerQuery.page = 1
  pickerVisible.value = true
  selectedIds.value = []
  nextTick(() => {
    pickerTableRef.value && pickerTableRef.value.clearSelection()
    loadPicker(1)
  })
}
async function loadPicker(page) {
  if (page) pickerQuery.page = page
  pickerLoading.value = true
  try {
    const res = await listForEdit(pickerQuery)
    pickerList.value = res.records || []
    pickerTotal.value = res.total || 0
    // 恢复已选状态
    nextTick(() => {
      const existIds = (form.questionIds || '')
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
        .map((s) => Number(s))
      pickerList.value.forEach((row) => {
        if (existIds.includes(row.id) || selectedIds.value.includes(row.id)) {
          pickerTableRef.value && pickerTableRef.value.toggleRowSelection(row, true)
        }
      })
    })
  } catch (e) {
    // ignore
  } finally {
    pickerLoading.value = false
  }
}
function confirmPick() {
  // 合并：已选 + 之前已存在但不在当前页的
  const existIds = (form.questionIds || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean)
    .map((s) => Number(s))
  const currentIds = pickerList.value.map((r) => r.id)
  // 保留非当前页的旧选择 + 当前页勾选
  const merged = [
    ...existIds.filter((id) => !currentIds.includes(id)),
    ...selectedIds.value,
  ]
  // 去重
  const dedup = Array.from(new Set(merged))
  form.questionIds = dedup.join(',')
  pickerVisible.value = false
}

onMounted(() => {
  loadCategories()
  loadList(1)
})
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.question-compose {
  width: 100%;
  .compose-actions {
    margin-top: 6px;
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .compose-count {
    color: #718096;
    font-size: 13px;
  }
}
.unit {
  margin-left: 8px;
  color: #718096;
}
.picker-summary {
  margin-top: 8px;
  color: #2B6CB0;
  font-size: 13px;
}
</style>
