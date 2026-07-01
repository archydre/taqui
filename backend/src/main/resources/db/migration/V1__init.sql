-- Baseline do schema, extraído com pg_dump do schema que o Hibernate gera a partir das
-- entidades atuais (users, products, posts, orders, comments). A partir daqui o Flyway é o
-- dono do schema: toda mudança vira um V2__..., V3__... e o Hibernate roda em modo `validate`.

CREATE TABLE public.users (
    user_id uuid NOT NULL,
    display_name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    email_verified boolean DEFAULT false NOT NULL,
    password character varying(255) NOT NULL,
    pix_key character varying(255),
    postal_code character varying(255),
    username character varying(255) NOT NULL,
    verification_token character varying(255),
    verification_token_expires_at timestamp(6) with time zone,
    whatsapp character varying(255)
);

CREATE TABLE public.products (
    product_id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    height numeric(38,2),
    image_url character varying(255),
    length numeric(38,2),
    price numeric(38,2) NOT NULL,
    product_description character varying(255),
    product_name character varying(255) NOT NULL,
    thumbnail_url character varying(255),
    updated_at timestamp(6) with time zone NOT NULL,
    weight numeric(38,2),
    width numeric(38,2),
    owner_id uuid NOT NULL
);

CREATE TABLE public.posts (
    post_id uuid NOT NULL,
    content character varying(255),
    created_at timestamp(6) with time zone NOT NULL,
    image_url character varying(255),
    thumbnail_url character varying(255),
    updated_at timestamp(6) with time zone,
    owner_id uuid NOT NULL,
    product_id uuid
);

CREATE TABLE public.orders (
    order_id uuid NOT NULL,
    city character varying(255) NOT NULL,
    complement character varying(255),
    district character varying(255) NOT NULL,
    number character varying(255) NOT NULL,
    postal_code character varying(255) NOT NULL,
    recipient_name character varying(255) NOT NULL,
    state character varying(255) NOT NULL,
    street character varying(255) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    freight_price numeric(38,2) NOT NULL,
    freight_service character varying(255) NOT NULL,
    quantity integer NOT NULL,
    seller_pix_key character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    total numeric(38,2) NOT NULL,
    unit_price numeric(38,2) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    buyer_id uuid NOT NULL,
    product_id uuid NOT NULL,
    CONSTRAINT orders_status_check CHECK (((status)::text = ANY ((ARRAY['AGUARDANDO_PAGAMENTO'::character varying, 'PAGO'::character varying, 'ENVIADO'::character varying, 'CANCELADO'::character varying])::text[])))
);

CREATE TABLE public.comments (
    comment_id uuid NOT NULL,
    content character varying(500) NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    author_id uuid NOT NULL,
    post_id uuid,
    product_id uuid
);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (product_id);

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT posts_pkey PRIMARY KEY (post_id);

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (order_id);

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (comment_id);

ALTER TABLE ONLY public.products
    ADD CONSTRAINT fkmodgy1j6kai83i3mweyp731qc FOREIGN KEY (owner_id) REFERENCES public.users(user_id);

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fk452mmghnrxq6h7f4f5v8ge95f FOREIGN KEY (owner_id) REFERENCES public.users(user_id);

ALTER TABLE ONLY public.posts
    ADD CONSTRAINT fkjfxaq0lgtcr5igk5kkm1ttpj6 FOREIGN KEY (product_id) REFERENCES public.products(product_id);

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fkhtx3insd5ge6w486omk4fnk54 FOREIGN KEY (buyer_id) REFERENCES public.users(user_id);

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fkkp5k52qtiygd8jkag4hayd0qg FOREIGN KEY (product_id) REFERENCES public.products(product_id);

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT fkn2na60ukhs76ibtpt9burkm27 FOREIGN KEY (author_id) REFERENCES public.users(user_id);

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT fkh4c7lvsc298whoyd4w9ta25cr FOREIGN KEY (post_id) REFERENCES public.posts(post_id);

ALTER TABLE ONLY public.comments
    ADD CONSTRAINT fk6uv0qku8gsu6x1r2jkrtqwjtn FOREIGN KEY (product_id) REFERENCES public.products(product_id);
