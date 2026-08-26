import { useState } from "react";
import useAuth from "../../hooks/useAuth";
import { useNavigate } from "react-router-dom";

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
          } catch (err) {
            console.log("error in loginform: ", err);
            setError("Fel e-post eller lösenord");
          } finally {
            setLoading(false);
          }
      };

    return (
        <form onSubmit={handleSubmit}>
            <label>Email
                <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
            </label>

            <label>Password
                <input type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
            </label>
            {error && <p>{error}</p>}
            <button type="submit" disabled={loading}>
                {loading ? "Loggar in..." : "Logga in"}
            </button>
        </form>
    )
}

export default LoginForm;