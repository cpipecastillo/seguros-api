INSERT INTO poliza (id, tipo, estado, meses_vigencia, canon_mensual, prima) VALUES
  (1, 'INDIVIDUAL', 'VIGENTE', 12, 1000000.00, 12000000.00),
  (2, 'COLECTIVA', 'VIGENTE', 12, 2500000.00, 30000000.00);

INSERT INTO riesgo (id, poliza_id, estado, identificador_inmueble, direccion, canon_mensual, prima) VALUES
  (1, 1, 'ACTIVO', 'INM-001', 'Calle 10 #20-30', 1000000.00, 12000000.00),
  (2, 2, 'ACTIVO', 'INM-101', 'Carrera 7 #45-12', 1500000.00, 18000000.00),
  (3, 2, 'ACTIVO', 'INM-102', 'Carrera 7 #45-14', 1000000.00, 12000000.00);

ALTER TABLE poliza ALTER COLUMN id RESTART WITH 3;
ALTER TABLE riesgo ALTER COLUMN id RESTART WITH 4;
