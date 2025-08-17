package com.planejao.gestao_projetos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectUpdateDTO(String nome, LocalDate dataInicio, LocalDate previsaoTermino, LocalDate dataRealTermino,
                               BigDecimal orcamentoTotal, String descricao, Long gerenteId) {}