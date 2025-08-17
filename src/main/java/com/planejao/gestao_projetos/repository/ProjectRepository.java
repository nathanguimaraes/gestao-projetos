package com.planejao.gestao_projetos.repository;
import com.planejao.gestao_projetos.domain.Project;
import com.planejao.gestao_projetos.domain.StatusProjeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByStatus(StatusProjeto status, Pageable pageable);
}