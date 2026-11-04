import { createContext } from "react";
import type { User, LoginCredentials } from "../types/auth";


type AuthContextType = {
    user: User | null;
    loading: boolean;
    login: (credentials: LoginCredentials) => Promise<User>;
    logout: () => Promise<void>;
    }

const AuthContext = createContext<AuthContextType | undefined>(undefined); 

export default AuthContext;