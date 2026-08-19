<template>
  <div class="question-import">
    <el-card shadow="never" class="upload-card">
      <template #header>
        <div class="card-header">
          <span>批量导入试题</span>
          <el-button type="primary" link @click="downloadTemplate">
            <el-icon><Download /></el-icon> 下载导入模板
          </el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px" label-position="right">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="业务分区" required>
              <el-select v-model="form.bizSection" style="width: 100%">
                <el-option v-for="o in bizSectionOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认分类" required>
              <el-cascader
                v-model="categoryCascader"
                :options="categoryTree"
                :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
                placeholder="Excel 未填时使用"
                clearable
                style="width: 100%"
                @change="(v) => (form.categoryId = v)"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="导入后状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option :value="0" label="待校对" />
                <el-option :value="1" label="待审核" />
                <el-option :value="2" label="直接通过" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="默认年份">
              <el-input-number v-model="form.year" :min="2000" :max="2099" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="默认来源">
              <el-input v-model="form.source" placeholder="如：2025年乙(A)试卷" maxlength="128" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Excel 文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="onFileChange"
            :on-exceed="onExceed"
            accept=".xlsx,.xls"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处，或 <em>点击选择</em>
            </div>
            <template #tip>
              <div class="upload-tip">
                仅支持 .xlsx / .xls 格式；首行表头必须为：题型 | 子题型 | 题干 | 答案 | 解析 | 难度 | 分数 | 选项A | 选项B | 选项C | 选项D | 选项E | (年份 | 来源 | 排序 可选)
                <br />复合题格式：父行填"题型=复合题 + 题干=语段"，后续子行"题型留空 + 子题型=单选题/简答题等 + 题干=小题"，直至遇到下一个非空"题型"为止。
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="uploadLoading" :disabled="!file" @click="doImport">
            开始导入
          </el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 导入结果 -->
    <el-card v-if="result" shadow="never" class="result-card">
      <template #header>
        <span>导入结果</span>
      </template>
      <el-descriptions :column="5" border>
        <el-descriptions-item label="批次ID">
          <el-tag size="small">{{ result.batchId }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="总数">{{ result.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="成功">
          <span class="text-success">{{ result.successCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="失败">
          <span class="text-danger">{{ result.failCount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="其中重复">
          <span class="text-warning">{{ result.duplicateCount || 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="result.failItems && result.failItems.length" class="fail-list">
        <h4>失败明细（共 {{ result.failCount }} 条）</h4>
        <el-table :data="result.failItems" border size="small" max-height="400">
          <el-table-column prop="row" label="Excel 行号" width="120" />
          <el-table-column prop="msg" label="失败原因" />
        </el-table>
      </div>
    </el-card>

    <!-- 批次列表 -->
    <el-card shadow="never" class="batch-card">
      <template #header>
        <span>历史批次</span>
      </template>
      <el-form :inline="true" :model="query">
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="文件名 / 批次ID" clearable @keyup.enter="loadBatches(1)" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option :value="0" label="处理中" />
            <el-option :value="1" label="成功" />
            <el-option :value="2" label="部分失败" />
            <el-option :value="3" label="失败" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="queryCategoryCascader"
            :options="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
            placeholder="全部"
            clearable
            style="width: 200px"
            @change="(v) => (query.categoryId = v)"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadBatches(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="batchList" v-loading="tableLoading" border>
        <el-table-column prop="batchId" label="批次ID" width="220">
          <template #default="{ row }">
            <el-tag size="small">{{ row.batchId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="结果" width="240">
          <template #default="{ row }">
            <span>共 {{ row.totalCount }} · </span>
            <span class="text-success">成功 {{ row.successCount }}</span>
            <span v-if="row.failCount > 0" class="text-danger"> · 失败 {{ row.failCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="batchStatusType(row.status)" size="small">
              {{ batchStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizSection" label="分区" width="80">
          <template #default="{ row }">{{ bizSectionText(row.bizSection) }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="导入时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewBatch(row)">查看明细</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadBatches()"
        @size-change="loadBatches(1)"
        class="pagination"
      />
    </el-card>

    <!-- 批次明细 -->
    <el-dialog v-model="batchDialogVisible" title="批次失败明细" width="700px">
      <div v-if="currentBatch">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="批次ID">{{ currentBatch.batchId }}</el-descriptions-item>
          <el-descriptions-item label="文件名">{{ currentBatch.fileName }}</el-descriptions-item>
          <el-descriptions-item label="总数">{{ currentBatch.totalCount }}</el-descriptions-item>
          <el-descriptions-item label="成功">{{ currentBatch.successCount }}</el-descriptions-item>
          <el-descriptions-item label="失败">{{ currentBatch.failCount }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ batchStatusText(currentBatch.status) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="parsedFailItems.length" class="fail-list">
          <h4>失败明细</h4>
          <el-table :data="parsedFailItems" border size="small" max-height="400">
            <el-table-column prop="row" label="Excel 行号" width="120" />
            <el-table-column prop="msg" label="失败原因" />
          </el-table>
        </div>
        <el-empty v-else description="无失败记录" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Download } from '@element-plus/icons-vue'
import { importQuestion, listImportBatch, getImportBatch, listCategories } from '@/api/question'

const bizSectionOptions = [
  { value: 1, label: '单招' },
  { value: 2, label: '普通' },
  { value: 3, label: '中考' },
  { value: 4, label: '高考' },
  { value: 5, label: '考研' },
]

const form = reactive({
  bizSection: 1,
  categoryId: null,
  year: new Date().getFullYear(),
  source: '',
  status: 0,
})
const categoryCascader = ref(null)
const queryCategoryCascader = ref(null)
const categoryTree = ref([])
const uploadRef = ref(null)
const file = ref(null)
const uploadLoading = ref(false)
const result = ref(null)

const query = reactive({ keyword: '', status: null, categoryId: null, page: 1, size: 10 })
const total = ref(0)
const batchList = ref([])
const tableLoading = ref(false)

const batchDialogVisible = ref(false)
const currentBatch = ref(null)
const parsedFailItems = computed(() => {
  if (!currentBatch.value || !currentBatch.value.failDetail) return []
  try {
    return JSON.parse(currentBatch.value.failDetail)
  } catch (e) {
    return []
  }
})

function bizSectionText(v) {
  return ({ 1: '单招', 2: '普通', 3: '中考', 4: '高考', 5: '考研' })[v] || '-'
}
function batchStatusText(s) {
  return ({ 0: '处理中', 1: '成功', 2: '部分失败', 3: '失败' })[s] || '未知'
}
function batchStatusType(s) {
  return ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' })[s] || 'info'
}

function onFileChange(fileObj) {
  file.value = fileObj.raw
}
function onExceed(files) {
  uploadRef.value.clearFiles()
  uploadRef.value.handleStart(files[0])
  file.value = files[0]
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

// 从已加载的分类树中查找"语文"分类，找不到返回 null
function findDefaultCategoryId() {
  const walk = (nodes) => {
    for (const n of nodes || []) {
      if ((n.name || '').trim() === '语文') return n.id
      if (n.children) {
        const r = walk(n.children)
        if (r) return r
      }
    }
    return null
  }
  return walk(categoryTree.value)
}

async function loadCategories() {
  try {
    const list = await listCategories()
    categoryTree.value = buildTree(list || [])
    // 默认选中"语文"分类（若存在）
    if (!form.categoryId) {
      const id = findDefaultCategoryId()
      if (id) {
        form.categoryId = id
        categoryCascader.value = id
      }
    }
  } catch (e) {
    // ignore
  }
}

async function doImport() {
  if (!file.value) {
    ElMessage.warning('请先选择 Excel 文件')
    return
  }
  if (!form.categoryId) {
    ElMessage.warning('请选择默认分类（Excel 未填时使用）')
    return
  }
  uploadLoading.value = true
  try {
    const res = await importQuestion(file.value, form)
    result.value = res
    const dup = res.duplicateCount || 0
    ElMessage.success(`导入完成：成功 ${res.successCount} 条，失败 ${res.failCount} 条${dup ? `（其中重复 ${dup} 条已跳过）` : ''}`)
    loadBatches(1)
  } catch (e) {
    // 由拦截器统一提示
  } finally {
    uploadLoading.value = false
  }
}

function reset() {
  Object.assign(form, {
    bizSection: 1,
    categoryId: null,
    year: new Date().getFullYear(),
    source: '',
    status: 0,
  })
  // 重置后仍保留默认"语文"分类，避免每次导入都要重新选择
  const id = findDefaultCategoryId()
  if (id) {
    form.categoryId = id
    categoryCascader.value = id
  } else {
    categoryCascader.value = null
  }
  file.value = null
  uploadRef.value?.clearFiles()
  result.value = null
}

function resetQuery() {
  query.keyword = ''
  query.status = null
  query.categoryId = null
  queryCategoryCascader.value = null
  loadBatches(1)
}

async function loadBatches(page) {
  if (page) query.page = page
  tableLoading.value = true
  try {
    const res = await listImportBatch(query)
    batchList.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    // ignore
  } finally {
    tableLoading.value = false
  }
}

async function viewBatch(row) {
  try {
    const detail = await getImportBatch(row.batchId)
    currentBatch.value = detail
    batchDialogVisible.value = true
  } catch (e) {
    // ignore
  }
}

function downloadTemplate() {
  // 模板通过前端生成，参照 originalRequirement/2025年乙(A)试卷_选项单列格式.xlsx
  // 列顺序：题型 | 子题型 | 题干 | 答案 | 解析 | 难度 | 分数 | 选项A | 选项B | 选项C | 选项D | 选项E
  // 复合题：父行 题型=复合题，题干=语段；后续子行 题型留空，子题型=单选题/简答题等
  const headers = [
    '题型', '子题型', '题干', '答案', '解析', '难度', '分数',
    '选项A', '选项B', '选项C', '选项D', '选项E',
  ]
  const sample = [
    // 1) 独立单选题
    ['单选题', '', '下列各组词语中，加点字的注音全都正确的一组是（）', 'A', '考查字音字形辨析', '中等', 3,
      '淘冶 蜷缩（quán）', '训戒 收敛（liǎn）', '青睐 铁锹（qiāo）', '熨帖 芭蕉（bā）', ''],
    // 2) 复合题父行：题型=复合题，题干=语段/材料，答案/选项留空
    ['复合题', '', '5.阅读语段，按要求完成下面的题目。(7分)\n2025年春晚，魔术《画蛇添福》无疑是最受瞩目的节目之一……', '', '', '', 7,
      '', '', '', '', ''],
    // 3) 复合题子行：题型留空，子题型=单选题（作为子题题型），题干=小题题干
    ['', '单选题', '(1) 依次填入文中括号内的词语，最恰当的一组是（）', 'A', '考查词语辨析', '中等', 2,
      '精湛巧妙', '精湛灵敏', '精细敏捷', '精深敏捷', ''],
    // 4) 复合题子行：子题型=简答题
    ['', '简答题', '(3) 请在文中横线处补写恰当的语句，使整段文字语意连贯，逻辑严密，不超过20个字。', '参考答案：此处应填…', '考查语言运用', '中等', 3,
      '', '', '', '', ''],
    // 5) 独立填空题（结束上一个复合题上下文：题型非空）
    ['填空题', '', '1.几处早莺争暖树，____。', '谁家新燕啄春泥', '考查古诗文默写（白居易《钱塘湖春行》）', '中等', 2,
      '', '', '', '', ''],
  ]
  const csv = [headers, ...sample]
    .map((row) => row.map((c) => `"${String(c ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  // 加 BOM 让 Excel 正确识别 UTF-8
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '题库批量导入模板.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('模板已下载（请使用 Excel 打开并按需另存为 .xlsx 后使用）')
}

onMounted(() => {
  loadCategories()
  loadBatches(1)
})
</script>

<style lang="scss" scoped>
.question-import {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .upload-tip {
    color: #909399;
    font-size: 12px;
    line-height: 1.6;
    margin-top: 4px;
  }

  .result-card,
  .batch-card {
    margin-top: 20px;
  }

  .fail-list {
    margin-top: 16px;

    h4 {
      margin: 0 0 8px;
      color: #f56c6c;
    }
  }

  .text-success {
    color: #67c23a;
    font-weight: 600;
  }

  .text-danger {
    color: #f56c6c;
    font-weight: 600;
  }

  .text-warning {
    color: #e6a23c;
    font-weight: 600;
  }

  .pagination {
    margin-top: 16px;
    text-align: right;
  }
}
</style>
