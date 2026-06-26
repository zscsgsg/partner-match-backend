import Index from "../pages/IndexPage.vue";
import Team from "../pages/TeamPage.vue";
import UserPage from "../pages/UserPage.vue";
import Search from "../pages/SearchPage.vue";
import EditUserPage from "../pages/EditUserPage.vue";
import SearchResultPage from "../pages/SearchResultPage.vue";
import LoginPage from "../pages/LoginPage.vue";
import TeamAddPage from "../pages/TeamAddPage.vue";
import TeamUpdatePage from "../pages/TeamUpdatePage.vue";
import UserUpdatePage from "../pages/UserUpdatePage.vue";
import UserTeamPageCreate from "../pages/UserTeamCreatePage.vue";
import UserTeamPageJoin from "../pages/UserTeamJoinPage.vue";
import RegisterPage from "../pages/RegisterPage.vue";
import TeamChatPage from "../pages/TeamChatPage.vue";
import TeamHistoryPage from "../pages/TeamHistoryPage.vue";
import EditTagsPage from "../pages/EditTagsPage.vue";
const routes = [
  { path: "/", component: Index },
  { path: "/team", component: Team, meta: { title: "队伍" } },
  { path: "/user", component: UserPage, meta: { title: "用户" } },
  { path: "/search", component: Search, meta: { title: "搜索" } },
  {
    path: "/user/edit",
    component: EditUserPage,
    meta: { title: "编辑用户信息" },
  },
  {
    path: "/user/list",
    component: SearchResultPage,
    meta: { title: "用户列表" },
  },
  { path: "/user/login", component: LoginPage, meta: { title: "登录用户" } },
  {
    path: "/user/register",
    component: RegisterPage,
    meta: { title: "注册账号" },
  },
  { path: "/team/add", component: TeamAddPage, meta: { title: "添加队伍" } },
  {
    path: "/team/update",
    component: TeamUpdatePage,
    meta: { title: "更新队伍" },
  },
  {
    path: "/user/update",
    component: UserUpdatePage,
    meta: { title: "更新用户信息" },
  },
  {
    path: "/user/team/create",
    component: UserTeamPageCreate,
    meta: { title: "创建用户队伍" },
  },
  {
    path: "/user/team/join",
    meta: { title: "加入用户队伍" },
    component: UserTeamPageJoin,
  },
  {
    path: "/team/chat",
    component: TeamChatPage,
    meta: { title: "队伍聊天室" },
  },
  {
    path: "/team/:userId/history",
    component: TeamHistoryPage,
    meta: { title: "组队历史" },
  },
  {
    path: "/user/edit/tags",
    component: EditTagsPage,
    meta: { title: "编辑标签" },
  },
];
export default routes;
