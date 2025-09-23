package br.senac.tsi.gamecatalog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Genero extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "O nome do gênero não pode ser vazio")
    @Size(min = 2, max = 50)
    public String nome;

    @Size(max = 200)
    public String descricao;

    // Relacionamento Muitos-para-Muitos (lado inverso)
    @ManyToMany(mappedBy = "generos", fetch = FetchType.LAZY)
    @JsonIgnore // Evita loop na serialização
    public Set<br.senac.tsi.gamecatalog.Jogo> jogos = new HashSet<>();
}

