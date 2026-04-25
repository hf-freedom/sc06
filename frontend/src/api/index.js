import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const employeeApi = {
  getAll: () => api.get('/employees'),
  getById: (id) => api.get(`/employees/${id}`),
  create: (data) => api.post('/employees', data),
  update: (id, data) => api.put(`/employees/${id}`, data),
  delete: (id) => api.delete(`/employees/${id}`)
}

export const shiftApi = {
  getAll: () => api.get('/shifts'),
  getById: (id) => api.get(`/shifts/${id}`),
  create: (data) => api.post('/shifts', data),
  update: (id, data) => api.put(`/shifts/${id}`, data),
  delete: (id) => api.delete(`/shifts/${id}`)
}

export const ruleApi = {
  getAll: () => api.get('/rules'),
  getById: (id) => api.get(`/rules/${id}`),
  create: (data) => api.post('/rules', data),
  update: (id, data) => api.put(`/rules/${id}`, data),
  delete: (id) => api.delete(`/rules/${id}`)
}

export const scheduleApi = {
  getByRange: (startDate, endDate) => 
    api.get('/schedules', { params: { startDate, endDate } }),
  generate: (startDate, endDate) => 
    api.post('/schedules/generate', null, { params: { startDate, endDate } }),
  deleteByRange: (startDate, endDate) => 
    api.delete('/schedules', { params: { startDate, endDate } })
}

export const gapApi = {
  getAll: () => api.get('/gaps'),
  getByRange: (startDate, endDate) => 
    api.get('/gaps/range', { params: { startDate, endDate } })
}

export const swapApi = {
  validate: (employee1Id, employee2Id, date) => 
    api.post('/swap/validate', null, { params: { employee1Id, employee2Id, date } }),
  execute: (employee1Id, employee2Id, date) => 
    api.post('/swap/execute', null, { params: { employee1Id, employee2Id, date } }),
  validateAssign: (employeeId, date, shiftId) => 
    api.post('/swap/validate-assign', null, { params: { employeeId, date, shiftId } }),
  assign: (employeeId, date, shiftId) => 
    api.post('/swap/assign', null, { params: { employeeId, date, shiftId } }),
  deleteSchedule: (employeeId, date) => 
    api.delete('/swap/remove', { params: { employeeId, date } })
}

export const logApi = {
  getAll: () => api.get('/logs'),
  getByType: (type) => api.get(`/logs/type/${type}`)
}

export default api
