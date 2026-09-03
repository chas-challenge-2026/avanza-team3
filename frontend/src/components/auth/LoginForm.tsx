import { useState } from "react";
import useAuth from "../../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import styles from "./LoginForm.module.css"
import Button from "@mui/material/Button";

function LoginForm(){
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
        e.preventDefault();

        setError("");
        setLoading(true);

        try {
            await login({ email, password });
            navigate("/");
          } catch {
            setError("Fel e-post eller lösenord");
          } finally {
            setLoading(false);
          }
      };

    return (
        <form className={styles.loginForm} onSubmit={handleSubmit}>
            <h3>Logga in</h3>
            <label>E-post
                <input 
                type="email"
                name="email"
                autoComplete="email"
                placeholder="din.mail@domän.se"
                required 
                value={email}
                aria-invalid={error !== ''}
                aria-describedby={error ? "login-error" : undefined}
                onChange={(e) => setEmail(e.target.value)} />
            </label>

            <label>Lösenord
                <input 
                type="password"
                name="password"
                autoComplete="current-password"
                required 
                placeholder="Ange ditt lösenord"
                value={password}
                aria-invalid={error !== ''}
                aria-describedby={error ? "login-error" : undefined}
                onChange={(e) => setPassword(e.target.value)}
                 />
            </label>
            {error && <p className={styles.error} id="login-error" role="alert">{error}</p>}
            <Button type="submit" disabled={loading} variant="contained" fullWidth>
                {loading ? "Loggar in..." : "Logga in"}
            </Button>
        </form>
    )
}

export default LoginForm;