package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinhaPropostaRepository extends JpaRepository<LinhaPropostaEntity, Long> {

    List<LinhaPropostaEntity> findByCasoIdOrderByIdAsc(Long casoId);

    Optional<LinhaPropostaEntity> findByIdAndCasoId(Long id, Long casoId);
}
