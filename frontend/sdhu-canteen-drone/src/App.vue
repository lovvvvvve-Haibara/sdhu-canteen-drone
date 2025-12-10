<template>
  <router-view />
</template>

<script setup>
// 这里暂时不放布局，直接由各个页面自己组织布局。
// 后面如果你想做顶栏/侧边栏，我们可以在这里加 MainLayout。
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
}
</style>
