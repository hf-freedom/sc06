<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>班次管理</span>
          <el-button type="primary" @click="handleAdd">添加班次</el-button>
        </div>
      </template>
      
      <el-table :data="shifts" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="班次ID" width="100" />
        <el-table-column prop="name" label="班次名称" width="150">
          <template #default="scope">
            <el-tag :type="getShiftType(scope.row.name)">
              {{ scope.row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="150" />
        <el-table-column prop="endTime" label="结束时间" width="150" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="班次名称" required>
          <el-select v-model="form.name" placeholder="请选择班次" style="width: 100%">
            <el-option label="早班" value="早班" />
            <el-option label="中班" value="中班" />
            <el-option label="晚班" value="晚班" />
            <el-option label="夜班" value="夜班" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" required>
          <el-time-select
            v-model="form.startTime"
            placeholder="选择开始时间"
            start="00:00"
            step="00:30"
            end="23:30"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-time-select
            v-model="form.endTime"
            placeholder="选择结束时间"
            start="00:00"
            step="00:30"
            end="23:30"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { shiftApi } from '../api'

const shifts = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const form = ref({
  name: '',
  startTime: '',
  endTime: ''
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

const loadShifts = async () => {
  loading.value = true
  try {
    const res = await shiftApi.getAll()
    if (res.data.code === 200) {
      shifts.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取班次列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加班次'
  form.value = {
    name: '',
    startTime: '',
    endTime: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑班次'
  form.value = {
    ...row
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该班次吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await shiftApi.delete(row.id)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadShifts()
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    let res
    if (isEdit.value) {
      res = await shiftApi.update(form.value.id, form.value)
    } else {
      res = await shiftApi.create(form.value)
    }
    
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadShifts()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadShifts()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
