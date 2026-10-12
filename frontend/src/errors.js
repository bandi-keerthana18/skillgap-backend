export function errText(err, fallback) {
  const d = err && err.response && err.response.data;
  if (d && d.message) return d.message;
  if (!err || !err.response) return "Cannot reach the server. Is the backend running?";
  const s = err.response.status;
  if (s === 403) return "You do not have permission to do this.";
  if (s === 409) return "That conflicts with existing data.";
  if (s === 400) return "Please check the details you entered.";
  return fallback;
}
