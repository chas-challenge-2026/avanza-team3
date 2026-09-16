import { useEffect, useState, type ReactNode } from "react";
import AuthContext from "./AuthContext";
import type { User, LoginCredentials } from "../types/auth";
import { loginUser, logoutUser, getUser } from "../services/authService";

function AuthProvider ({ children}: {children: ReactNode}) {
    const [user, setUser] = useState<User | null>(null);

    useEffect(() => {
      const loadUser = async () => {
        const currentUser = await getUser();
        setUser(currentUser);
    };
    loadUser();
  }, []);

    const login = async (
        credentials: LoginCredentials
      ): Promise<User> => {
       await loginUser(credentials);
      
       const loggedInUser = await getUser();

       console.log("loggedInUser from /me:", loggedInUser);

       if (!loggedInUser) {
        throw new Error("Kunde inte hämta användaren.");
      }
        setUser(loggedInUser);
      
        return loggedInUser;
      };

    const logout = async (): Promise<void> => {
        await logoutUser();

        setUser(null);
      };

    return (
        <AuthContext.Provider value={{ user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export default AuthProvider;