import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../api/axios';

export default function PlaceOrder() {
  const location = useLocation();
  const [form, setForm] = useState({ productId: location.state?.productId ?? '', quantity: 1, deliveryAddress: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/orders', form);
      setSuccess('Order placed successfully!');
      setTimeout(() => navigate('/orders/buyer'), 1500);
    } catch {
      setError('Failed to place order. Check stock availability.');
    }
  };

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div className="page">
      <div className="form-container">
        <h2>Place Order</h2>
        {error && <p className="error">{error}</p>}
        {success && <p className="success">{success}</p>}
        <form onSubmit={handleSubmit}>
          <input placeholder="Product ID" value={form.productId} onChange={set('productId')} required />
          <input placeholder="Quantity" type="number" min="1" value={form.quantity} onChange={set('quantity')} required />
          <input placeholder="Delivery Address" value={form.deliveryAddress} onChange={set('deliveryAddress')} required />
          <div className="form-actions">
            <button type="button" onClick={() => navigate('/products')}>Cancel</button>
            <button type="submit">Place Order</button>
          </div>
        </form>
      </div>
    </div>
  );
}
