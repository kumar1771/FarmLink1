import { useState } from 'react';
import api from '../api/axios';

export default function AiChat() {
  const [messages, setMessages] = useState([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;
    const userMsg = { role: 'user', text: query };
    setMessages(prev => [...prev, userMsg]);
    setQuery('');
    setLoading(true);
    try {
      const { data } = await api.post('/Ai/openAi', { query });
      setMessages(prev => [...prev, { role: 'ai', text: data.response ?? data }]);
    } catch {
      setMessages(prev => [...prev, { role: 'ai', text: 'Sorry, something went wrong.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="chat-container">
        <h2>🤖 AI Assistant</h2>
        <div className="chat-messages">
          {messages.length === 0 && <p className="chat-hint">Ask anything about farming, crops, or orders!</p>}
          {messages.map((m, i) => (
            <div key={i} className={`chat-bubble ${m.role === 'user' ? 'bubble-user' : 'bubble-ai'}`}>
              {m.text}
            </div>
          ))}
          {loading && <div className="chat-bubble bubble-ai">Thinking...</div>}
        </div>
        <form className="chat-input" onSubmit={handleSend}>
          <input
            placeholder="Ask something..."
            value={query}
            onChange={e => setQuery(e.target.value)}
            disabled={loading}
          />
          <button type="submit" disabled={loading}>Send</button>
        </form>
      </div>
    </div>
  );
}
