<template>
  <div class="question-entry">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>试题录入</span>
          <el-tag size="small" type="info">录入后题目默认进入"待审核"，可在试题审核模块中审核发布</el-tag>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        label-position="right"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="业务分区" prop="bizSection">
              <el-select v-model="form.bizSection" placeholder="请选择" style="width: 100%">
                <el-option v-for="o in bizSectionOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="categoryId">
              <el-cascader
                v-model="categoryCascader"
                :options="categoryTree"
                :props="{ value: 'id', label: 'name', children: 'children', checkStrictly: true, emitPath: false }"
                placeholder="请选择分类"
                clearable
                style="width: 100%"
                @change="onCategoryChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="题型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%" @change="onTypeChange">
                <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="难度">
              <el-select v-model="form.difficulty" placeholder="请选择" style="width: 100%">
                <el-option v-for="o in difficultyOptions" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分值">
              <el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序号">
              <el-input-number v-model="form.sort" :min="0" :max="9999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="真题年份">
              <el-input-number v-model="form.year" :min="2000" :max="2099" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="来源">
              <el-input v-model="form.source" placeholder="如：2025年乙(A)试卷" maxlength="128" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="子题型">
          <el-input v-model="form.subType" placeholder="如：阅读理解-推理判断（可选）" maxlength="32" style="width: 360px" />
        </el-form-item>

        <el-form-item label="题干" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 12 }"
            placeholder="请输入题干内容"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>

        <!-- 选项区：仅选择题展示 -->
        <el-form-item v-if="hasOptions" label="选项">
          <div class="options-wrap">
            <div v-for="(opt, idx) in form.options" :key="idx" class="option-row">
              <span class="opt-label">{{ optionLetters[idx] }}</span>
              <el-input v-model="form.options[idx]" placeholder="请输入选项内容" maxlength="500" />
              <el-button v-if="form.options.length > 2" type="danger" link @click="removeOption(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button v-if="form.options.length < 6" type="primary" link @click="addOption">
              <el-icon><Plus /></el-icon> 添加选项
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="正确答案" prop="answer">
          <el-input
            v-model="form.answer"
            :placeholder="answerPlaceholder"
            maxlength="2000"
          />
          <div class="form-tip">{{ answerTip }}</div>
        </el-form-item>

        <el-form-item label="答案解析">
          <el-input
            v-model="form.analysis"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 10 }"
            placeholder="请输入答案解析（可选）"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="submit('submit_for_review')">
            提交审核
          </el-button>
          <el-button :loading="draftLoading" @click="submit('save_draft')">保存为待校对</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createQuestion, listCategories } from '@/api/question'

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
  { value: 7, label: '复合题（大题）' },
]
const difficultyOptions = [
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' },
]
const optionLetters = ['A', 'B', 'C', 'D', 'E', 'F']

const formRef = ref(null)
const submitLoading = ref(false)
const draftLoading = ref(false)
const categoryTree = ref([])
const categoryCascader = ref(null)

const defaultForm = () => ({
  bizSection: 1,
  categoryId: null,
  parentId: 0,
  type: 1,
  subType: '',
  sort: 0,
  difficulty: 2,
  content: '',
  options: ['', '', '', ''],
  answer: '',
  score: 3,
  analysis: '',
  year: new Date().getFullYear(),
  source: '',
  status: null,
  submitType: '',
})

const form = reactive(defaultForm())

const rules = {
  bizSection: [{ required: true, message: '请选择业务分区', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入正确答案', trigger: 'blur' }],
}

const hasOptions = computed(() => [1, 2, 3].includes(form.type))

const answerPlaceholder = computed(() => {
  switch (form.type) {
    case 1: return '单选答案，如：A'
    case 2: return '多选答案，如：ABD'
    case 3: return '判断答案，如：正确 或 错误（或 T/F）'
    case 4: return '填空答案，多个空用 | 分隔，如：答案1|答案2'
    case 5: return '简答参考范文'
    case 6: return '计算题参考答案'
    case 7: return '复合题无需统一答案（在子题中设置）'
    default: return '请输入正确答案'
  }
})

const answerTip = computed(() => {
  if (form.type === 2) return '多选答案按字母顺序连写，例如 "ABD"'
  if (form.type === 4) return '多个空用 | 分隔，按填空顺序书写'
  return ''
})

function onTypeChange() {
  if (hasOptions.value && form.options.length === 0) {
    form.options = ['', '', '', '']
  }
  if (!hasOptions.value) {
    form.options = []
  }
}

function addOption() {
  if (form.options.length < 6) form.options.push('')
}

function removeOption(idx) {
  form.options.splice(idx, 1)
}

function onCategoryChange(val) {
  form.categoryId = val
}

function buildTree(list) {
  const map = {}
  const roots = []
  list.forEach((i) => {
    map[i.id] = { id: i.id, name: i.name, parentId: i.parentId, sort: i.sort, children: [] }
  })
  list.forEach((i) => {
    const node = map[i.id]
    if (!i.parentId || i.parentId === 0) {
      roots.push(node)
    } else if (map[i.parentId]) {
      map[i.parentId].children.push(node)
    } else {
      roots.push(node)
    }
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

async function submit(submitType) {
  const loadingRef = submitType === 'save_draft' ? draftLoading : submitLoading
  loadingRef.value = true
  try {
    // validate 校验失败会 reject false，必须放进 try 否则会冒泡触发 dev-server overlay
    await formRef.value.validate()
    if (hasOptions.value) {
      const valid = form.options.filter((o) => o && o.trim())
      if (valid.length < 2) {
        ElMessage.warning('选择题至少填写 2 个选项')
        return
      }
    }
    const payload = {
      ...form,
      options: hasOptions.value ? form.options.filter((o) => o && o.trim()) : null,
      submitType,
    }
    const id = await createQuestion(payload)
    ElMessage.success(submitType === 'save_draft' ? `待校对题目已保存（ID: ${id}）` : `已提交审核（ID: ${id}）`)
    resetForm()
  } catch (e) {
    // 校验失败：e 为 false 或字段错误对象，由 Element Plus 在表单项上标红，无需额外弹窗
    // API 错误：由 request.js 拦截器统一 ElMessage.error 提示
    // 不再向外抛，避免触发 dev-server overlay 显示 [object Object]
  } finally {
    loadingRef.value = false
  }
}

function resetForm() {
  Object.assign(form, defaultForm())
  // 重置后仍保留默认"语文"分类，避免每次录入都要重新选择
  const id = findDefaultCategoryId()
  if (id) {
    form.categoryId = id
    categoryCascader.value = id
  } else {
    categoryCascader.value = null
  }
  formRef.value?.clearValidate()
}

onMounted(() => {
  loadCategories()
})
</script>

<style lang="scss" scoped>
.question-entry {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
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

  .form-tip {
    color: #909399;
    font-size: 12px;
    line-height: 1.4;
    margin-top: 4px;
  }
}
</style>
