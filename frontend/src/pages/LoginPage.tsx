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
        <section className={styles.loginContainer}>
                <h1>Logga in</h1>
                <LoginForm />
        </section>
      );
    }

export default LoginPage;