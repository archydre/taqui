-- Trilha de ações (quem fez qual ação, sobre qual entidade). Não estende autoria
-- (não tem created_by/updated_by) para não auditar a própria tabela de auditoria.

CREATE TABLE public.audit_log (
    id uuid NOT NULL,
    actor_id uuid,
    action character varying(50) NOT NULL,
    entity_type character varying(50) NOT NULL,
    entity_id uuid NOT NULL,
    details text,
    created_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT audit_log_pkey PRIMARY KEY (id)
);
