<template>
  <div class="paper-publish">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>试卷发布</span>
          <div class="header-stats">
            <el-tag type="info" size="large">草稿 {{ stat.draft }}</el-tag>
            <el-tag type="success" size="large">已发布 {{ stat.published }}</el-tag>
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
        <el-table-column label="题数" width="70">
          <template #default="{ row }">{{ countIds(row.questionIds) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 1"
              type="success"
              link
              size="small"
              @click="toggle(row, 1)"
            >发布</el-button>
            <el-button
              v-else
              type="warning"
              link
              size="small"
              @click="toggle(row, 0)"
            >下架</el-button>
            <el-button type="primary" link size="small" @click="openDetail(row)">查看</el-button>
            <el-button type="info" link size="small" @click="openPreview(row)">预览</el-button>
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

    <!-- 试卷详情弹窗 -->
    <el-dialog v-model="detailVisible" title="试卷详情" width="720px">
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="试卷名称" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="业务分区">{{ bizSectionText(detail.bizSection) }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ categoryMap[detail.categoryId] || detail.categoryId }}</el-descriptions-item>
        <el-descriptions-item label="年份">{{ detail.year || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ detail.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="考试时长">{{ detail.duration }} 分钟</el-descriptions-item>
        <el-descriptions-item label="总分 / 及格分">{{ detail.totalScore }} / {{ detail.passScore }}</el-descriptions-item>
        <el-descriptions-item label="题目数量">{{ countIds(detail.questionIds) }} 题</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status === 1 ? 'success' : 'info'" size="small">
            {{ detail.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="试卷说明" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="题目ID" :span="2">{{ detail.questionIds || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 试卷预览弹窗 -->
    <el-dialog
      v-model="previewVisible"
      :title="`试卷预览${previewPaper ? ' - ' + previewPaper.title : ''}`"
      width="900px"
      top="5vh"
    >
      <div v-loading="previewLoading" class="preview-body">
        <div v-if="previewPaper" class="preview-meta">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="试卷名称" :span="3">{{ previewPaper.title }}</el-descriptions-item>
            <el-descriptions-item label="分区">{{ bizSectionText(previewPaper.bizSection) }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ categoryMap[previewPaper.categoryId] || previewPaper.categoryId }}</el-descriptions-item>
            <el-descriptions-item label="年份">{{ previewPaper.year || '-' }}</el-descriptions-item>
            <el-descriptions-item label="来源" :span="3">{{ previewPaper.source || '-' }}</el-descriptions-item>
            <el-descriptions-item label="时长">{{ previewPaper.duration }} 分钟</el-descriptions-item>
            <el-descriptions-item label="总分/及格">{{ previewPaper.totalScore }} / {{ previewPaper.passScore }}</el-descriptions-item>
            <el-descriptions-item label="题数">{{ previewQuestions.length }} 题</el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-if="!previewLoading && previewQuestions.length === 0" class="preview-empty">
          该试卷暂无题目
        </div>
        <div v-else class="question-list">
          <div
            v-for="q in previewQuestions"
            :key="q.id"
            :class="['question-item', q.parentId && q.parentId !== 0 ? 'is-child' : '']"
          >
            <div class="q-header">
              <span class="q-no">{{ q._no }}</span>
              <el-tag size="small" :type="qTypeTag(q.type)">{{ qTypeText(q.type) }}</el-tag>
              <el-tag v-if="q.difficulty" size="small" type="info">{{ qDifficultyText(q.difficulty) }}</el-tag>
              <span v-if="q.score != null" class="q-score">{{ q.score }} 分</span>
            </div>
            <div class="q-content">{{ q.content }}</div>
            <div v-if="parseOptions(q.options).length" class="q-options">
              <div v-for="(opt, i) in parseOptions(q.options)" :key="i" class="q-opt">{{ opt }}</div>
            </div>
            <div v-if="q.answer" class="q-answer"><span class="label">答案：</span>{{ formatAnswer(q) }}</div>
            <div v-if="q.analysis" class="q-analysis"><span class="label">解析：</span>{{ q.analysis }}</div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPapers, getPaperDetail, previewPaper as fetchPaperPreview, updatePaperStatus } from '@/api/paper'
import { listCategories } from '@/api/question'

const bizSectionOptions = [
  { value: 1, label: '单招' },
  { value: 2, label: '普通' },
  { value: 3, label: '中考' },
  { value: 4, label: '高考' },
  { value: 5, label: '考研' },
]

function bizSectionText(v) {
  return ({ 1: '单招', 2: '普通', 3: '中考', 4: '高考', 5: '考研' })[v] || '-'
}
function countIds(ids) {
  if (!ids) return 0
  return ids.split(',').filter((s) => s && s.trim()).length
}

// 题型/难度文本与样式
const TYPE_TEXT = { 1: '单选', 2: '多选', 3: '判断', 4: '填空', 5: '简答', 6: '计算', 7: '复合' }
const TYPE_TAG = { 1: 'success', 2: 'success', 3: 'success', 4: 'warning', 5: 'danger', 6: 'danger', 7: 'primary' }
const DIFFICULTY_TEXT = { 1: '简单', 2: '中等', 3: '困难' }
function qTypeText(t) {
  return TYPE_TEXT[t] || '未知'
}
function qTypeTag(t) {
  return TYPE_TAG[t] || ''
}
function qDifficultyText(d) {
  return DIFFICULTY_TEXT[d] || ''
}
// 解析选项 JSON 字符串 → 数组
function parseOptions(opts) {
  if (!opts) return []
  try {
    const arr = JSON.parse(opts)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
}
// 格式化答案：填空题 answer 是 JSON 数组，转成 "1、xxx 2、xxx"
function formatAnswer(q) {
  if (q.answer == null || q.answer === '') return ''
  if (q.type === 4) {
    try {
      const arr = JSON.parse(q.answer)
      if (Array.isArray(arr)) {
        return arr.map((a, i) => `${i + 1}、${a}`).join('  ')
      }
    } catch (e) {
      // 非数组，按原值返回
    }
  }
  return q.answer
}

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

const stat = computed(() => {
  let draft = 0
  let published = 0
  list.value.forEach((r) => {
    if (r.status === 1) published++
    else draft++
  })
  return { draft, published }
})

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
    // ignore
  } finally {
    tableLoading.value = false
  }
}

// 分类树
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

// 发布/下架
async function toggle(row, status) {
  const action = status === 1 ? '发布' : '下架'
  try {
    await ElMessageBox.confirm(`确认${action}试卷「${row.title}」？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await updatePaperStatus(row.id, status)
    ElMessage.success(`已${action}`)
    loadList()
  } catch (e) {
    // ignore
  }
}

// 详情
const detailVisible = ref(false)
const detail = ref(null)
async function openDetail(row) {
  try {
    detail.value = await getPaperDetail(row.id)
    detailVisible.value = true
  } catch (e) {
    // ignore
  }
}

// 预览：整套试卷按题目顺序展示
const previewVisible = ref(false)
const previewLoading = ref(false)
const previewPaper = ref(null)
const previewQuestions = ref([])
async function openPreview(row) {
  previewLoading.value = true
  previewVisible.value = true
  previewPaper.value = null
  previewQuestions.value = []
  try {
    const data = await fetchPaperPreview(row.id)
    previewPaper.value = data.paper
    // 计算题号：大题(parentId=0)用 1,2,3...；子题用 (1)(2)... 相对父题
    let mainNo = 0
    let subNo = 0
    previewQuestions.value = (data.questions || []).map((q) => {
      const isChild = q.parentId && q.parentId !== 0
      let no
      if (isChild) {
        subNo++
        no = `(${subNo})`
      } else {
        mainNo++
        subNo = 0
        no = `${mainNo}.`
      }
      return { ...q, _no: no }
    })
  } catch (e) {
    // ignore
  } finally {
    previewLoading.value = false
  }
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
  .header-stats {
    display: flex;
    gap: 10px;
  }
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

// 试卷预览
.preview-body {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 6px;
}
.preview-meta {
  margin-bottom: 16px;
}
.preview-empty {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}
.question-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.question-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px;
  background: #fafafa;
  &.is-child {
    margin-left: 32px;
    background: #fff;
    border-left: 3px solid #409eff;
  }
}
.q-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  .q-no {
    font-weight: 600;
    color: #303133;
    margin-right: 4px;
  }
  .q-score {
    margin-left: auto;
    color: #f56c6c;
    font-size: 13px;
  }
}
.q-content {
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 8px;
}
.q-options {
  margin: 6px 0 8px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  .q-opt {
    color: #606266;
    line-height: 1.6;
  }
}
.q-answer,
.q-analysis {
  font-size: 13px;
  line-height: 1.7;
  padding: 6px 10px;
  border-radius: 4px;
  margin-top: 6px;
  word-break: break-word;
  .label {
    color: #909399;
    font-weight: 600;
  }
}
.q-answer {
  background: #f0f9eb;
  color: #529b2e;
}
.q-analysis {
  background: #fdf6ec;
  color: #b88230;
}
</style>
