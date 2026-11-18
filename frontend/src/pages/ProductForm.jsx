import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/axios';

const empty = { name: '', description: '', price: '', quantity: '' };

export default function ProductForm() {
  const [form, setForm] = useState(empty);
  const [error, setError] = useState('');
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  useEffect(() => {
    if (isEdit) {
      api.get(`/products/${id}`).then(({ data }) => setForm({
        name: data.name, description: data.description,
        price: data.price, quantity: data.quantity,
      }));
    }
  }, [id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (isEdit) await api.put(`/products/${id}`, form);
      else await api.post('/products', form);
      navigate('/products/my');
    } catch {
      setError('Failed to save product');
    }
  };

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div className="page">
      <div className="form-container">
        <h2>{isEdit ? 'Edit Product' : 'Add Product'}</h2>
        {error && <p className="error">{error}</p>}
        <form onSubmit={handleSubmit}>
          <input placeholder="Product Name" value={form.name} onChange={set('name')} required />
          <textarea placeholder="Description" value={form.description} onChange={set('description')} required />
          <input placeholder="Price (₹)" type="number" step="0.01" value={form.price} onChange={set('price')} required />
          <input placeholder="Quantity" type="number" value={form.quantity} onChange={set('quantity')} required />
          <div className="form-actions">
            <button type="button" onClick={() => navigate('/products/my')}>Cancel</button>
            <button type="submit">{isEdit ? 'Update' : 'Add Product'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}
