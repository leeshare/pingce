<template>
  <div class="question-review">
    <!-- 统计 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover" @click="filterByStatus(1)">
          <div class="stat-item clickable">
            <div class="stat-value text-warning">{{ stat.pendingReview || 0 }}</div>
            <div class="stat-label">待审核</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" @click="filterByStatus(2)">
          <div class="stat-item clickable">
            <div class="stat-value text-success">{{ stat.approved || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" @click="filterByStatus(3)">
          <div class="stat-item clickable">
            <div class="stat-value text-danger">{{ stat.rejected || 0 }}</div>
            <div class="stat-label">已驳回</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ stat.total || 0 }}</div>
            <div class="stat-label">题目总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>试题审核</span>
          <div>
            <el-button type="success" :disabled="!selected.length" @click="openReview(2)">
              <el-icon><Check /></el-icon> 批量通过
            </el-button>
            <el-button type="danger" :disabled="!selected.length" @click="openReview(3)">
              <el-icon><Close /></el-icon> 批量驳回
            </el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px" @change="loadList(1)">
            <el-option :value="0" label="待校对" />
            <el-option :value="1" label="待审核" />
            <el-option :value="2" label="已通过" />
            <el-option :value="3" label="已驳回" />
          </el-select>
        </el-form-item>
        <el-form-item label="题型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
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
        <el-table-column type="selection" width="50" :selectable="(row) => row.status === 1" />
        <el-table-column label="题型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
            <el-tag v-if="row.type === 7" type="danger" size="small" effect="plain" class="composite-tag">复合</el-tag>
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
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="160" show-overflow-tooltip />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="createdAt" label="录入时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.status === 1" type="success" link size="small" @click="reviewOne(row, 2)">通过</el-button>
            <el-button v-if="row.status === 1" type="danger" link size="small" @click="reviewOne(row, 3)">驳回</el-button>
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

    <!-- 审核弹窗 -->
    <el-dialog v-model="reviewDialogVisible" :title="reviewTitle" width="520px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核题目">
          <el-tag size="small">{{ selectedIds.length }} 道</el-tag>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewForm.status">
            <el-radio :value="2">通过</el-radio>
            <el-radio :value="3">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="reviewForm.remark" type="textarea" :rows="4" :placeholder="remarkPlaceholder" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="confirmReview">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="试题详情" width="780px">
      <div v-if="detail" class="detail-wrap">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="题型">{{ typeText(detail.type) }}</el-descriptions-item>
          <el-descriptions-item label="难度">{{ difficultyText(detail.difficulty) }}</el-descriptions-item>
          <el-descriptions-item label="分值">{{ detail.score || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ detail.source || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年份">{{ detail.year || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="子题型">{{ detail.subType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="排序">{{ detail.sort }}</el-descriptions-item>
        </el-descriptions>

        <div class="section">
          <div class="section-label">题干</div>
          <div class="section-content pre-wrap">{{ detail.content }}</div>
        </div>

        <div v-if="parsedOptions.length" class="section">
          <div class="section-label">选项</div>
          <div class="section-content">
            <div v-for="(opt, idx) in parsedOptions" :key="idx" class="option-line">
              <span class="opt-label">{{ optionLetters[idx] }}</span>
              <span>{{ opt }}</span>
            </div>
          </div>
        </div>

        <div class="section">
          <div class="section-label">正确答案</div>
          <div class="section-content answer">{{ detail.answer || '-' }}</div>
        </div>

        <div v-if="detail.analysis" class="section">
          <div class="section-label">答案解析</div>
          <div class="section-content pre-wrap">{{ detail.analysis }}</div>
        </div>

        <div v-if="detail.reviewRemark" class="section">
          <div class="section-label">审核备注</div>
          <div class="section-content pre-wrap">{{ detail.reviewRemark }}</div>
        </div>
      </div>

      <!-- 复合题子题列表（只读展示） -->
      <div v-if="detail && detail.type === 7" class="children-block">
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
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, Reading } from '@element-plus/icons-vue'
import { listForEdit, getQuestionDetail, getQuestionStat, reviewQuestion, getQuestionChildren } from '@/api/question'

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
  status: 1,
  type: null,
  keyword: '',
  page: 1,
  size: 10,
})
const list = ref([])
const total = ref(0)
const tableLoading = ref(false)
const selected = ref([])

