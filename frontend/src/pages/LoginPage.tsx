import LoginForm from "../components/auth/LoginForm";
import { Navigate } from "react-router-dom";
import useAuth from "../hooks/useAuth";
import styles from "./LoginPage.module.css";

function LoginPage(){
    const { user } = useAuth();

    if (user) {
        return <Navigate to="/" replace />;
      }
    
      return (
        <main className={styles.loginPage}>
            <header className={styles.loginHeader}>
                <span>AVANZA</span>
                <h1>Portföljhälsa</h1>
                <p>Få koll på din portfölj, risk och utveckling.</p>
            </header>

            <LoginForm />
        </main>
      );
    }

export default LoginPage;