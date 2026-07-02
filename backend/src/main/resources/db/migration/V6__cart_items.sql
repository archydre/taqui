-- Carrinho: itens que o usuário salvou pra comprar depois (1 user → N itens, 1 linha por produto).
-- Diferente do pedido, não guarda snapshot de preço/endereço — o checkout continua sendo por
-- produto/vendedor, e é lá que preço e frete são fixados. A unicidade (buyer, product) faz o
-- "adicionar" somar a quantidade em vez de duplicar a linha.

CREATE TABLE public.cart_items (
    id uuid NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    buyer_id uuid NOT NULL,
    product_id uuid NOT NULL,
    CONSTRAINT cart_items_pkey PRIMARY KEY (id),
    CONSTRAINT uq_cart_buyer_product UNIQUE (buyer_id, product_id)
);

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT fk_cart_items_buyer FOREIGN KEY (buyer_id) REFERENCES public.users(user_id);

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES public.products(product_id);

CREATE INDEX idx_cart_items_buyer ON public.cart_items (buyer_id);
