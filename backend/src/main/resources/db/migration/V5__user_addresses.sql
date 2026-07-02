-- Endereços salvos por usuário (1 user → N endereços). Reaproveita o value object Address
-- (mesmas colunas do @Embeddable usado em orders). O pedido continua guardando sua própria
-- cópia do endereço; estes aqui são só a fonte para pré-preencher o checkout.

CREATE TABLE public.user_addresses (
    address_id uuid NOT NULL,
    recipient_name character varying(255) NOT NULL,
    postal_code character varying(255) NOT NULL,
    street character varying(255) NOT NULL,
    number character varying(255) NOT NULL,
    complement character varying(255),
    district character varying(255) NOT NULL,
    city character varying(255) NOT NULL,
    state character varying(255) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    owner_id uuid NOT NULL,
    CONSTRAINT user_addresses_pkey PRIMARY KEY (address_id)
);

ALTER TABLE ONLY public.user_addresses
    ADD CONSTRAINT fk_user_addresses_owner FOREIGN KEY (owner_id) REFERENCES public.users(user_id);

CREATE INDEX idx_user_addresses_owner ON public.user_addresses (owner_id);
