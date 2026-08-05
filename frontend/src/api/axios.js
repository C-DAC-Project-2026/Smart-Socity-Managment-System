import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
});

// Attach JWT token to every request if present
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// If token expired / invalid, or the API is unreachable, kick user back to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const unauthorized = err.response && err.response.status === 401;
    const unreachable = !err.response; // network error / wrong host / server down
    if (unauthorized || unreachable) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(err);
  }
);

export default api;
