package br.senac.tsi.gamecatalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
public class PerfilDesenvolvedora extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Size(max = 2000)
    @Column(length = 2000)
    public String historia;

    @Size(max = 100)
    public String principaisFranquias;

    // Relacionamento Um-para-Um (lado inverso)
    @OneToOne(mappedBy = "perfil", fetch = FetchType.LAZY)
    @JsonIgnore
    public Desenvolvedora desenvolvedora; // Simplificado
}