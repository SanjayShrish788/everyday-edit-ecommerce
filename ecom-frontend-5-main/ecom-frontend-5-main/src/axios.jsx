import axios from "axios";

// One API root is shared by JSON requests, multipart forms, and image URLs.
// Vite reads this setting at startup/build time, so restart after editing .env.
export const API_BASE_URL = (
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8081/api"
).replace(/\/$/, "");

const API = axios.create({ baseURL: API_BASE_URL });
// This demo API does not consume an Authorization header from other Axios users.
delete API.defaults.headers.common["Authorization"];
export default API;
