export type CasoTipo = 'RMG' | 'RMGS';

export type EstadoCaso =
  | 'RASCUNHO'
  | 'PROPOSTA_ENVIADA'
  | 'EM_ANALISE'
  | 'DEVOLVIDO'
  | 'APROVADO'
  | 'CANCELADO';

export type AcaoCaso = 'ENVIAR' | 'ASSUMIR_ANALISE' | 'DEVOLVER' | 'APROVAR' | 'CANCELAR';

export type EstadoLinha = 'PROPOSTA' | 'EDITADA' | 'EXCLUIDA';

export interface CasoResumo {
  id: number;
  numero: string;
  tipo: CasoTipo;
  empresaCodigo: string;
  exercicio: number;
  tipoAssembleia: string;
  dataAssembleia: string | null;
  estado: EstadoCaso;
  justificativa: string | null;
  responsavel: string | null;
  criadoPor: string;
  criadoEm: string;
  atualizadoEm: string;
}

export interface Totais {
  vigente: string;
  proposto: string;
  variacaoPercentual: string;
}

export interface LinhaResposta {
  id: number;
  instanciaId: number;
  instancia: string;
  cargoId: number;
  cargo: string;
  quantidadeMembros: number;
  honorarioVigente: string;
  honorarioProposto: string;
  beneficioVigente: string;
  beneficioProposto: string;
  totalVigente: string;
  totalProposto: string;
  variacaoPercentual: string;
  estado: EstadoLinha;
}

export interface CasoDetalhe {
  caso: CasoResumo;
  linhas: LinhaResposta[];
  totais: Totais;
}

export interface Instancia {
  id: number;
  codigo: string;
  nome: string;
  descricao: string | null;
  ativo: boolean;
}

export interface Cargo {
  id: number;
  codigo: string;
  nome: string;
  ativo: boolean;
}

export interface Empresa {
  codigo: string;
  sigla: string;
  setor: string;
  porte: string;
  controle: string;
  controladora: boolean;
}

export const ROTULO_ESTADO: Record<EstadoCaso, string> = {
  RASCUNHO: 'Rascunho',
  PROPOSTA_ENVIADA: 'Proposta enviada',
  EM_ANALISE: 'Em análise',
  DEVOLVIDO: 'Devolvido',
  APROVADO: 'Aprovado',
  CANCELADO: 'Cancelado',
};

export const ROTULO_ACAO: Record<AcaoCaso, string> = {
  ENVIAR: 'Enviar proposta',
  ASSUMIR_ANALISE: 'Assumir análise',
  DEVOLVER: 'Devolver',
  APROVAR: 'Aprovar',
  CANCELAR: 'Cancelar',
};