const reviewDialogVisible = ref(false)
const reviewForm = reactive({ status: 2, remark: '' })
const reviewLoading = ref(false)

const detailDialogVisible = ref(false)
const detail = ref(null)
// 详情弹窗的复合题子题（只读展示）
const children = ref([])
const childrenLoading = ref(false)
const childrenLoaded = ref(false)

const selectedIds = computed(() => selected.value.map((r) => r.id))
const reviewTitle = computed(() => (reviewForm.status === 2 ? '批量通过审核' : '批量驳回审核'))
const remarkPlaceholder = computed(() =>
  reviewForm.status === 2 ? '通过备注（可选）' : '请填写驳回原因（建议必填）',
)

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

const parsedOptions = computed(() => {
  if (!detail.value || !detail.value.options) return []
  try {
    return JSON.parse(detail.value.options) || []
  } catch (e) {
    return []
  }
})

function parseOptions(opts) {
  if (!opts) return []
  try {
    const arr = JSON.parse(opts)
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    return []
  }
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
  query.status = 1
  query.type = null
  query.keyword = ''
  loadList(1)
}

function filterByStatus(status) {
  query.status = status
  loadList(1)
}

function onSelectionChange(rows) {
  selected.value = rows
}

function openReview(status) {
  reviewForm.status = status
  reviewForm.remark = ''
  reviewDialogVisible.value = true
}

async function reviewOne(row, status) {
  selected.value = [row]
  openReview(status)
}

async function confirmReview() {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择待审核题目')
    return
  }
  if (reviewForm.status === 3 && !reviewForm.remark) {
    ElMessage.warning('驳回时请填写原因')
    return
  }
  reviewLoading.value = true
  try {
    const n = await reviewQuestion({
      ids: selectedIds.value,
      status: reviewForm.status,
      remark: reviewForm.remark,
    })
    ElMessage.success(`已${reviewForm.status === 2 ? '通过' : '驳回'} ${n} 道题目`)
    reviewDialogVisible.value = false
    selected.value = []
    loadList()
    loadStat()
  } catch (e) {
    // ignore
  } finally {
    reviewLoading.value = false
  }
}

async function viewDetail(row) {
  try {
    detail.value = await getQuestionDetail(row.id)
    children.value = []
    childrenLoaded.value = false
    detailDialogVisible.value = true
    // 复合题：加载子题（只读展示）
    if (detail.value.type === 7) {
      childrenLoading.value = true
      try {
        children.value = await getQuestionChildren(detail.value.id)
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
  loadStat()
  loadList(1)
})
</script>

<style lang="scss" scoped>
.question-review {
  .stat-row {
    margin-bottom: 20px;

    .stat-item {
      text-align: center;

      &.clickable {
        cursor: pointer;
      }
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

    .text-warning {
      color: #e6a23c;
    }

    .text-success {
      color: #67c23a;
    }

    .text-danger {
      color: #f56c6c;
    }
  }

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

  .detail-wrap {
    .section {
      margin-top: 16px;

      .section-label {
        font-weight: 600;
        color: #2C5282;
        margin-bottom: 6px;
      }

      .section-content {
        background: #F7FAFC;
        padding: 12px;
        border-radius: 4px;
        line-height: 1.6;
        color: #2D3748;

        &.pre-wrap {
          white-space: pre-wrap;
          word-break: break-word;
        }

        &.answer {
          color: #38A169;
          font-weight: 600;
        }
      }

      .option-line {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;

        .opt-label {
          display: inline-block;
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
  }

  // 详情弹窗内复合题子题列表块
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

// 非复合题行隐藏展开图标
:deep(.el-table) tr.no-expand .el-table__expand-icon {
  visibility: hidden;
}
</style>
