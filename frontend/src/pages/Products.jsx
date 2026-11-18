import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

export default function Products() {
  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState('');
  const { role } = useAuth();
  const navigate = useNavigate();

  const fetchProducts = async (p = 0) => {
    try {
      const { data } = await api.get(`/products?page=${p}&size=12`);
      const list = Array.isArray(data) ? data : data.content ?? [];
      setProducts(list);
      setHasMore(list.length === 12);
    } catch {
      setError('Failed to load products');
    }
  };

  useEffect(() => { fetchProducts(page); }, [page]);

  const handleOrder = (productId) => navigate('/orders/new', { state: { productId } });

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this product?')) return;
    await api.delete(`/products/${id}`);
    fetchProducts(page);
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>Products</h2>
        {role === 'FARMER' && <button onClick={() => navigate('/products/new')}>+ Add Product</button>}
      </div>
      {error && <p className="error">{error}</p>}
      <div className="grid">
        {products.map(p => (
          <div key={p.id} className="card">
            <h3>{p.name}</h3>
            <p>{p.description}</p>
            <p className="price">₹{p.price}</p>
            <p className="stock">Stock: {p.quantity}</p>
            {role === 'BUYER' && <button onClick={() => handleOrder(p.id)}>Order</button>}
            {role === 'FARMER' && (
              <div className="card-actions">
                <button onClick={() => navigate(`/products/${p.id}/edit`)}>Edit</button>
                <button className="danger" onClick={() => handleDelete(p.id)}>Delete</button>
              </div>
            )}
          </div>
        ))}
      </div>
      <div className="pagination">
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Prev</button>
        <span>Page {page + 1}</span>
        <button disabled={!hasMore} onClick={() => setPage(p => p + 1)}>Next →</button>
      </div>
    </div>
  );
}
