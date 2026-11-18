import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await api.post('/Users/login', form);
      login(data);
      navigate('/products');
    } catch {
      setError('Invalid email or password');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>🌾 UrbanFleet</h2>
        <h3>Login</h3>
        {error && <p className="error">{error}</p>}
        <form onSubmit={handleSubmit}>
          <input placeholder="Email" type="email" value={form.email}
            onChange={e => setForm({ ...form, email: e.target.value })} required />
          <input placeholder="Password" type="password" value={form.password}
            onChange={e => setForm({ ...form, password: e.target.value })} required />
          <button type="submit">Login</button>
        </form>
        <p>Don't have an account? <Link to="/signup">Sign up</Link></p>
      </div>
    </div>
  );
}
