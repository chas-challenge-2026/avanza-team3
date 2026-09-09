import type { LoginCredentials, User } from "../types/auth";

const API_URL = "http://localhost:8082/api/auth";

// MOCK STUFF
// const mockUsers: User[] = [
//   {
//     id: 1,
//     name: "Anna",
//     email: "anna@example.com",
//   },
//   {
//     id: 2,
//     name: "Erik",
//     email: "erik@example.com",
//   },
// ];

// const mockPassword = "password";

// export const loginUser = async ( credentials: LoginCredentials ): Promise<User> => {

//   const user = mockUsers.find(
//     (user) => user.email === credentials.email
//   );

//   if (!user || credentials.password !== mockPassword) {
//     throw new Error("Fel e-post eller lösenord");
//   }
//     localStorage.setItem("user", JSON.stringify(user));

//     return user;
// }

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