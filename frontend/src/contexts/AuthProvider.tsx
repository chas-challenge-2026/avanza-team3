import { useState, type ReactNode } from "react";
import AuthContext from "./AuthContext";
import type { User, LoginCredentials } from "../types/auth";
import { loginUser, logoutUser, getUser } from "../services/authService";

function AuthProvider ({ children}: {children: ReactNode}) {
    const [user, setUser] = useState<User | null>(() => getUser());

    const login = async (
        credentials: LoginCredentials
      ): Promise<User> => {
        const loggedInUser = await loginUser(credentials);
      
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