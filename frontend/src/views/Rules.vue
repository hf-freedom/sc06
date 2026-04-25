<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排班规则管理</span>
          <el-button type="primary" @click="batchEdit">批量编辑</el-button>
        </div>
      </template>

      <el-table
        :data="rulesTableData"
        style="width: 100%"
        v-loading="loading"
        border
      >
        <el-table-column prop="dayOfWeek" label="星期" width="150">
          <template #default="scope">
            {{ getWeekDayName(scope.row.dayOfWeek) }}
          </template>
        </el-table-column>
        <el-table-column
          v-for="shift in shifts"
          :key="shift.id"
          :label="shift.name"
          min-width="150"
        >
          <template #default="scope">
            <el-input-number
              v-model="scope.row[`shift_${shift.id}`]"
              :min="0"
              :max="10"
              @change="handleRuleChange(scope.row.dayOfWeek, shift.id, scope.row[`shift_${shift.id}`])"
            />
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right">
        <el-button type="primary" @click="saveAllRules">保存所有规则</el-button>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>规则说明</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="配置方式">
          表格中每个数字代表对应星期、对应班次需要安排的员工数量。
        </el-descriptions-item>
        <el-descriptions-item label="默认值">
          系统默认每天每个班次需要 2 人。
        </el-descriptions-item>
        <el-descriptions-item label="生效方式">
          修改规则后，点击"保存所有规则"按钮即可保存。下次生成排班时会使用新的规则。
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { ruleApi, shiftApi } from '../api'

const loading = ref(false)
const rules = ref([])
const shifts = ref([])
const rulesTableData = ref([])

const changedRules = reactive(new Map())

const weekDays = [
  { value: 1, name: '星期一' },
  { value: 2, name: '星期二' },
  { value: 3, name: '星期三' },
  { value: 4, name: '星期四' },
  { value: 5, name: '星期五' },
  { value: 6, name: '星期六' },
  { value: 7, name: '星期日' }
]

const getWeekDayName = (dayOfWeek) => {
  const day = weekDays.find(d => d.value === dayOfWeek)
  return day ? day.name : ''
}

const loadShifts = async () => {
  try {
    const res = await shiftApi.getAll()
    if (res.data.code === 200) {
      shifts.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取班次列表失败')
  }
}

const loadRules = async () => {
  loading.value = true
  try {
    const res = await ruleApi.getAll()
    if (res.data.code === 200) {
      rules.value = res.data.data
      buildTableData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取排班规则失败')
  } finally {
    loading.value = false
  }
}

const buildTableData = () => {
  rulesTableData.value = []
  changedRules.clear()

  for (let day = 1; day <= 7; day++) {
    const row = {
      dayOfWeek: day
    }

    for (const shift of shifts.value) {
      const rule = rules.value.find(
        r => r.dayOfWeek === day && r.shiftId === shift.id
      )
      row[`shift_${shift.id}`] = rule ? rule.requiredCount : 0
      row[`ruleId_${shift.id}`] = rule ? rule.id : null
    }

    rulesTableData.value.push(row)
  }
}

const handleRuleChange = (dayOfWeek, shiftId, count) => {
  const key = `${dayOfWeek}_${shiftId}`
  const row = rulesTableData.value.find(r => r.dayOfWeek === dayOfWeek)
  const ruleId = row ? row[`ruleId_${shiftId}`] : null

  changedRules.set(key, {
    dayOfWeek,
    shiftId,
    requiredCount: count,
    ruleId
  })
}

const saveAllRules = async () => {
  if (changedRules.size === 0) {
    ElMessage.info('没有需要保存的更改')
    return
  }

  loading.value = true
  let successCount = 0
  let failCount = 0

  for (const [key, ruleData] of changedRules) {
    try {
      if (ruleData.ruleId) {
        const res = await ruleApi.update(ruleData.ruleId, {
          id: ruleData.ruleId,
          dayOfWeek: ruleData.dayOfWeek,
          shiftId: ruleData.shiftId,
          requiredCount: ruleData.requiredCount
        })
        if (res.data.code === 200) {
          successCount++
        } else {
          failCount++
        }
      } else {
        const res = await ruleApi.create({
          dayOfWeek: ruleData.dayOfWeek,
          shiftId: ruleData.shiftId,
          requiredCount: ruleData.requiredCount
        })
        if (res.data.code === 200) {
          successCount++
        } else {
          failCount++
        }
      }
    } catch (error) {
      failCount++
    }
  }

  changedRules.clear()

  if (failCount === 0) {
    ElMessage.success(`成功保存 ${successCount} 条规则`)
  } else {
    ElMessage.warning(`保存完成：成功 ${successCount} 条，失败 ${failCount} 条`)
  }

  await loadRules()
  loading.value = false
}

const batchEdit = () => {
  ElMessage.info('请直接在表格中修改人数，然后点击"保存所有规则"')
}

onMounted(() => {
  loadShifts().then(() => {
    loadRules()
  })
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
