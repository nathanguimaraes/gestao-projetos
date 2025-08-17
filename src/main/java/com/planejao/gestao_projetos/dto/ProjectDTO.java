package com.planejao.gestao_projetos.dto;

import com.planejao.gestao_projetos.domain.StatusProjeto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProjectDTO(Long id, String nome, LocalDate dataInicio, LocalDate previsaoTermino, LocalDate dataRealTermino,
                         BigDecimal orcamentoTotal, String descricao, MemberDTO gerente, StatusProjeto status, String risco, List<Long> membros) {}