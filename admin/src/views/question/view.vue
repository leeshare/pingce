<template>
  <div class="question-view">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>试题查看</span>
          <el-tag type="info" size="small">只读模式</el-tag>
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
            style="width: 120px"
            @change="onCategoryChange"
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
        <el-table-column label="题型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
            <el-tag v-if="row.type === 7" type="danger" size="small" effect="plain" class="composite-tag">复合</el-tag>
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
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">查看详情</el-button>
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

    <!-- 只读详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="试题详情" width="820px" :close-on-click-modal="true">
      <el-descriptions v-if="form" :column="3" border size="small" class="meta-desc">
        <el-descriptions-item label="ID">{{ form.id }}</el-descriptions-item>
        <el-descriptions-item label="业务分区">{{ bizSectionText(form.bizSection) }}</el-descriptions-item>
        <el-descriptions-item label="题型">{{ typeText(form.type) }}</el-descriptions-item>
        <el-descriptions-item label="难度">{{ difficultyText(form.difficulty) }}</el-descriptions-item>
        <el-descriptions-item label="分值">{{ form.score || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(form.status)" size="small">{{ statusText(form.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ form.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年份">{{ form.year || '-' }}</el-descriptions-item>
        <el-descriptions-item label="子题型">{{ form.subType || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-form v-if="form" label-width="80px" class="detail-form">
        <el-form-item label="题干">
          <div class="readonly-text">{{ form.content }}</div>
        </el-form-item>

        <el-form-item v-if="form.options" label="选项">
          <div class="options-wrap">
            <div v-for="(opt, idx) in parseOptions(form.options)" :key="idx" class="option-row">
              <span class="opt-label">{{ optionLetters[idx] }}</span>
              <span class="opt-text">{{ opt }}</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="正确答案">
          <div class="readonly-text">{{ form.answer || '-' }}</div>
        </el-form-item>

        <el-form-item label="答案解析">
          <div class="readonly-text pre-wrap">{{ form.analysis || '-' }}</div>
        </el-form-item>
      </el-form>

      <!-- 复合题子题 -->
      <div v-if="form && form.type === 7" class="children-block">
        <div class="children-block-title">
          <el-icon><Reading /></el-icon>
          <span>子题列表</span>
          <el-tag v-if="childrenLoaded" size="small" type="info">{{ children.length }} 道</el-tag>
        </div>
        <div v-if="childrenLoading" class="loading-hint">加载中...</div>
        <div v-else-if="children.length">
          <div v-for="(child, idx) in children" :key="child.id" class="child-item">
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
        <el-empty v-else description="暂无子题" :image-size="60" />
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { listForEdit, getQuestionDetail, getQuestionChildren, listCategories } from '@/api/question'

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
const categoryTree = ref([])
const categoryCascader = ref(null)

const dialogVisible = ref(false)
const form = ref(null)
const children = ref([])
const childrenLoading = ref(false)
const childrenLoaded = ref(false)

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
function bizSectionText(v) {
  return ({ 1: '单招', 2: '普通', 3: '中考', 4: '高考', 5: '考研' })[v] || '-'
}

function parseOptions(opts) {
  if (!opts) return []
  try {
    const arr = JSON.parse(opts)
    return Array.isArray(arr) ? arr : []
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

function onCategoryChange(val) {
  query.categoryId = val
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
  categoryCascader.value = null
  loadList(1)
}

async function openDetail(row) {
  try {
    const detail = await getQuestionDetail(row.id)
    form.value = detail
    children.value = []
    childrenLoaded.value = false
    dialogVisible.value = true
    // 复合题：加载子题
    if (detail.type === 7) {
      childrenLoading.value = true
      try {
        children.value = await getQuestionChildren(detail.id)
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

onMounted(() => {
  loadCategories()
  loadList(1)
})
</script>

<style lang="scss" scoped>
.question-view {
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

  .composite-tag {
    margin-left: 6px;
  }

  .pagination {
    margin-top: 16px;
    text-align: right;
  }

  .readonly-text {
    color: #2D3748;
    line-height: 1.7;

    &.pre-wrap {
      white-space: pre-wrap;
      word-break: break-word;
    }
  }

  .detail-form {
    margin-top: 12px;
  }

  .meta-desc {
    margin-bottom: 4px;
  }

  .options-wrap {
    width: 100%;

    .option-row {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 8px;

      .opt-label {
        flex: 0 0 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        background: #2B6CB0;
        color: #fff;
        border-radius: 4px;
        font-size: 13px;
        font-weight: 600;
      }

      .opt-text {
        flex: 1;
        line-height: 24px;
        color: #2D3748;
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

  .child-item {
    padding: 10px 12px;
    margin-bottom: 8px;
    background: #fff;
    border: 1px solid #E2E8F0;
    border-radius: 4px;

    .child-head {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 6px;

      .child-score {
        color: #C05621;
        font-size: 13px;
      }

      .child-sub {
        color: #718096;
        font-size: 12px;
      }
    }

    .child-content {
      color: #2D3748;
      line-height: 1.6;
      margin-bottom: 6px;
      white-space: pre-wrap;
      word-break: break-word;
    }

    .child-options {
      margin: 6px 0 6px 8px;
    }

    .child-opt {
      display: flex;
      align-items: flex-start;
      gap: 6px;
      font-size: 13px;
      color: #4A5568;
      margin-bottom: 4px;

      .opt-letter {
        flex: 0 0 18px;
        font-weight: 600;
        color: #2B6CB0;
      }
    }

    .child-answer,
    .child-analysis {
      font-size: 13px;
      color: #4A5568;
      margin-top: 4px;

      .label {
        color: #718096;
      }
    }
  }

  .loading-hint {
    padding: 12px;
    color: #718096;
    text-align: center;
  }
}

// 隞复合题行的展开图标，避免无子题的行出现多余箭头
:deep(.el-table) tr.no-expand .el-table__expand-icon {
  visibility: hidden;
}
</style>
