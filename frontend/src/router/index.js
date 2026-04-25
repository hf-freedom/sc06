import { createRouter, createWebHistory } from 'vue-router'
import Employees from '../views/Employees.vue'
import Shifts from '../views/Shifts.vue'
import Rules from '../views/Rules.vue'
import Schedule from '../views/Schedule.vue'
import Gaps from '../views/Gaps.vue'

const routes = [
  {
    path: '/',
    redirect: '/employees'
  },
  {
    path: '/employees',
    name: 'Employees',
    component: Employees
  },
  {
    path: '/shifts',
    name: 'Shifts',
    component: Shifts
  },
  {
    path: '/rules',
    name: 'Rules',
    component: Rules
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: Schedule
  },
  {
    path: '/gaps',
    name: 'Gaps',
    component: Gaps
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
