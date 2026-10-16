package com.seguros.api.repository;

import com.seguros.api.domain.EstadoPoliza;
import com.seguros.api.domain.TipoPoliza;
import com.seguros.api.entity.Poliza;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    @Query("""
            SELECT p FROM Poliza p
            WHERE (:tipo IS NULL OR p.tipo = :tipo)
              AND (:estado IS NULL OR p.estado = :estado)
            ORDER BY p.id
            """)
    List<Poliza> findByFiltros(@Param("tipo") TipoPoliza tipo, @Param("estado") EstadoPoliza estado);
}
