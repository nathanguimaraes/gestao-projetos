package com.planejao.gestao_projetos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private LocalDate dataInicio;

    private LocalDate previsaoTermino;

    private LocalDate dataRealTermino;

    private BigDecimal orcamentoTotal;

    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerente_id", referencedColumnName = "id")
    private Member gerente;

    @Enumerated(EnumType.STRING)
    private StatusProjeto status;

    @ElementCollection
    @CollectionTable(name = "project_membros", joinColumns = @JoinColumn(name = "project_id"))
    private List<Long> membros = new ArrayList<>();

    public String getRisco() {
        if (dataInicio == null || previsaoTermino == null || orcamentoTotal == null) {
            return "Indefinido";
        }
        long meses = ChronoUnit.MONTHS.between(dataInicio, previsaoTermino);
        BigDecimal low = new BigDecimal("100000");
        BigDecimal high = new BigDecimal("500000");
        if (orcamentoTotal.compareTo(high) > 0 || meses > 6) {
            return "Alto";
        } else if (orcamentoTotal.compareTo(low) > 0 || (meses > 3 && meses <= 6)) {
            return "Medio";
        } else if (orcamentoTotal.compareTo(low) <= 0 && meses <= 3) {
            return "Baixo";
        } else {
            return "Medio"; // Fallback para casos borda
        }
    }

    // Remover setGerenteId e getGerenteId, pois o acesso é via 'gerente'
    // Substituir por métodos que trabalham com Member, se necessário
    public void setGerente(Member gerente) {
        this.gerente = gerente;
    }

    public Member getGerente() {
        return this.gerente;
    }
}