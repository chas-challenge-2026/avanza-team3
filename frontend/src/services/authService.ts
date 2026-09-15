import type { LoginCredentials, User } from "../types/auth";

const API_URL = "/api/auth";

export const logoutUser = async (): Promise<void> => {
  const response = await fetch(`${API_URL}/logout`, {
    method: "DELETE",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Kunde inte logga ut");
  }
};

export const getUser = async (): Promise<User | null> => {
  const response = await fetch(`${API_URL}/me`, {
    credentials: "include",
  });

  if (response.status === 401 || response.status === 403) {
    return null;
  }

  if (!response.ok) {
    throw new Error("Kunde inte hämta användare");
  }

  return response.json();
};

export const loginUser = async (
  credentials: LoginCredentials
): Promise<void> => {
  const response = await fetch(`${API_URL}/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify(credentials),
  });

  if (!response.ok) {
    throw new Error("Fel e-post eller lösenord");
  }
};