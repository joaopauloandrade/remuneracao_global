package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanciaGestaoRepository extends JpaRepository<InstanciaGestaoEntity, Long> {

    List<InstanciaGestaoEntity> findAllByOrderByNomeAsc();
}
