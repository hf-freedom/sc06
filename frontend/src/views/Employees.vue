<template>
  <div>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>员工管理</span>
          <el-button type="primary" @click="handleAdd">添加员工</el-button>
        </div>
      </template>
      
      <el-table :data="employees" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="员工ID" width="100" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="position" label="岗位" width="150" />
        <el-table-column prop="department" label="部门" width="150" />
        <el-table-column prop="maxShiftsPerWeek" label="每周最大班次数" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
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
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="岗位" required>
          <el-input v-model="form.position" placeholder="请输入岗位" />
        </el-form-item>
        <el-form-item label="部门" required>
          <el-input v-model="form.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="每周最大班次数" required>
          <el-input-number v-model="form.maxShiftsPerWeek" :min="1" :max="7" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option :label="在职" :value="1" />
            <el-option :label="离职" :value="0" />
          </el-select>
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
import { employeeApi } from '../api'

const employees = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)

const form = ref({
  name: '',
  position: '',
  department: '',
  maxShiftsPerWeek: 5,
  status: 1
})

const loadEmployees = async () => {
  loading.value = true
  try {
    const res = await employeeApi.getAll()
    if (res.data.code === 200) {
      employees.value = res.data.data
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('获取员工列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加员工'
  form.value = {
    name: '',
    position: '',
    department: '',
    maxShiftsPerWeek: 5,
    status: 1
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑员工'
  form.value = {
    ...row
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await employeeApi.delete(row.id)
      if (res.data.code === 200) {
        ElMessage.success('删除成功')
        loadEmployees()
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!form.value.name || !form.value.position || !form.value.department) {
    ElMessage.warning('请填写必填项')
    return
  }

  try {
    let res
    if (isEdit.value) {
      res = await employeeApi.update(form.value.id, form.value)
    } else {
      res = await employeeApi.create(form.value)
    }
    
    if (res.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadEmployees()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadEmployees()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
