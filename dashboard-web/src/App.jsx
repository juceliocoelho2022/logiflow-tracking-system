import { useMemo, useState } from 'react'

const STATUS_META = {
  PEDIDO_CRIADO: { label: 'Pedido criado', icon: '📦', tone: 'neutral' },
  PAGAMENTO_APROVADO: { label: 'Pagamento aprovado', icon: '✓', tone: 'success' },
  ESTOQUE_RESERVADO: { label: 'Estoque reservado', icon: '▣', tone: 'info' },
  EM_SEPARACAO: { label: 'Em separação', icon: '◫', tone: 'warning' },
  EXPEDIDO: { label: 'Expedido', icon: '↗', tone: 'info' },
  EM_TRANSPORTE: { label: 'Em transporte', icon: '🚚', tone: 'info' },
  SAIU_PARA_ENTREGA: { label: 'Saiu para entrega', icon: '➜', tone: 'warning' },
  ENTREGUE: { label: 'Entregue', icon: '✓', tone: 'success' },
  ENTREGA_NAO_REALIZADA: { label: 'Entrega não realizada', icon: '!', tone: 'danger' },
  CANCELADO: { label: 'Cancelado', icon: '×', tone: 'danger' },
}

function getStatusMeta(status) {
  return (
    STATUS_META[status] ?? {
      label: String(status ?? 'Status desconhecido').replaceAll('_', ' '),
      icon: '•',
      tone: 'neutral',
    }
  )
}

