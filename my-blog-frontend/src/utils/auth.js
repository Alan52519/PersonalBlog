export function getCurrentUser() {
  const token = localStorage.getItem("token")
  if (!token) return null

  try {
    const base64 = token.split(".")[1]

    // 先 base64 decode，再 UTF-8 解码
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map(c => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    )

    const payload = JSON.parse(json)

    return {
      username: payload.sub || payload.username,
      userId: payload.userId
    }
  } catch (e) {
    console.error("Token 解析失败：", e)
    return null
  }
}