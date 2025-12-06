<template>
  <AppLayout v-if="showLayout" />
  <router-view v-else />
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import AppLayout from './components/AppLayout.vue';

const route = useRoute();

// 判断是否需要显示布局（登录和注册页面不显示）
const showLayout = computed(() => {
  return route.name !== 'Login' && route.name !== 'Register';
});
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  height: 100%;
}

a {
  color: inherit;
  text-decoration: none;
}

button {
  cursor: pointer;
}
</style>
