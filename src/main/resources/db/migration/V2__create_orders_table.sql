CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        customer_name VARCHAR(200) NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
                        total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL REFERENCES orders(id),
                             product_id BIGINT NOT NULL REFERENCES products(id),
                             quantity INTEGER NOT NULL CHECK (quantity > 0),
                             unit_price NUMERIC(19, 2) NOT NULL,
                             total_price NUMERIC(19, 2) NOT NULL
);

CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_customer ON orders(customer_name);
CREATE INDEX idx_order_item_order ON order_items(order_id);
CREATE INDEX idx_order_item_product ON order_items(product_id);