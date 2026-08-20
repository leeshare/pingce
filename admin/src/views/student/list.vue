<template>
  <div class="student-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>学员列表</span>
          <span class="text-muted">数据来自微信小程序登录过的用户</span>
        </div>
      </template>

      <el-form :inline="true" :model="query">
        <el-form-item label="关键字">
          <el-input
            v-model="query.keyword"
            placeholder="昵称 / 手机号"
            clearable
            @keyup.enter="loadList(1)"
          />
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="query.identity" placeholder="全部" clearable style="width: 160px">
            <el-option value="sanxiao" label="三校生" />
            <el-option value="putong" label="普通高中毕业生" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="query.memberLevel" placeholder="全部" clearable style="width: 120px">
            <el-option :value="0" label="普通" />
            <el-option :value="1" label="VIP" />
          </el-select>
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="query.province" placeholder="如 陕西" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="学校">
          <el-input v-model="query.school" placeholder="学校关键字" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadList(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="tableLoading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="学员" min-width="220">
          <template #default="{ row }">
            <div class="student-cell">
              <el-avatar :size="36" :src="row.avatar" />
              <div class="student-info">
                <div class="nickname">{{ row.nickname || '未授权昵称' }}</div>
                <div class="phone text-muted">{{ row.phone || '未绑定手机' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ genderText(row.gender) }}</template>
        </el-table-column>
        <el-table-column prop="identity" label="身份" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.identity" size="small" :type="row.identity === 'sanxiao' ? 'danger' : 'info'">
              {{ identityText(row.identity) }}
            </el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="地区" min-width="180">
          <template #default="{ row }">
            <span v-if="row.province || row.city || row.district">
              {{ [row.province, row.city, row.district].filter(Boolean).join(' / ') }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="school" label="学校" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.school">{{ row.school }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="90">
          <template #default="{ row }">
            <span v-if="row.grade">{{ row.grade }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="会员" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.memberLevel === 1" type="warning" size="small" effect="dark">VIP</el-tag>
            <el-tag v-else type="info" size="small">普通</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="刷题统计" width="180">
          <template #default="{ row }">
            <div class="stats-cell">
              <div>累计 <span class="num">{{ row.totalPractice || 0 }}</span> 题</div>
              <div>正确率 <span :class="['num', correctRateClass(row.correctRate)]">{{ row.correctRate || 0 }}%</span></div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastPracticeAt" label="最近刷题" width="170">
          <template #default="{ row }">
            <span v-if="row.lastPracticeAt">{{ row.lastPracticeAt }}</span>
            <span v-else class="text-muted">未刷题</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { pageStudents } from '@/api/student'

const query = reactive({
  keyword: '',
  identity: '',
  memberLevel: null,
  province: '',
  school: '',
  page: 1,
  size: 10,
})
const list = ref([])
const total = ref(0)
const tableLoading = ref(false)

function genderText(v) {
  return ({ 0: '未知', 1: '男', 2: '女' })[v] || '未知'
}

// 学员身份映射（与小程序 identity.html 一致：sanxiao 三校生 / putong 普通高中毕业生）
function identityText(v) {
  return ({ sanxiao: '三校生', putong: '普通高中毕业生' })[v] || v || '-'
}

function correctRateClass(rate) {
  if (!rate || rate === 0) return 'muted'
  if (rate >= 80) return 'success'
  if (rate >= 60) return 'warning'
  return 'danger'
}

async function loadList(page) {
  if (page) query.page = page
  tableLoading.value = true
  try {
    const res = await pageStudents(query)
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
  query.identity = ''
  query.memberLevel = null
  query.province = ''
  query.school = ''
  loadList(1)
}

onMounted(() => {
  loadList(1)
})
</script>

<style lang="scss" scoped>
.student-list {
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .text-muted {
      color: #909399;
      font-size: 13px;
      font-weight: normal;
    }
  }

  .pagination {
    margin-top: 16px;
    text-align: right;
  }

  .student-cell {
    display: flex;
    align-items: center;
    gap: 10px;

    .student-info {
      .nickname {
        font-weight: 600;
        color: #2D3748;
      }
      .phone {
        font-size: 12px;
      }
    }
  }

  .stats-cell {
    font-size: 13px;
    line-height: 1.6;

    .num {
      font-weight: 600;

      &.success { color: #67c23a; }
      &.warning { color: #e6a23c; }
      &.danger  { color: #f56c6c; }
      &.muted   { color: #909399; }
    }
  }

  .text-muted {
    color: #A0AEC0;
    font-size: 13px;
  }
}
</style>
