import { useState, useEffect } from 'react'
import axios from 'axios'
import {
  Link as LinkIcon,
  Copy,
  Check,
  History,
  Trash2,
  ExternalLink,
  Zap,
  AlertCircle,
  Loader2
} from 'lucide-react'
import './App.css'

function App() {
  const [url, setUrl] = useState('')
  const [shortCode, setShortCode] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [copied, setCopied] = useState(false)
  const [history, setHistory] = useState([])

  // Load history from localStorage
  useEffect(() => {
    const savedHistory = localStorage.getItem('shorty_history')
    if (savedHistory) {
      try {
        setHistory(JSON.parse(savedHistory))
      } catch (e) {
        console.error('Failed to parse history', e)
      }
    }
  }, [])

  // Save history to localStorage
  useEffect(() => {
    localStorage.setItem('shorty_history', JSON.stringify(history))
  }, [history])

  const handleShorten = async (e) => {
    e.preventDefault()
    if (!url) return

    setLoading(true)
    setError('')
    setShortCode('')

    try {
      const response = await axios.post('http://localhost:8080/shorten', { url })
      const newShortCode = response.data.shortCode
      setShortCode(newShortCode)

      // Add to history
      const newEntry = {
        id: Date.now(),
        originalUrl: url,
        shortCode: newShortCode,
        timestamp: new Date().toISOString()
      }

      // Keep only unique original URLs in history (most recent first)
      setHistory(prev => {
        const filtered = prev.filter(item => item.originalUrl !== url)
        return [newEntry, ...filtered].slice(0, 5) // Keep last 5
      })

      setUrl('') // clear input on success
    } catch (err) {
      setError(err.response?.data || 'Something went wrong. Is the backend running?')
    } finally {
      setLoading(false)
    }
  }

  const copyToClipboard = (code) => {
    const fullUrl = `http://localhost:8080/${code}`
    navigator.clipboard.writeText(fullUrl)
    setCopied(code)
    setTimeout(() => setCopied(false), 2000)
  }

  const clearHistory = () => {
    if (window.confirm('Clear all recent links?')) {
      setHistory([])
    }
  }

  const removeHistoryItem = (id) => {
    setHistory(prev => prev.filter(item => item.id !== id))
  }

  return (
    <>
      <header>
        <div className="logo-container">
          <Zap className="logo-icon" size={32} fill="currentColor" />
          <h1>url-shortener</h1>
        </div>
        <p className="subtitle">Lighter link, longer reach.</p>
      </header>

      <main>
        <div className="main-card">
          <form onSubmit={handleShorten} className="input-section">
            <div className="input-container">
              <LinkIcon className="input-icon" size={20} />
              <input
                type="url"
                placeholder="Paste your long URL here..."
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                required
              />
            </div>

            <button type="submit" className="shorten-btn" disabled={loading}>
              {loading ? (
                <>
                  <Loader2 className="animate-spin" size={20} />
                  <span>Shortening...</span>
                </>
              ) : (
                <>
                  <span>Get Short Link</span>
                  <Zap size={18} />
                </>
              )}
            </button>

            {error && (
              <div className="error-message">
                <AlertCircle size={18} />
                <span>{error}</span>
              </div>
            )}
          </form>

          {shortCode && (
            <div className="result-section">
              <span className="result-label">Your shortened link</span>
              <div className="result-box">
                <a
                  href={`http://localhost:8080/${shortCode}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="short-link"
                >
                  {`localhost:8080/${shortCode}`}
                </a>
                <div className="action-buttons">
                  <button
                    className={`icon-btn ${copied === shortCode ? 'success' : ''}`}
                    onClick={() => copyToClipboard(shortCode)}
                    title="Copy to clipboard"
                  >
                    {copied === shortCode ? <Check size={18} /> : <Copy size={18} />}
                  </button>
                  <a
                    href={`http://localhost:8080/${shortCode}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="icon-btn"
                    title="Open link"
                  >
                    <ExternalLink size={18} />
                  </a>
                </div>
              </div>
            </div>
          )}
        </div>

        {history.length > 0 && (
          <section className="history-section">
            <div className="history-header">
              <div className="history-title">
                <History size={20} />
                <span>Recent Links</span>
              </div>
              <button className="clear-btn" onClick={clearHistory}>
                <Trash2 size={14} />
                <span>Clear All</span>
              </button>
            </div>

            <div className="history-list">
              {history.map((item) => (
                <div key={item.id} className="history-item">
                  <div className="history-info">
                    <a
                      href={`http://localhost:8080/${item.shortCode}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="history-short-link"
                    >
                      {`localhost:8080/${item.shortCode}`}
                    </a>
                    <span className="original-url">{item.originalUrl}</span>
                  </div>
                  <div className="action-buttons">
                    <button
                      className={`icon-btn ${copied === item.shortCode ? 'success' : ''}`}
                      onClick={() => copyToClipboard(item.shortCode)}
                    >
                      {copied === item.shortCode ? <Check size={16} /> : <Copy size={16} />}
                    </button>
                    <button className="icon-btn" onClick={() => removeHistoryItem(item.id)}>
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}
      </main>

    </>
  )
}

export default App
