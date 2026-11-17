package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CasoRepository extends JpaRepository<CasoEntity, Long> {

    List<CasoEntity> findAllByOrderByAtualizadoEmDesc();
}
