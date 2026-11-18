import { useEffect, useState } from 'react';
import api from '../api/axios';

const empty = { fullName: '', address: '', city: '' };

export default function BuyerProfile() {
  const [form, setForm] = useState(empty);
  const [exists, setExists] = useState(false);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/buyer/profile/me')
      .then(({ data }) => { setForm(data); setExists(true); })
      .catch(() => setExists(false));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMsg(''); setError('');
    try {
      if (exists) await api.put('/buyer/profile/me', form);
      else await api.post('/buyer/profile', form);
      setMsg('Profile saved!');
      setExists(true);
    } catch {
      setError('Failed to save profile');
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete your profile?')) return;
    await api.delete('/buyer/profile/me');
    setForm(empty);
    setExists(false);
    setMsg('Profile deleted');
  };

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div className="page">
      <div className="form-container">
        <h2>Buyer Profile</h2>
        {msg && <p className="success">{msg}</p>}
        {error && <p className="error">{error}</p>}
        <form onSubmit={handleSubmit}>
          <input placeholder="Full Name" value={form.fullName ?? ''} onChange={set('fullName')} required />
          <input placeholder="Address" value={form.address ?? ''} onChange={set('address')} required />
          <input placeholder="City" value={form.city ?? ''} onChange={set('city')} required />
          <div className="form-actions">
            <button type="submit">{exists ? 'Update' : 'Create'} Profile</button>
            {exists && <button type="button" className="danger" onClick={handleDelete}>Delete Profile</button>}
          </div>
        </form>
      </div>
    </div>
  );
}
