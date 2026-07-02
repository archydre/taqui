-- Autoria (quem criou / quem alterou por último). Colunas nullable: linhas já existentes
-- e ações sem login (register, sistema) ficam com created_by/updated_by nulos.

ALTER TABLE public.products ADD COLUMN created_by uuid;
ALTER TABLE public.products ADD COLUMN updated_by uuid;

ALTER TABLE public.posts ADD COLUMN created_by uuid;
ALTER TABLE public.posts ADD COLUMN updated_by uuid;

ALTER TABLE public.orders ADD COLUMN created_by uuid;
ALTER TABLE public.orders ADD COLUMN updated_by uuid;

ALTER TABLE public.comments ADD COLUMN created_by uuid;
ALTER TABLE public.comments ADD COLUMN updated_by uuid;
