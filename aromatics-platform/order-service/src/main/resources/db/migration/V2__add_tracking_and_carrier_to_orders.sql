-- Bổ sung cột mã vận đơn và đơn vị vận chuyển khớp với Order.java Entity
ALTER TABLE orders
    ADD COLUMN tracking_number VARCHAR(100) NULL AFTER staff_notes,
    ADD COLUMN carrier VARCHAR(100) NULL AFTER tracking_number;
