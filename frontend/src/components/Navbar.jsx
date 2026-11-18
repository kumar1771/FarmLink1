import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { token, role, name, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!token) return null;

  return (
    <nav className="navbar">
      <span className="navbar-brand">🌾 UrbanFleet</span>
      <div className="navbar-links">
        <Link to="/products">Products</Link>
        {role === 'FARMER' && <Link to="/products/my">My Products</Link>}
        {role === 'FARMER' && <Link to="/farmer/profile">My Profile</Link>}
        {role === 'BUYER' && <Link to="/buyer/profile">My Profile</Link>}
        {role === 'FARMER' && <Link to="/orders/farmer">Orders</Link>}
        {role === 'BUYER' && <Link to="/orders/buyer">My Orders</Link>}
        <Link to="/ai">AI Chat</Link>
      </div>
      <div className="navbar-user">
        <span>👤 {name} ({role})</span>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}