function formatDate(value) {
  if (!value) return '—'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function locationLabel(city, state) {
  if (city && state) return `${city} / ${state}`
  return city || state || 'Local não informado'
}

export default function App() {
  const [trackingCode, setTrackingCode] = useState('')
  const [tracking, setTracking] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const history = useMemo(() => {
    if (!tracking?.history) return []

    return [...tracking.history].sort(
      (a, b) => new Date(a.occurredAt).getTime() - new Date(b.occurredAt).getTime(),
    )
  }, [tracking])

  async function handleSubmit(event) {
    event.preventDefault()

    const code = trackingCode.trim().toUpperCase()
    if (!code) {
      setError('Digite um código de rastreio para consultar.')
      setTracking(null)
      return
    }

    setLoading(true)
    setError('')

    try {
      const response = await fetch(`/api/tracking/${encodeURIComponent(code)}`)

      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('Código de rastreio não encontrado.')
        }
        throw new Error('Não foi possível consultar o rastreamento agora.')
      }

      const data = await response.json()
      setTracking(data)
      setTrackingCode(code)
    } catch (requestError) {
      setTracking(null)
      setError(
        requestError instanceof Error
          ? requestError.message
          : 'Falha inesperada ao consultar o rastreamento.',
      )
    } finally {
      setLoading(false)
    }
  }

  const currentStatus = tracking ? getStatusMeta(tracking.currentStatus) : null

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-mark">LF</div>
          <div>
            <strong>LogiFlow</strong>
            <span>Tracking</span>
          </div>
        </div>
        <div className="system-badge">
          <span className="pulse" />
          Event-driven tracking
        </div>
      </header>

      <main>
        <section className="hero">
          <div className="hero-copy">
            <span className="eyebrow">VISIBILIDADE LOGÍSTICA</span>
            <h1>Acompanhe cada etapa da sua entrega.</h1>
            <p>
              Consulte o código de rastreio para visualizar o status atual, a última
              localização e toda a timeline processada pelo LogiFlow.
            </p>
          </div>

          <form className="search-card" onSubmit={handleSubmit}>
            <label htmlFor="trackingCode">Código de rastreio</label>
            <div className="search-row">
              <div className="input-wrap">
                <span aria-hidden="true">⌕</span>
                <input
                  id="trackingCode"
                  value={trackingCode}
                  onChange={(event) => setTrackingCode(event.target.value)}
                  placeholder="Ex.: LF2026000145BR"
                  autoComplete="off"
                  spellCheck="false"
                />
              </div>
              <button type="submit" disabled={loading}>
                {loading ? 'Consultando…' : 'Rastrear pedido'}
              </button>
            </div>
            <small>Os dados são consultados diretamente na Tracking API.</small>
          </form>
        </section>

        {error && (
          <section className="feedback error-card" role="alert">
            <span className="feedback-icon">!</span>
            <div>
              <strong>Não encontramos o rastreamento</strong>
              <p>{error}</p>
            </div>
          </section>
        )}

        {loading && (
          <section className="loading-panel" aria-label="Carregando rastreamento">
            <div className="skeleton skeleton-title" />
            <div className="skeleton-grid">
              <div className="skeleton" />
              <div className="skeleton" />
              <div className="skeleton" />
            </div>
          </section>
        )}

        {tracking && !loading && (
          <section className="tracking-result">
            <div className="overview-card">
              <div className="overview-heading">
                <div>
                  <span className="section-kicker">STATUS ATUAL</span>
                  <div className={`status-pill ${currentStatus.tone}`}>
                    <span>{currentStatus.icon}</span>
                    {currentStatus.label}
                  </div>
                </div>
                <div className="tracking-code-block">
                  <span>Código de rastreio</span>
                  <strong>{tracking.trackingCode}</strong>
                </div>
              </div>

              <div className="summary-grid">
                <article className="summary-item">
                  <span className="summary-icon">⌖</span>
                  <div>
                    <span>Última localização</span>
                    <strong>
                      {locationLabel(tracking.currentCity, tracking.currentState)}
                    </strong>
                  </div>
                </article>

                <article className="summary-item">
                  <span className="summary-icon">◷</span>
                  <div>
                    <span>Última atualização</span>
                    <strong>{formatDate(tracking.lastUpdate)}</strong>
                  </div>
                </article>

                <article className="summary-item">
                  <span className="summary-icon">#</span>
                  <div>
                    <span>Pedido</span>
                    <strong>{tracking.orderId || '—'}</strong>
                  </div>
                </article>
              </div>
            </div>

            <div className="timeline-card">
              <div className="timeline-header">
                <div>
                  <span className="section-kicker">HISTÓRICO DA ENTREGA</span>
                  <h2>Timeline de rastreamento</h2>
                </div>
                <span className="event-count">
                  {history.length} {history.length === 1 ? 'evento' : 'eventos'}
                </span>
              </div>

              {history.length === 0 ? (
                <div className="empty-state">Nenhum evento registrado para este pedido.</div>
              ) : (
                <ol className="timeline-list">
                  {history.map((item, index) => {
                    const meta = getStatusMeta(item.status)
                    const isLatest = index === history.length - 1

                    return (
                      <li className={`timeline-item ${isLatest ? 'latest' : ''}`} key={item.eventId}>
                        <div className="timeline-rail" aria-hidden="true">
                          <span className={`timeline-dot ${meta.tone}`}>{meta.icon}</span>
                          {index < history.length - 1 && <span className="timeline-line" />}
                        </div>

                        <article className="event-card">
                          <div className="event-topline">
                            <div>
                              <span className={`event-status ${meta.tone}`}>{meta.label}</span>
                              {isLatest && <span className="latest-label">Atual</span>}
                            </div>
                            <time dateTime={item.occurredAt}>{formatDate(item.occurredAt)}</time>
                          </div>

                          <p>{item.description || 'Atualização logística registrada.'}</p>
                          <div className="event-location">
                            <span>⌖</span>
                            {locationLabel(item.city, item.state)}
                          </div>
                        </article>
                      </li>
                    )
                  })}
                </ol>
              )}
            </div>
          </section>
        )}

        {!tracking && !loading && !error && (
          <section className="welcome-panel">
            <div className="welcome-icon">↗</div>
            <div>
              <strong>Pronto para rastrear</strong>
              <p>Digite um código acima para carregar a jornada logística do pedido.</p>
            </div>
          </section>
        )}
      </main>

      <footer>
        <span>LogiFlow Tracking</span>
        <span>Java 21 · Spring Boot · Apache Kafka · React</span>
      </footer>
    </div>
  )
}
