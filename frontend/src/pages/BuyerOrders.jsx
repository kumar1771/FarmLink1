import { useEffect, useState } from 'react';
import api from '../api/axios';

const STATUS_BADGE = {
  PENDING: 'badge-yellow', CONFIRMED: 'badge-blue', SHIPPED: 'badge-purple',
  DELIVERED: 'badge-green', CANCELLED: 'badge-red',
};

export default function BuyerOrders() {
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState('');

  const fetchOrders = async (p = 0) => {
    try {
      const { data } = await api.get(`/orders/buyer?page=${p}&size=10`);
      const list = Array.isArray(data) ? data : data.content ?? [];
      setOrders(list);
      setHasMore(list.length === 10);
    } catch {
      setError('Failed to load orders');
    }
  };

  useEffect(() => { fetchOrders(page); }, [page]);

  const updateStatus = async (id, status) => {
    try {
      await api.put(`/orders/${id}/status`, { status });
      fetchOrders(page);
    } catch {
      alert('Failed to update status');
    }
  };

  return (
    <div className="page">
      <h2>My Orders</h2>
      {error && <p className="error">{error}</p>}
      {orders.length === 0 && <p>No orders yet.</p>}
      <div className="order-list">
        {orders.map(o => (
          <div key={o.id} className="order-card">
            <div className="order-header">
              <span>Order #{o.id}</span>
              <span className={`badge ${STATUS_BADGE[o.status]}`}>{o.status}</span>
            </div>
            <p>Product ID: {o.productId}</p>
            <p>Quantity: {o.quantity}</p>
            <p>Address: {o.deliveryAddress}</p>
            <div className="order-actions">
              {o.status === 'SHIPPED' && (
                <button onClick={() => updateStatus(o.id, 'DELIVERED')}>Mark Delivered</button>
              )}
              {o.status !== 'DELIVERED' && o.status !== 'CANCELLED' && (
                <button className="danger" onClick={() => updateStatus(o.id, 'CANCELLED')}>Cancel</button>
              )}
            </div>
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
