import { createApp } from "vue";
import "./style.css";
import App from "./App.vue";
// import "vant/lib/index.css";
import { createMemoryHistory, createRouter } from "vue-router";
import routes from "./config/route";
const router = createRouter({
  history: createMemoryHistory(),
  routes,
});
createApp(App).use(router).mount("#app");
