import { useEffect, useState } from 'react';
import api from '../api/axios';

const empty = { farmName: '', location: '', bio: '' };

export default function FarmerProfile() {
  const [form, setForm] = useState(empty);
  const [exists, setExists] = useState(false);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/farmer/profile/me')
      .then(({ data }) => { setForm(data); setExists(true); })
      .catch(() => setExists(false));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMsg(''); setError('');
    try {
      if (exists) await api.put('/farmer/profile/me', form);
      else await api.post('/farmer/profile', form);
      setMsg('Profile saved!');
      setExists(true);
    } catch {
      setError('Failed to save profile');
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete your profile?')) return;
    await api.delete('/farmer/profile/me');
    setForm(empty);
    setExists(false);
    setMsg('Profile deleted');
  };

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div className="page">
      <div className="form-container">
        <h2>Farmer Profile</h2>
        {msg && <p className="success">{msg}</p>}
        {error && <p className="error">{error}</p>}
        <form onSubmit={handleSubmit}>
          <input placeholder="Farm Name" value={form.farmName ?? ''} onChange={set('farmName')} required />
          <input placeholder="Location" value={form.location ?? ''} onChange={set('location')} required />
          <textarea placeholder="Bio" value={form.bio ?? ''} onChange={set('bio')} />
          <div className="form-actions">
            <button type="submit">{exists ? 'Update' : 'Create'} Profile</button>
            {exists && <button type="button" className="danger" onClick={handleDelete}>Delete Profile</button>}
          </div>
        </form>
      </div>
    </div>
  );
}
