import type { LoginCredentials, User } from "../types/auth";

const mockUser: User = {
    id: 1,
    name: "Anna",
    email: "anna@example.com",
  };

const mockPassword = "password";

export const loginUser = async ( credentials: LoginCredentials ): Promise<User> => {
    if (
        credentials.email !== mockUser.email ||
        credentials.password !== mockPassword
      ) {
        throw new Error("Fel e-post eller lösenord");
      }

    localStorage.setItem("user", JSON.stringify(mockUser));

    return mockUser;
}

export const logoutUser = async (): Promise<void> => {
    localStorage.removeItem("user");
  };

export const getUser = (): User | null => {
    const storedUser = localStorage.getItem("user");
  
    if (!storedUser) {
      return null;
    }
  
    return JSON.parse(storedUser) as User;
  };