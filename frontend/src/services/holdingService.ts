export const getHoldings = async () => {
    const token = localStorage.getItem("token");
  
    if (!token) {
      throw new Error("Ingen token hittades");
    }
  
    const response = await fetch("/api/holdings", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  
    if (!response.ok) {
      throw new Error(`Kunde inte hämta holdings: ${response.status}`);
    }
  
    return await response.json();
  };

  export const getPortfolio = async () => {
    const token = localStorage.getItem("token");
  
    if (!token) {
      throw new Error("Ingen token hittades");
    }
  
    const response = await fetch("/api/portfolio", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  
    if (!response.ok) {
      throw new Error(`Kunde inte hämta portfolio: ${response.status}`);
    }
  
    return await response.json();
  };