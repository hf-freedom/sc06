<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>排班表</span>
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
            <el-button type="primary" @click="loadAllData">查询</el-button>
            <el-button type="success" @click="generateSchedule">生成排班</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="scheduleTableData"
        style="width: 100%"
        v-loading="loading"
        border
      >
        <el-table-column prop="employeeName" label="员工" width="120" fixed />
        <el-table-column
          v-for="date in dates"
          :key="date"
          :label="formatDateLabel(date)"
          min-width="180"
        >
          <template #default="scope">
            <div v-if="getSchedule(scope.row.employeeId, date)">
              <el-tag 
                :type="getShiftType(getSchedule(scope.row.employeeId, date).shiftName)"
                @click="showScheduleDetail(scope.row.employeeId, date)"
                style="cursor: pointer"
              >
                {{ getSchedule(scope.row.employeeId, date).shiftName }}
              </el-tag>
              <el-button
                type="danger"
                link
                size="small"
                @click="handleDeleteSchedule(scope.row.employeeId, date)"
              >
                删除
              </el-button>
            </div>
            <div v-else>
              <el-button type="primary" link size="small" @click="handleAssign(scope.row.employeeId, date)">
                排班
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>缺口记录</span>
          <el-badge :value="gaps.length" :hidden="gaps.length === 0" class="item">
            <el-tag type="danger" v-if="gaps.length > 0">存在缺口</el-tag>
          </el-badge>
        </div>
      </template>

      <el-table
        :data="gaps"
        style="width: 100%"
        v-loading="gapLoading"
      >
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
      </el-table>

      <el-empty v-if="gaps.length === 0 && !gapLoading" description="暂无缺口记录" />

      <div v-if="gaps.length > 0" style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #ebeef5">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="总缺口数">
            <el-tag type="danger">{{ totalGapCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="涉及天数">
            <el-tag type="warning">{{ uniqueDaysCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最大单日缺口">
            <el-tag type="danger">{{ maxGapPerDay }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="涉及班次类型">
            <el-tag type="warning">{{ uniqueShiftsCount }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-dialog v-model="swapDialogVisible" title="换班" width="400px">
      <el-form :model="swapForm" label-width="100px">
        <el-form-item label="日期">
          <span>{{ swapForm.date }}</span>
        </el-form-item>
        <el-form-item label="原员工">
          <span>{{ getEmployeeName(swapForm.employee1Id) }}</span>
        </el-form-item>
        <el-form-item label="目标员工">
          <el-select v-model="swapForm.employee2Id" placeholder="请选择目标员工" style="width: 100%">
            <el-option
              v-for="emp in availableEmployees"
              :key="emp.id"
              :label="emp.name"
              :value="emp.id"
              :disabled="emp.id === swapForm.employee1Id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="swapDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="executeSwap">确认换班</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="手动排班" width="400px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="员工">
          <span>{{ getEmployeeName(assignForm.employeeId) }}</span>
        </el-form-item>
        <el-form-item label="日期">
          <span>{{ assignForm.date }}</span>
        </el-form-item>
        <el-form-item label="班次" required>
          <el-select v-model="assignForm.shiftId" placeholder="请选择班次" style="width: 100%">
            <el-option
              v-for="shift in shifts"
              :key="shift.id"
              :label="shift.name"
              :value="shift.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="executeAssign">确认排班</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="scheduleDetailVisible" title="排班详情" width="400px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="员工">{{ scheduleDetail.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ scheduleDetail.date }}</el-descriptions-item>
        <el-descriptions-item label="班次">{{ scheduleDetail.shiftName }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ scheduleDetail.position }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="openSwapFromDetail">换班</el-button>
        <el-button type="danger" @click="handleDeleteSchedule(scheduleDetail.employeeId, scheduleDetail.date)">
          删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { scheduleApi, shiftApi, employeeApi, swapApi, gapApi } from '../api'

const loading = ref(false)
const gapLoading = ref(false)
const dateRange = ref([])
const dates = ref([])
const schedules = ref([])
const employees = ref([])
const shifts = ref([])
const scheduleTableData = ref([])
const gaps = ref([])

const swapDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const scheduleDetailVisible = ref(false)

const swapForm = ref({
  employee1Id: null,
  employee2Id: null,
  date: ''
})

const assignForm = ref({
  employeeId: null,
  date: '',
  shiftId: null
})

const scheduleDetail = ref({
  employeeId: null,
  employeeName: '',
  date: '',
  shiftName: '',
  position: ''
})

const totalGapCount = computed(() => {
  return gaps.value.reduce((sum, gap) => sum + gap.gapCount, 0)
})

const uniqueDaysCount = computed(() => {
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

const uniqueShiftsCount = computed(() => {
  const shiftNames = new Set(gaps.value.map(gap => gap.shiftName))
  return shiftNames.size
})

const availableEmployees = computed(() => {
  return employees.value.filter(emp => emp.status === 1)
})

const getShiftType = (name) => {
  const types = {
    '早班': 'success',
    '中班': 'warning',
    '晚班': 'primary',
    '夜班': 'danger'
  }
  return types[name] || 'info'
}

const formatDateLabel = (dateStr) => {
  const date = new Date(dateStr)
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const month = date.getMonth() + 1
  const day = date.getDate()
  const weekDay = weekDays[date.getDay()]
  return `${month}/${day} 周${weekDay}`
}

const getEmployeeName = (id) => {
  const emp = employees.value.find(e => e.id === id)
  return emp ? emp.name : ''
}

const getShiftName = (id) => {
  const shift = shifts.value.find(s => s.id === id)
  return shift ? shift.name : ''
}

const getSchedule = (employeeId, date) => {
  const schedule = schedules.value.find(s => 
    s.employeeId === employeeId && s.date === date
  )
  if (schedule) {
    return {
      ...schedule,
      shiftName: getShiftName(schedule.shiftId)
    }
  }
  return null
}

const initDateRange = () => {
  const today = new Date()
  const startDate = new Date(today)
  startDate.setDate(today.getDate() - today.getDay() + (today.getDay() === 0 ? -6 : 1))
  const endDate = new Date(startDate)
  endDate.setDate(startDate.getDate() + 6)
  
  const formatDate = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  
  dateRange.value = [formatDate(startDate), formatDate(endDate)]
}

const loadEmployees = async () => {
  try {
    const res = await employeeApi.getAll()
    if (res.data.code === 200) {
      employees.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  }
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

const loadGaps = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return
  }

  gapLoading.value = true
  try {
    const res = await gapApi.getByRange(dateRange.value[0], dateRange.value[1])
    if (res.data.code === 200) {
      gaps.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取缺口记录失败')
  } finally {
    gapLoading.value = false
  }
}

const loadSchedule = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }

  loading.value = true
  try {
    const res = await scheduleApi.getByRange(dateRange.value[0], dateRange.value[1])
    if (res.data.code === 200) {
      schedules.value = res.data.data
      buildDateList()
      buildTableData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取排班表失败')
  } finally {
    loading.value = false
  }
}

const loadAllData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }

  loading.value = true
  gapLoading.value = true

  try {
    await Promise.all([loadSchedule(), loadGaps()])
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
    gapLoading.value = false
  }
}

const buildDateList = () => {
  dates.value = []
  const start = new Date(dateRange.value[0])
  const end = new Date(dateRange.value[1])
  
  const current = new Date(start)
  while (current <= end) {
    const year = current.getFullYear()
    const month = String(current.getMonth() + 1).padStart(2, '0')
    const day = String(current.getDate()).padStart(2, '0')
    dates.value.push(`${year}-${month}-${day}`)
    current.setDate(current.getDate() + 1)
  }
}

const buildTableData = () => {
  scheduleTableData.value = employees.value
    .filter(emp => emp.status === 1)
    .map(emp => ({
      employeeId: emp.id,
      employeeName: emp.name
    }))
}

const generateSchedule = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }

  loading.value = true
  try {
    const res = await scheduleApi.generate(dateRange.value[0], dateRange.value[1])
    if (res.data.code === 200) {
      const gapRecords = res.data.data
      if (gapRecords && gapRecords.length > 0) {
        ElMessage.warning(`排班完成，但存在 ${gapRecords.length} 个缺口`)
      } else {
        ElMessage.success('排班完成')
      }
      loadAllData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('生成排班失败')
  } finally {
    loading.value = false
  }
}

const showScheduleDetail = (employeeId, date) => {
  const schedule = getSchedule(employeeId, date)
  if (schedule) {
    scheduleDetail.value = {
      employeeId: employeeId,
      employeeName: getEmployeeName(employeeId),
      date: date,
      shiftName: schedule.shiftName,
      position: schedule.position
    }
    scheduleDetailVisible.value = true
  }
}

const openSwapFromDetail = () => {
  scheduleDetailVisible.value = false
  swapForm.value = {
    employee1Id: scheduleDetail.value.employeeId,
    employee2Id: null,
    date: scheduleDetail.value.date
  }
  swapDialogVisible.value = true
}

const handleAssign = (employeeId, date) => {
  assignForm.value = {
    employeeId: employeeId,
    date: date,
    shiftId: null
  }
  assignDialogVisible.value = true
}

const executeAssign = async () => {
  if (!assignForm.value.shiftId) {
    ElMessage.warning('请选择班次')
    return
  }

  try {
    const res = await swapApi.assign(
      assignForm.value.employeeId,
      assignForm.value.date,
      assignForm.value.shiftId
    )
    if (res.data.code === 200) {
      ElMessage.success('排班成功')
      assignDialogVisible.value = false
      loadAllData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('排班失败')
  }
}

const executeSwap = async () => {
  if (!swapForm.value.employee2Id) {
    ElMessage.warning('请选择目标员工')
    return
  }

  try {
    const res = await swapApi.execute(
      swapForm.value.employee1Id,
      swapForm.value.employee2Id,
      swapForm.value.date
    )
    if (res.data.code === 200) {
      ElMessage.success('换班成功')
      swapDialogVisible.value = false
      loadAllData()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('换班失败')
  }
}

const handleDeleteSchedule = async (employeeId, date) => {
  ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await swapApi.deleteSchedule(employeeId, date)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        scheduleDetailVisible.value = false
        loadAllData()
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(async () => {
  initDateRange()
  await loadEmployees()
  await loadShifts()
  loadAllData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
