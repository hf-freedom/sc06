<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>缺口列表</span>
          <div>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="margin-right: 10px"
            />
            <el-button type="primary" @click="loadGaps">查询</el-button>
          </div>
        </div>
      </template>

      <el-table :data="gaps" style="width: 100%" v-loading="loading">
        <el-table-column prop="date" label="日期" width="150" sortable />
        <el-table-column prop="shiftName" label="班次" width="120">
          <template #default="scope">
            <el-tag :type="getShiftType(scope.row.shiftName)">
              {{ scope.row.shiftName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requiredCount" label="需求人数" width="120" />
        <el-table-column prop="actualCount" label="实际人数" width="120" />
        <el-table-column prop="gapCount" label="缺口人数" width="120">
          <template #default="scope">
            <el-tag type="danger">
              {{ scope.row.gapCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag type="warning">待补充</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="gaps.length === 0 && !loading" description="暂无缺口记录" />
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>缺口统计</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="总缺口数" :value="totalGaps" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="涉及天数" :value="uniqueDays" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="最大单日缺口" :value="maxGapPerDay" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="涉及班次类型" :value="uniqueShifts" />
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { gapApi } from '../api'

const loading = ref(false)
const dateRange = ref([])
const gaps = ref([])

const getShiftType = (name) => {
  const types = {
    '早班': 'success',
    '中班': 'warning',
    '晚班': 'primary',
    '夜班': 'danger'
  }
  return types[name] || 'info'
}

const totalGaps = computed(() => {
  return gaps.value.reduce((sum, gap) => sum + gap.gapCount, 0)
})

const uniqueDays = computed(() => {
  const days = new Set(gaps.value.map(gap => gap.date))
  return days.size
})

const maxGapPerDay = computed(() => {
  if (gaps.value.length === 0) return 0
  const dayGaps = {}
  gaps.value.forEach(gap => {
    dayGaps[gap.date] = (dayGaps[gap.date] || 0) + gap.gapCount
  })
  return Math.max(...Object.values(dayGaps))
})

const uniqueShifts = computed(() => {
  const shifts = new Set(gaps.value.map(gap => gap.shiftName))
  return shifts.size
})

const initDateRange = () => {
  const today = new Date()
  const startDate = new Date(today)
  startDate.setDate(today.getDate() - today.getDay() + (today.getDay() === 0 ? -6 : 1))
  const endDate = new Date(startDate)
  endDate.setDate(startDate.getDate() + 13)
  
  const formatDate = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  
  dateRange.value = [formatDate(startDate), formatDate(endDate)]
}

const loadGaps = async () => {
  loading.value = true
  try {
    let res
    if (dateRange.value && dateRange.value.length === 2) {
      res = await gapApi.getByRange(dateRange.value[0], dateRange.value[1])
    } else {
      res = await gapApi.getAll()
    }
    
    if (res.data.code === 200) {
      gaps.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取缺口列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initDateRange()
  loadGaps()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
