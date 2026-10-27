const KEY = 'club_token'

/** 读取令牌 */
export const getToken = () => localStorage.getItem(KEY)

/** 登录成功后写入令牌 */
export const setToken = (token) => localStorage.setItem(KEY, token)

/** 登出或令牌失效时清除 */
export const clearToken = () => localStorage.removeItem(KEY)

export const isLoggedIn = () => Boolean(getToken())
