import type { LoginCredentials, User } from "../types/auth";

const mockUsers: User[] = [
  {
    id: 1,
    name: "Anna",
    email: "anna@example.com",
  },
  {
    id: 2,
    name: "Erik",
    email: "erik@example.com",
  },
];

const mockPassword = "password";

export const loginUser = async ( credentials: LoginCredentials ): Promise<User> => {

  const user = mockUsers.find(
    (user) => user.email === credentials.email
  );

  if (!user || credentials.password !== mockPassword) {
    throw new Error("Fel e-post eller lösenord");
  }
    localStorage.setItem("user", JSON.stringify(user));

    return user;
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