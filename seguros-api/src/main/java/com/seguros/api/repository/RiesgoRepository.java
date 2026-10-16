package com.seguros.api.repository;

import com.seguros.api.domain.EstadoRiesgo;
import com.seguros.api.entity.Riesgo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiesgoRepository extends JpaRepository<Riesgo, Long> {

    List<Riesgo> findByPoliza_IdOrderById(Long polizaId);

    List<Riesgo> findByPoliza_IdAndEstado(Long polizaId, EstadoRiesgo estado);
}
