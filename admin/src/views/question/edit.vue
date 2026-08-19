<template>
  <div class="question-edit">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>试题编辑</span>
          <div>
            <el-button type="danger" :disabled="!selected.length" @click="batchDelete">
              <el-icon><Delete /></el-icon> 批量删除
            </el-button>
            <el-button type="primary" @click="goEntry">
              <el-icon><Plus /></el-icon> 录入新题
            </el-button>
          </div>
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
            v-model="queryCategoryCascader"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
            placeholder="全部"
            clearable
            style="width: 120px"
            @change="(v) => (query.categoryId = v)"
          />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="query.difficulty" placeholder="全部" clearable style="width: 110px">
            <el-option :value="1" label="简单" />
            <el-option :value="2" label="中等" />
            <el-option :value="3" label="困难" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option :value="0" label="待校对" />
            <el-option :value="1" label="待审核" />
            <el-option :value="2" label="已通过" />
            <el-option :value="3" label="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number v-model="query.year" :min="2000" :max="2099" :controls="false" style="width: 100px" />
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="题干关键字" clearable @keyup.enter="loadList(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="list"
        v-loading="tableLoading"
        border
        row-key="id"
        :row-class-name="rowClassName"
        @selection-change="onSelectionChange"
        @expand-change="onExpandChange"
      >
        <el-table-column type="expand" width="40">
          <template #default="{ row }">
            <div v-if="row.type === 7" class="children-wrap">
              <div v-if="row._childrenLoading" class="loading-hint">子题加载中...</div>
              <div v-else-if="row._children && row._children.length">
                <div class="children-title">子题（{{ row._children.length }}）</div>
                <div v-for="(child, idx) in row._children" :key="child.id" class="child-item">
                  <div class="child-head">
                    <el-tag size="small" type="info">{{ idx + 1 }}. {{ typeText(child.type) }}</el-tag>
                    <span v-if="child.score" class="child-score">{{ child.score }}分</span>
                    <span v-if="child.subType" class="child-sub">{{ child.subType }}</span>
                  </div>
                  <div class="child-content">{{ child.content }}</div>
                  <div v-if="parseOptions(child.options).length" class="child-options">
                    <div v-for="(opt, oi) in parseOptions(child.options)" :key="oi" class="child-opt">
                      <span class="opt-letter">{{ optionLetters[oi] }}</span>
                      <span>{{ opt }}</span>
                    </div>
                  </div>
                  <div v-if="child.answer" class="child-answer">
                    <span class="label">答案：</span>{{ child.answer }}
                  </div>
                  <div v-if="child.analysis" class="child-analysis">
                    <span class="label">解析：</span>{{ child.analysis }}
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无子题" :image-size="40" />
            </div>
          </template>
        </el-table-column>
        <el-table-column type="selection" width="50" />
        <el-table-column label="题型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ categoryText(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="题干" min-width="320" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="content-cell">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">{{ difficultyText(row.difficulty) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="160" show-overflow-tooltip />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="updatedAt" label="更新时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button type="success" link size="small" @click="openCopy(row)">复制</el-button>
            <el-button type="danger" link size="small" @click="deleteOne(row)">删除</el-button>
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="820px" :close-on-click-modal="false">
      <el-form v-if="form" ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="业务分区" prop="bizSection">
              <el-select v-model="form.bizSection" style="width: 100%">
                <el-option v-for="o in bizSectionOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="题型" prop="type">
              <el-select v-model="form.type" style="width: 100%" @change="onTypeChange">
                <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" style="width: 100%">
                <el-option :value="1" label="简单" />
                <el-option :value="2" label="中等" />
                <el-option :value="3" label="困难" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="分类">
              <el-cascader
                v-model="categoryCascader"
                :options="categoryTree"
                :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
                placeholder="请选择"
                clearable
                style="width: 100%"
                @change="(v) => (form.categoryId = v)"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分值">
              <el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="年份">
              <el-input-number v-model="form.year" :min="2000" :max="2099" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="来源">
              <el-input v-model="form.source" maxlength="128" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="子题型">
          <el-input v-model="form.subType" maxlength="32" style="width: 360px" />
        </el-form-item>

        <el-form-item label="题干" prop="content">
          <el-input v-model="form.content" type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" maxlength="5000" show-word-limit />
        </el-form-item>

        <el-form-item v-if="hasOptions" label="选项">
          <div class="options-wrap">
            <div v-for="(opt, idx) in form.options" :key="idx" class="option-row">
              <span class="opt-label">{{ optionLetters[idx] }}</span>
              <el-input v-model="form.options[idx]" />
              <el-button v-if="form.options.length > 2" type="danger" link @click="removeOption(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button v-if="form.options.length < 6" type="primary" link @click="addOption">
              <el-icon><Plus /></el-icon> 添加选项
            </el-button>
          </div>
        </el-form-item>

        <el-form-item v-if="form.type !== 7" label="正确答案" prop="answer">
          <el-input v-model="form.answer" :placeholder="answerPlaceholder" />
        </el-form-item>

        <el-form-item v-if="form.type !== 7" label="答案解析">
          <el-input v-model="form.analysis" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">待校对</el-radio>
            <el-radio :value="1">待审核</el-radio>
            <el-radio :value="2">直接通过</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <!-- 复合题子题列表（可编辑） -->
      <div v-if="form && form.type === 7" class="children-block">
        <div class="children-block-title">
          <span>子题列表</span>
          <el-tag v-if="childrenLoaded" size="small" type="info">{{ children.length }} 道</el-tag>
        </div>
        <div v-if="childrenLoading" class="loading-hint">加载中...</div>
        <div v-else-if="children.length">
          <div v-for="(child, idx) in children" :key="child.id" class="child-item">
            <div class="child-head">
              <el-tag size="small" type="info">{{ idx + 1 }}. 子题 #{{ child.id }}</el-tag>
            </div>
            <el-form label-width="72px" class="child-form">
              <el-row :gutter="12">
                <el-col :span="6">
                  <el-form-item label="题型">
                    <el-select v-model="child.type" style="width: 100%" @change="onChildTypeChange(child)">
                      <el-option :value="1" label="单选题" />
                      <el-option :value="2" label="多选题" />
                      <el-option :value="3" label="判断题" />
                      <el-option :value="4" label="填空题" />
                      <el-option :value="5" label="简答题" />
                      <el-option :value="6" label="计算题" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="分值">
                    <el-input-number v-model="child.score" :min="0" :max="100" :step="0.5" :precision="1" style="width: 100%" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="子题型">
                    <el-input v-model="child.subType" maxlength="32" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="题干">
                <el-input v-model="child.content" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" />
              </el-form-item>
              <el-form-item v-if="childHasOptions(child)" label="选项">
                <div class="options-wrap">
                  <div v-for="(_, oi) in child.options" :key="oi" class="option-row">
                    <span class="opt-label">{{ optionLetters[oi] }}</span>
                    <el-input v-model="child.options[oi]" />
                    <el-button v-if="child.options.length > 2" type="danger" link @click="removeChildOption(child, oi)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <el-button v-if="child.options.length < 6" type="primary" link @click="addChildOption(child)">
                    <el-icon><Plus /></el-icon> 添加选项
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="答案">
                <el-input v-model="child.answer" />
              </el-form-item>
              <el-form-item label="解析">
                <el-input v-model="child.analysis" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" />
              </el-form-item>
            </el-form>
          </div>
        </div>
        <el-empty v-else description="暂无子题" :image-size="60" />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import { listForEdit, getQuestionDetail, updateQuestion, createQuestion, deleteQuestion, batchDeleteQuestion, listCategories, getQuestionChildren } from '@/api/question'

const router = useRouter()

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
const optionLetters = ['A', 'B', 'C', 'D', 'E', 'F']

const query = reactive({
  bizSection: null,
  categoryId: null,
  type: null,
  difficulty: null,
  status: null,
  year: null,
  keyword: '',
  page: 1,
  size: 10,
})
const list = ref([])
const total = ref(0)
const tableLoading = ref(false)
const selected = ref([])

const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref(null)
const categoryCascader = ref(null)
const queryCategoryCascader = ref(null)
const categoryTree = ref([])
const saveLoading = ref(false)
// 复合题子题（编辑弹窗内只读展示）
const children = ref([])
const childrenLoading = ref(false)
const childrenLoaded = ref(false)
// 复制模式标识：true 表示当前弹窗是"复制创建新试题"
const isCopy = ref(false)
const dialogTitle = computed(() => (isCopy.value ? '复制创建新试题' : '试题编辑'))

const rules = {
  bizSection: [{ required: true, message: '请选择业务分区', trigger: 'change' }],
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  // 复合题(type=7)的答案在子题中，父题不要求答案；其他题型必填
  answer: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (form.value && form.value.type === 7) {
          // 复合题：跳过答案校验
          callback()
          return
        }
        if (!value || !value.trim()) {
          callback(new Error('请输入正确答案'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const hasOptions = computed(() => form.value && [1, 2, 3].includes(form.value.type))

const answerPlaceholder = computed(() => {
  if (!form.value) return ''
  switch (form.value.type) {
    case 1: return '单选答案，如：A'
    case 2: return '多选答案，如：ABD'
    case 3: return '判断答案，如：正确 或 错误'
    case 4: return '填空答案，多个空用 | 分隔'
    case 5: return '简答参考范文'
    case 6: return '计算题参考答案'
    case 7: return '复合题无需统一答案'
    default: return ''
  }
})

function typeText(v) {
  return ({ 1: '单选题', 2: '多选题', 3: '判断题', 4: '填空题', 5: '简答题', 6: '计算题', 7: '复合题' })[v] || '-'
}
function typeTagType(v) {
  return ({ 1: '', 2: 'success', 3: 'warning', 4: 'info', 5: 'info', 6: 'info', 7: 'danger' })[v] || ''
}
function difficultyText(v) {
  return ({ 1: '简单', 2: '中等', 3: '困难' })[v] || '-'
}
function statusText(s) {
  return ({ 0: '待校对', 1: '待审核', 2: '已通过', 3: '已驳回' })[s] || '未知'
}
function statusTagType(s) {
  return ({ 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' })[s] || 'info'
}

function parseOptions(opts) {
  if (!opts) return []
  try {
    return JSON.parse(opts) || []
  } catch (e) {
    return []
  }
}

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

// 扁平化分类树，用于在表格中按 categoryId 显示分类名
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

function categoryText(id) {
  if (!id) return '-'
  return categoryMap.value[id] || '-'
}

async function loadList(page) {
  if (page) query.page = page
  tableLoading.value = true
  try {
    const res = await listForEdit(query)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    // ignore
  } finally {
    tableLoading.value = false
  }
}

function resetQuery() {
  query.bizSection = null
  query.categoryId = null
  query.type = null
  query.difficulty = null
  query.status = null
  query.year = null
  query.keyword = ''
  queryCategoryCascader.value = null
  loadList(1)
}

function onSelectionChange(rows) {
  selected.value = rows
}

function rowClassName({ row }) {
  return row.type === 7 ? 'is-composite' : 'no-expand'
}

async function onExpandChange(row, expandedRows) {
  if (row.type !== 7) return
  const isExpanded = expandedRows.some((r) => r.id === row.id)
  if (!isExpanded || row._children) return
  row._childrenLoading = true
  try {
    row._children = await getQuestionChildren(row.id)
  } catch (e) {
    row._children = []
  } finally {
    row._childrenLoading = false
  }
}

function goEntry() {
  router.push('/question/entry')
}

function onTypeChange() {
  if (!form.value) return
  if (hasOptions.value && form.value.options.length === 0) {
    form.value.options = ['', '', '', '']
  }
  if (!hasOptions.value) {
    form.value.options = []
  }
}

function addOption() {
  if (form.value.options.length < 6) form.value.options.push('')
}
function removeOption(idx) {
  form.value.options.splice(idx, 1)
}

async function openEdit(row) {
  isCopy.value = false
  try {
    const detail = await getQuestionDetail(row.id)
    form.value = {
      id: detail.id,
      bizSection: detail.bizSection,
      categoryId: detail.categoryId,
      parentId: detail.parentId,
      type: detail.type,
      subType: detail.subType || '',
      sort: detail.sort,
      difficulty: detail.difficulty,
      content: detail.content,
      options: parseOptions(detail.options),
      answer: detail.answer || '',
      score: detail.score,
      analysis: detail.analysis || '',
      year: detail.year,
      source: detail.source || '',
      status: detail.status,
    }
    categoryCascader.value = detail.categoryId
    // 重置子题状态
    children.value = []
    childrenLoaded.value = false
    dialogVisible.value = true
    // 复合题：加载子题列表，并将 options 解析为数组，便于双向绑定
    if (detail.type === 7) {
      childrenLoading.value = true
      try {
        const list = await getQuestionChildren(detail.id)
        children.value = (list || []).map((c) => ({
          id: c.id,
          parentId: detail.id,
          bizSection: c.bizSection ?? detail.bizSection,
          categoryId: c.categoryId ?? detail.categoryId,
          type: c.type,
          subType: c.subType || '',
          sort: c.sort,
          difficulty: c.difficulty ?? detail.difficulty,
          content: c.content || '',
          options: parseOptions(c.options),
          answer: c.answer || '',
          score: c.score,
          analysis: c.analysis || '',
          year: c.year ?? detail.year,
          source: c.source ?? detail.source,
          status: c.status ?? detail.status,
        }))
        childrenLoaded.value = true
      } catch (e) {
        children.value = []
      } finally {
        childrenLoading.value = false
      }
    }
  } catch (e) {
    // ignore
  }
}

// 复制：基于现有题目快速创建一道新题，所有 id 清空、状态置为待校对
async function openCopy(row) {
  isCopy.value = true
  try {
    const detail = await getQuestionDetail(row.id)
    form.value = {
      // id 故意不填，表示新建
      bizSection: detail.bizSection,
      categoryId: detail.categoryId,
      parentId: 0,
      type: detail.type,
      subType: detail.subType || '',
      sort: detail.sort,
      difficulty: detail.difficulty,
      content: detail.content,
      options: parseOptions(detail.options),
      answer: detail.answer || '',
      score: detail.score,
      analysis: detail.analysis || '',
      year: detail.year,
      source: detail.source || '',
      status: 0, // 复制出的新题默认"待校对"
    }
    categoryCascader.value = detail.categoryId
    // 重置子题状态
    children.value = []
    childrenLoaded.value = false
    dialogVisible.value = true
    // 复合题：加载子题列表（子题 id 清空，等父题创建后回填 parentId 再逐一创建）
    if (detail.type === 7) {
      childrenLoading.value = true
      try {
        const list = await getQuestionChildren(detail.id)
        children.value = (list || []).map((c) => ({
          // id 不填，表示新建
          parentId: 0,
          bizSection: c.bizSection ?? detail.bizSection,
          categoryId: c.categoryId ?? detail.categoryId,
          type: c.type,
          subType: c.subType || '',
          sort: c.sort,
          difficulty: c.difficulty ?? detail.difficulty,
          content: c.content || '',
          options: parseOptions(c.options),
          answer: c.answer || '',
          score: c.score,
          analysis: c.analysis || '',
          year: c.year ?? detail.year,
          source: c.source ?? detail.source,
          status: 0,
        }))
        childrenLoaded.value = true
      } catch (e) {
        children.value = []
      } finally {
        childrenLoading.value = false
      }
    }
  } catch (e) {
    // ignore
  }
}

// 子题选项支持判断（题型 1单选/2多选/3判断 才有选项）
function childHasOptions(child) {
  return [1, 2, 3].includes(child.type)
}

// 切换子题题型时调整 options
function onChildTypeChange(child) {
  if (childHasOptions(child) && child.options.length === 0) {
    child.options = ['', '', '', '']
  }
  if (!childHasOptions(child)) {
    child.options = []
  }
}

function addChildOption(child) {
  if (child.options.length < 6) child.options.push('')
}
function removeChildOption(child, idx) {
  child.options.splice(idx, 1)
}

async function save() {
  if (!form.value) return
  saveLoading.value = true
  try {
    // validate 校验失败会 reject，必须放进 try 否则会冒泡触发 dev-server overlay
    await formRef.value.validate()
    if (hasOptions.value) {
      const valid = form.value.options.filter((o) => o && o.trim())
      if (valid.length < 2) {
        ElMessage.warning('选择题至少填写 2 个选项')
        return
      }
    }
    // 校验子题：题干必填、选择题至少 2 个选项、答案必填
    if (form.value.type === 7 && children.value.length) {
      for (let i = 0; i < children.value.length; i++) {
        const c = children.value[i]
        if (!c.content || !c.content.trim()) {
          ElMessage.warning(`第 ${i + 1} 道子题题干不能为空`)
          return
        }
        if (childHasOptions(c)) {
          const valid = c.options.filter((o) => o && o.trim())
          if (valid.length < 2) {
            ElMessage.warning(`第 ${i + 1} 道子题选项至少 2 个`)
            return
          }
        }
        // 子题答案必填
        if (!c.answer || !c.answer.trim()) {
          ElMessage.warning(`第 ${i + 1} 道子题答案不能为空`)
          return
        }
      }
    }
    // 保存父题
    const payload = {
      ...form.value,
      options: hasOptions.value ? form.value.options.filter((o) => o && o.trim()) : null,
    }
    if (isCopy.value) {
      // 复制模式：用 createQuestion 创建父题，返回新题 ID
      const newId = await createQuestion(payload)
      // 批量创建子题：parentId 回填为新父题 ID
      if (form.value.type === 7 && children.value.length) {
        for (const c of children.value) {
          const childPayload = {
            ...c,
            parentId: newId,
            options: childHasOptions(c) ? c.options.filter((o) => o && o.trim()) : null,
          }
          await createQuestion(childPayload)
        }
      }
      ElMessage.success('已复制创建新试题')
    } else {
      await updateQuestion(payload)
      // 批量保存子题（按顺序执行，避免并发冲突）
      if (form.value.type === 7 && children.value.length) {
        for (const c of children.value) {
          const childPayload = {
            ...c,
            options: childHasOptions(c) ? c.options.filter((o) => o && o.trim()) : null,
          }
          await updateQuestion(childPayload)
        }
      }
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 校验失败：e 为 false 或字段错误对象，由 Element Plus 在表单项上标红，无需额外弹窗
    // API 错误：由 request.js 拦截器统一 ElMessage.error 提示
    // 不再向外抛，避免触发 dev-server overlay 显示 [object Object]
  } finally {
    saveLoading.value = false
  }
}

async function deleteOne(row) {
  try {
    // confirm 取消时会 reject 'cancel'，必须放进 try 否则会冒泡触发 dev-server overlay
    await ElMessageBox.confirm(`确认删除题目「${(row.content || '').slice(0, 30)}」？`, '删除确认', {
      type: 'warning',
    })
    await deleteQuestion(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e) {
    // 用户取消(e='cancel')：无需处理；API 错误由 request.js 拦截器提示
  }
}

async function batchDelete() {
  if (!selected.value.length) return
  try {
    // confirm 取消时会 reject 'cancel'，必须放进 try 否则会冒泡触发 dev-server overlay
    await ElMessageBox.confirm(`确认批量删除选中的 ${selected.value.length} 道题目？`, '批量删除', {
      type: 'warning',
    })
    const ids = selected.value.map((r) => r.id)
    const n = await batchDeleteQuestion(ids)
    ElMessage.success(`已删除 ${n} 条`)
    loadList()
  } catch (e) {
    // 用户取消：无需处理；API 错误由 request.js 拦截器提示
  }
}

onMounted(() => {
  loadCategories()
  loadList(1)
})
</script>

<style lang="scss" scoped>
.question-edit {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .content-cell {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    color: #4A5568;
  }

  .pagination {
    margin-top: 16px;
    text-align: right;
  }

  .options-wrap {
    width: 100%;

    .option-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .opt-label {
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        background: #2B6CB0;
        color: #fff;
        border-radius: 4px;
        font-size: 13px;
        font-weight: 600;
      }

      :deep(.el-input) {
        flex: 1;
      }
    }
  }

  .children-wrap {
    padding: 8px 16px 8px 24px;

    .children-title {
      font-weight: 600;
      color: #2B6CB0;
      margin-bottom: 8px;
    }
  }

  .child-item {
    padding: 12px 14px;
    margin-bottom: 10px;
    background: #fff;
    border: 1px solid #E2E8F0;
    border-radius: 4px;

    .child-head {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
    }

    .child-form {
      // 嵌套表单紧凑化
      :deep(.el-form-item) {
        margin-bottom: 12px;
      }
    }
  }

  .loading-hint {
    padding: 12px;
    color: #718096;
    text-align: center;
  }

  // 编辑弹窗内复合题子题列表块
  .children-block {
    margin-top: 16px;
    padding: 12px;
    background: #F7FAFC;
    border-radius: 6px;
    border: 1px solid #E2E8F0;

    .children-block-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 600;
      color: #2B6CB0;
      margin-bottom: 12px;
    }
  }
}

// 隞复合题行的展开图标，避免无子题的行出现多余箭头
:deep(.el-table) tr.no-expand .el-table__expand-icon {
  visibility: hidden;
}
</style>
