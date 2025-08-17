package com.planejao.gestao_projetos.domain;

public enum StatusProjeto {
    EM_ANALISE, ANALISE_REALIZADA, ANALISE_APROVADA, INICIADO, PLANEJADO, EM_ANDAMENTO, ENCERRADO, CANCELADO;

    public boolean isActive() {
        return this != ENCERRADO && this != CANCELADO;
    }
}