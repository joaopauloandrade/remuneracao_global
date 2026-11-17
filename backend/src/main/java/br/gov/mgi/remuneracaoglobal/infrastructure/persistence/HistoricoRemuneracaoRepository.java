package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoRemuneracaoRepository extends JpaRepository<HistoricoRemuneracaoEntity, Long> {

    Optional<HistoricoRemuneracaoEntity> findFirstByEmpresaCodigoAndInstanciaIdAndCargoIdAndExercicioLessThanOrderByExercicioDescIdDesc(
            String empresaCodigo, Long instanciaId, Long cargoId, int exercicio);
}
