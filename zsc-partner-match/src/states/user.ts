import type { UserType } from "../models/user";
// 当前用户状态

let currentUser: UserType;

const setCurrentUserState = (user: UserType) => {
  currentUser = user;
};

const getCurrentUserState = (): UserType => {
  return currentUser;
};

export { setCurrentUserState, getCurrentUserState };
