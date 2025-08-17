package com.planejao.gestao_projetos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProjectRequestDTO(
        @NotBlank String nome,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate previsaoTermino,
        @NotNull @Positive BigDecimal orcamentoTotal,
        @NotBlank String descricao,
        @NotNull Long gerenteId,
        @NotNull List<Long> membros) {}