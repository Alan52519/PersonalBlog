import { createRouter, createWebHistory } from 'vue-router';
import Login from '../components/Login.vue';
import Register from '../components/Register.vue';
import Home from '../components/Home.vue';
import NewPost from '../components/NewPost.vue';
import Chat from '../components/Chat.vue';
import Edit from '../components/EditPost.vue';

const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },

  { path: '/home', component: Home, meta: { requiresAuth: true } },
  { path: '/newpost', component: NewPost, meta: { requiresAuth: true } },
  { path: '/chat', component: Chat, meta: { requiresAuth: true } },
 { path: '/edit/:id', component: Edit, meta: { requiresAuth: true } },
  { path: '/', redirect: '/login' }, // 默认重定向
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");

  // 访问需要登录的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      console.warn("未登录，禁止访问页面:", to.path);
      return next("/login");
    }
  }

  next();
});

export default router;
