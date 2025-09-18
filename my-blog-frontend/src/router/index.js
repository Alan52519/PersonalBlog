import { createRouter, createWebHistory } from 'vue-router';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import Home from '../components/Home.vue';
import NewPost from '../components/NewPost.vue';

const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/Home', component: Home },
   { path: '/newpost', component: NewPost },
  { path: '/', redirect: '/login' } // 默认重定向
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
