-- Notificações in-app. recipient_id é uuid puro (sem FK, como o audit_log). A mensagem já vem
-- montada (denormalizada); o front monta o link a partir de related_entity_type + related_entity_id.

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    recipient_id uuid NOT NULL,
    type character varying(50) NOT NULL,
    related_entity_type character varying(50) NOT NULL,
    related_entity_id uuid NOT NULL,
    message text NOT NULL,
    read boolean DEFAULT false NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT notifications_pkey PRIMARY KEY (id)
);

-- unread-count e listagem por destinatário são as queries quentes.
CREATE INDEX idx_notifications_recipient_read ON public.notifications (recipient_id, read);
