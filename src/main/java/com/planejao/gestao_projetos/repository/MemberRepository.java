package com.planejao.gestao_projetos.repository;

import com.planejao.gestao_projetos.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByCargo(String cargo);
}
