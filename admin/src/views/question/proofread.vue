<template>
  <div class="question-proofread">
    <!-- 统计 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stat.pendingProofread || 0 }}</div>
            <div class="stat-label">待校对</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stat.pendingReview || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value text-success">{{ stat.approved || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value text-danger">{{ stat.rejected || 0 }}</div>
            <div class="stat-label">已驳回</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="list-card">
      <template #header>
        <span>待校对题列表</span>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option :value="0" label="待校对" />
            <el-option :value="1" label="待审核" />
            <el-option :value="2" label="已通过" />
            <el-option :value="3" label="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="题干关键字" clearable @keyup.enter="loadList(1)" />
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="query.difficulty" placeholder="全部" clearable style="width: 120px">
            <el-option :value="1" label="简单" />
            <el-option :value="2" label="中等" />
            <el-option :value="3" label="困难" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次ID">
          <el-input v-model="query.importBatchId" placeholder="可选" clearable style="width: 220px" />
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
                  <div v-if="parseOptions(child).length" class="child-options">
                    <div v-for="(opt, oi) in parseOptions(child)" :key="oi" class="child-opt">
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
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题干" min-width="320" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="content-cell">{{ row.content }}</span>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="80">
          <template #default="{ row }">{{ difficultyText(row.difficulty) }}</template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="180" show-overflow-tooltip />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="录入时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 2" type="primary" link size="small" @click="openProofread(row)">校对</el-button>
            <span v-else class="text-muted">已通过</span>
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

    <!-- 校对弹窗 -->
    <el-dialog v-model="dialogVisible" title="试题校对" width="780px" :close-on-click-modal="false">
      <el-form v-if="form" :model="form" label-width="80px">
        <el-descriptions :column="3" border size="small" class="meta-desc">
          <el-descriptions-item label="ID">{{ form.id }}</el-descriptions-item>
          <el-descriptions-item label="题型">{{ typeText(form.type) }}</el-descriptions-item>
          <el-descriptions-item label="难度">
            <el-select v-model="form.difficulty" size="small" style="width: 100px">
              <el-option :value="1" label="简单" />
              <el-option :value="2" label="中等" />
              <el-option :value="3" label="困难" />
            </el-select>
          </el-descriptions-item>
          <el-descriptions-item label="分值">
            <el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" :precision="1" size="small" style="width: 120px" />
          </el-descriptions-item>
          <el-descriptions-item label="来源">{{ form.source || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年份">{{ form.year || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-form-item label="题干">
          <el-input v-model="form.content" type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" maxlength="5000" show-word-limit />
        </el-form-item>

        <el-form-item v-if="form.options && form.options.length" label="选项">
          <div class="options-wrap">
            <div v-for="(opt, idx) in form.options" :key="idx" class="option-row">
              <span class="opt-label">{{ optionLetters[idx] }}</span>
              <el-input v-model="form.options[idx]" />
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="form.type !== 7" label="答案">
          <el-input v-model="form.answer" />
        </el-form-item>

        <el-form-item v-if="form.type !== 7" label="解析">
          <el-input v-model="form.analysis" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" />
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
        <el-button type="warning" :loading="saveLoading" @click="save">保存校对</el-button>
        <el-button type="success" :loading="submitLoading" @click="submitForReview">保存并提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import { listForProofread, proofreadSave, updateQuestion, getQuestionStat, getQuestionChildren } from '@/api/question'

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

const stat = ref({})
const query = reactive({
  keyword: '',
  type: null,
  difficulty: null,
  importBatchId: '',
  status: 0, // 默认查"待校对"（status=0），与统计卡片"待校对"对齐
  page: 1,
  size: 10,
})
const list = ref([])
const total = ref(0)
const tableLoading = ref(false)

const dialogVisible = ref(false)
const form = ref(null)
const saveLoading = ref(false)
const submitLoading = ref(false)
// 复合题子题（校对弹窗内可编辑）
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

function parseOptions(question) {
  if (!question.options) return []
  // 兼容已解析为数组的情况
  if (Array.isArray(question.options)) return question.options
  try {
    return JSON.parse(question.options) || []
  } catch (e) {
    return []
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

async function loadStat() {
  try {
    stat.value = await getQuestionStat()
  } catch (e) {
    // ignore
  }
}

async function loadList(page) {
  if (page) query.page = page
  tableLoading.value = true
  try {
    const res = await listForProofread(query)
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
  query.type = null
  query.difficulty = null
  query.importBatchId = ''
  query.status = 0
  loadList(1)
}

async function openProofread(row) {
  // 深拷贝避免直接修改列表数据
  form.value = {
    id: row.id,
    type: row.type,
    difficulty: row.difficulty,
    score: row.score,
    content: row.content,
    options: parseOptions(row),
    answer: row.answer,
    analysis: row.analysis,
    source: row.source,
    year: row.year,
  }
  // 重置子题状态
  children.value = []
  childrenLoaded.value = false
  dialogVisible.value = true
  // 复合题：加载子题列表，并将 options 解析为数组，便于双向绑定
  if (row.type === 7) {
    childrenLoading.value = true
    try {
      const list = await getQuestionChildren(row.id)
      children.value = (list || []).map((c) => ({
        id: c.id,
        parentId: row.id,
        bizSection: c.bizSection ?? row.bizSection,
        categoryId: c.categoryId ?? row.categoryId,
        type: c.type,
        subType: c.subType || '',
        sort: c.sort,
        difficulty: c.difficulty ?? row.difficulty,
        content: c.content || '',
        options: parseOptions(c) || [],
        answer: c.answer || '',
        score: c.score,
        analysis: c.analysis || '',
        year: c.year ?? row.year,
        source: c.source ?? row.source,
        status: c.status ?? row.status,
      }))
      childrenLoaded.value = true
    } catch (e) {
      children.value = []
    } finally {
      childrenLoading.value = false
    }
  }
}

async function save() {
  if (!form.value) return
  saveLoading.value = true
  try {
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
        if (!c.answer || !c.answer.trim()) {
          ElMessage.warning(`第 ${i + 1} 道子题答案不能为空`)
          return
        }
      }
    }
    await proofreadSave(buildPayload())
    // 复合题：批量保存子题
    if (form.value.type === 7 && children.value.length) {
      for (const c of children.value) {
        await updateQuestion(buildChildPayload(c))
      }
    }
    ElMessage.success('校对已保存')
    dialogVisible.value = false
    loadList()
    loadStat()
  } catch (e) {
    // API 错误由 request.js 拦截器统一提示
  } finally {
    saveLoading.value = false
  }
}

async function submitForReview() {
  if (!form.value) return
  submitLoading.value = true
  try {
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
        if (!c.answer || !c.answer.trim()) {
          ElMessage.warning(`第 ${i + 1} 道子题答案不能为空`)
          return
        }
      }
    }
    const payload = { ...buildPayload(), status: 1 }
    await updateQuestion(payload)
    // 复合题：批量保存子题
    if (form.value.type === 7 && children.value.length) {
      for (const c of children.value) {
        await updateQuestion(buildChildPayload(c))
      }
    }
    ElMessage.success('已保存并提交审核')
    dialogVisible.value = false
    loadList()
    loadStat()
  } catch (e) {
    // API 错误由 request.js 拦截器统一提示
  } finally {
    submitLoading.value = false
  }
}

function buildChildPayload(c) {
  return {
    id: c.id,
    parentId: c.parentId,
    bizSection: c.bizSection,
    categoryId: c.categoryId,
    type: c.type,
    subType: c.subType,
    sort: c.sort,
    difficulty: c.difficulty,
    content: c.content,
    options: childHasOptions(c) ? c.options.filter((o) => o && o.trim()) : null,
    answer: c.answer,
    score: c.score,
    analysis: c.analysis,
    year: c.year,
    source: c.source,
    status: c.status,
  }
}

function buildPayload() {
  const f = form.value
  return {
    id: f.id,
    content: f.content,
    options: f.options,
    answer: f.answer,
    analysis: f.analysis,
    score: f.score,
    difficulty: f.difficulty,
  }
}

onMounted(() => {
  loadStat()
  loadList(1)
})
</script>

<style lang="scss" scoped>
.question-proofread {
  .stat-row {
    margin-bottom: 20px;

    .stat-item {
      text-align: center;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #1A202C;
    }

    .stat-label {
      color: #718096;
      font-size: 13px;
      margin-top: 4px;
    }

    .text-success {
      color: #67c23a;
    }

    .text-danger {
      color: #f56c6c;
    }
  }

  .list-card {
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

    .text-muted {
      color: #a0aec0;
      font-size: 13px;
    }
  }

  .meta-desc {
    margin-bottom: 16px;
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

  // 表格内展开的子题显示
  .children-wrap {
    padding: 8px 16px 8px 24px;

    .children-title {
      font-weight: 600;
      color: #2B6CB0;
      margin-bottom: 8px;
    }

    .child-content {
      color: #2D3748;
      margin: 6px 0;
    }

    .child-options {
      margin: 6px 0;
    }

    .child-opt {
      display: flex;
      gap: 6px;
      align-items: flex-start;
      margin-bottom: 4px;
      color: #4A5568;
    }

    .opt-letter {
      width: 20px;
      height: 20px;
      line-height: 20px;
      text-align: center;
      background: #EDF2F7;
      color: #2B6CB0;
      border-radius: 3px;
      font-size: 12px;
      font-weight: 600;
      flex-shrink: 0;
    }

    .child-answer,
    .child-analysis {
      margin-top: 4px;
      color: #4A5568;
      font-size: 13px;

      .label {
        color: #718096;
      }
    }
  }

  // 校对弹窗内复合题子题列表块
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
}

// 非复合题行隐藏展开图标
:deep(.el-table) tr.no-expand .el-table__expand-icon {
  visibility: hidden;
}
</style>
