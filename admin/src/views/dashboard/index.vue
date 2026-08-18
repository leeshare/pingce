<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover">
          <div class="stat-card">
            <el-icon class="stat-icon" :style="{ background: card.color }">
              <component :is="card.icon" />
            </el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card">
      <h2>欢迎使用陕西单招学习服务平台-管理后台</h2>
      <p>当前为 W1 阶段，已搭建管理后台基础脚手架，后续将逐步完善：</p>
      <ul>
        <li>W2：题库管理、院校库管理、用户管理完善</li>
        <li>W3：试卷管理、模考管理、错题分析</li>
        <li>W4：订单管理、会员管理、数据看板</li>
        <li>W5：内容运营、广告位、活动管理</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const cards = ref([
  { title: '用户总数', value: 0, icon: 'User', color: '#2B6CB0' },
  { title: '题目总数', value: 0, icon: 'Document', color: '#38A169' },
  { title: '今日访问', value: 0, icon: 'View', color: '#DD6B20' },
  { title: '订单总数', value: 0, icon: 'ShoppingCart', color: '#805AD5' },
])
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    color: #fff;
    font-size: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .stat-info {
    flex: 1;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: #1A202C;
  }

  .stat-label {
    font-size: 14px;
    color: #718096;
    margin-top: 4px;
  }

  .welcome-card {
    margin-top: 20px;

    h2 {
      color: #2B6CB0;
      margin: 0 0 16px;
    }

    p {
      color: #4A5568;
    }

    ul {
      color: #718096;
      padding-left: 20px;
      line-height: 1.8;
    }
  }
}
</style>
