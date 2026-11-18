import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function MyProducts() {
  const [products, setProducts] = useState([]);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const fetch = async () => {
    try {
      const { data } = await api.get('/products/my');
      setProducts(Array.isArray(data) ? data : data.content ?? []);
    } catch {
      setError('Failed to load your products');
    }
  };

  useEffect(() => { fetch(); }, []);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this product?')) return;
    await api.delete(`/products/${id}`);
    fetch();
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>My Products</h2>
        <button onClick={() => navigate('/products/new')}>+ Add Product</button>
      </div>
      {error && <p className="error">{error}</p>}
      {products.length === 0 && <p>No products yet.</p>}
      <div className="grid">
        {products.map(p => (
          <div key={p.id} className="card">
            <h3>{p.name}</h3>
            <p>{p.description}</p>
            <p className="price">₹{p.price}</p>
            <p className="stock">Stock: {p.quantity}</p>
            <div className="card-actions">
              <button onClick={() => navigate(`/products/${p.id}/edit`)}>Edit</button>
              <button className="danger" onClick={() => handleDelete(p.id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
