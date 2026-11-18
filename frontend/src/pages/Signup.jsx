import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';

export default function Signup() {
  const [form, setForm] = useState({ username: '', email: '', password: '', role: 'BUYER', phone: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/Users/signup', form);
      setSuccess('Account created! Redirecting to login...');
      setTimeout(() => navigate('/login'), 1500);
    } catch {
      setError('Signup failed. Email may already be in use.');
    }
  };

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>🌾 UrbanFleet</h2>
        <h3>Create Account</h3>
        {error && <p className="error">{error}</p>}
        {success && <p className="success">{success}</p>}
        <form onSubmit={handleSubmit}>
          <input placeholder="Username" value={form.username} onChange={set('username')} required />
          <input placeholder="Email" type="email" value={form.email} onChange={set('email')} required />
          <input placeholder="Password" type="password" value={form.password} onChange={set('password')} required />
          <input placeholder="Phone" value={form.phone} onChange={set('phone')} required />
          <select value={form.role} onChange={set('role')}>
            <option value="BUYER">Buyer</option>
            <option value="FARMER">Farmer</option>
          </select>
          <button type="submit">Sign Up</button>
        </form>
        <p>Already have an account? <Link to="/login">Login</Link></p>
      </div>
    </div>
  );
}
