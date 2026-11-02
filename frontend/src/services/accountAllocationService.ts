export const getAccountAllocation = async (accountId: number) => {
    const token = localStorage.getItem("token");
  
    if (!token) {
      throw new Error("Ingen token hittades");
    }
  
    const response = await fetch(
      `/api/accounts/${accountId}/allocation`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
  
    if (!response.ok) {
      throw new Error("Kunde inte hämta kontots allokering");
    }
  
    return response.json();
  };
  
  export default getAccountAllocation;