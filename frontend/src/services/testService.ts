async function test() {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/portfolio", {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  if (!response.ok) {
    throw new Error("kunde inte hämta data");
  }
  const data = await response.json();
  console.log(data);
  return data;
}
test();
