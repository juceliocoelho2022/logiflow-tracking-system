CREATE TABLE tracking_event (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    tracking_code VARCHAR(60) NOT NULL,
    order_id VARCHAR(60) NOT NULL,
    status VARCHAR(40) NOT NULL,
    city VARCHAR(120),
    state VARCHAR(2),
    description VARCHAR(500),
    occurred_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_tracking_event_event_id UNIQUE (event_id)
);

CREATE INDEX idx_tracking_event_tracking_code ON tracking_event (tracking_code);
CREATE INDEX idx_tracking_event_order_id ON tracking_event (order_id);
CREATE INDEX idx_tracking_event_occurred_at ON tracking_event (occurred_at);
