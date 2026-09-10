import type { LoginCredentials, User } from "../types/auth";

const API_URL = "/api/auth";

export const logoutUser = async (): Promise<void> => {
    localStorage.removeItem("user");
  };

  export const getUser = async (): Promise<User | null> => {
    const token = localStorage.getItem("token");
  
    if (!token) {
      return null;
    }
  
    const response = await fetch(`${API_URL}/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  
    if (response.status === 401 || response.status === 403) {
      localStorage.removeItem("token");
      return null;
    }
  
    if (!response.ok) {
      throw new Error("Kunde inte hämta användare");
    }
  
    return response.json();
  };


  export const loginUser = async (credentials: LoginCredentials) => {
    const response = await fetch(`${API_URL}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(credentials),
    });
  
    if (!response.ok) {
      throw new Error("Fel e-post eller lösenord");
    }
  
    const data = await response.json();
  
    localStorage.setItem("token", data.token);
  
    return data;
  };